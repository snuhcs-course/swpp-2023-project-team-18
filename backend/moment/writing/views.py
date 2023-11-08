from datetime import datetime, timedelta
import re
import json
from collections import Counter

from rest_framework import permissions
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response
from rest_framework.request import Request
from django.db.models import Q

from user.models import User
from .models import MomentPair, Story, Hashtag
from .serializers import (
    MomentPairQuerySerializer,
    MomentPairSerializer,
    MomentPairCreateSerializer,
    DayCompletionSerializer,
    StorySerializer,
    StoryQuerySerializer,
    StoryCreateSerializer,
    StoryDeleteSerializer,
    EmotionCreateSerializer,
    EmotionQuerySerializer,
    ScoreCreateSerializer,
    HashtagSerializer,
    HashtagCreateSerializer,
    HashtagQuerySerializer,
)
from .constants import (
    Emotions,
)
from .utils.gpt import GPTAgent
from .utils.log import log
from .utils.prompt import MomentReplyTemplate, StoryGenerateTemplate


# View related to moments
class MomentView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = MomentPairSerializer

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.gpt_agent = GPTAgent()

    def get(self, request: Request) -> Response:
        params = MomentPairQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        start_date = datetime.fromtimestamp(params.validated_data["start"])
        end_date = datetime.fromtimestamp(params.validated_data["end"])

        moment_pairs = MomentPair.objects.filter(
            moment_created_at__range=(start_date, end_date),
            user=user,
        ).order_by("moment_created_at")
        serializer = self.get_serializer(moment_pairs, many=True)

        log(
            f"Successfully queried moments (length: {len(serializer.data)})",
            username=user.username,
            place="MomentView.get",
        )

        return Response(
            data={"moments": serializer.data},
            status=200,
        )

    def post(self, request: Request) -> Response:
        body = MomentPairCreateSerializer(data=request.data)
        body.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        self.gpt_agent.reset_messages()
        prompt = MomentReplyTemplate.get_prompt(moment=body.data["moment"])
        self.gpt_agent.add_message(prompt)

        try:
            reply = self.gpt_agent.get_answer(
                timeout=15, max_trial=2
            )  # TODO: 테스트 해보고 시간 파라미터 조절하기

        except GPTAgent.GPTError:
            log(
                f"Error while calling GPT API",
                tag="error",
                username=user.username,
                place="MomentView.post",
            )

            # Failure does not affect the user's quota
            for throttle in self.get_throttles():
                history = throttle.cache.get(throttle.get_cache_key(request, self), [])

                throttle.cache.set(
                    throttle.get_cache_key(request, self),
                    history[1:],
                )

            return Response(
                data={"error": "GPT API call failed"},
                status=500,
            )

        moment_pair = MomentPair.objects.create(
            user=user,
            moment=body.data["moment"],
            reply=reply,
            story=None,
            moment_created_at=datetime.now(),
            reply_created_at=datetime.now(),
        )
        moment_pair.save()

        log(
            f"Successfully created moment",
            username=user.username,
            place="MomentView.post",
        )

        serializer = self.get_serializer(moment_pair)

        return Response(
            data={"moment": serializer.data},
            status=201,
        )

    # Override
    def get_throttles(self):
        if self.request.method.lower() == "post":
            self.throttle_scope = "moment-reply"

        return super().get_throttles()


# View related to Stories
class StoryView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = StorySerializer

    def get(self, request: Request) -> Response:
        params = StoryQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        start_date = datetime.fromtimestamp(params.validated_data["start"])
        end_date = datetime.fromtimestamp(params.validated_data["end"])

        stories = Story.objects.filter(
            created_at__range=(start_date, end_date),
            user=user,
        ).order_by("created_at")

        serializer = self.get_serializer(stories, many=True)

        log(
            f"Successfully queried stories (length: {len(serializer.data)})",
            username=user.username,
            place="StoryView.get",
        )

        return Response(
            data={"stories": serializer.data},
            status=200,
        )

    def post(self, request: Request) -> Response:
        body = StoryCreateSerializer(data=request.data)
        body.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        content = body.validated_data["content"]
        title = body.validated_data["title"]

        story = Story.objects.filter(
            user=user,
        ).latest("created_at")

        story.title = title
        story.content = content
        story.save()

        log(
            f"Successfully posted story",
            username=user.username,
            place="StoryView.post",
        )

        return Response(
            data={"message": "Success!"},
            status=201,
        )

    def delete(self, request: Request) -> Response:
        body = StoryDeleteSerializer(data=request.data)
        body.is_valid(raise_exception=True)

        story_id = body.validated_data["story_id"]

        story = Story.objects.get(id=story_id)
        story.delete()

        return Response(
            data={"message": "Deleted story!"},
            status=204,
        )


class StoryGenerateView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    # serializer_class = StoryCreateSerializer

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.gpt_agent = GPTAgent()

    def get(self, request: Request) -> Response:
        user = User.objects.get(pk=request.user.id)

        story = Story.objects.filter(
            user=user,
        ).latest("created_at")

        story_date = story.created_at
        curr_date = (story_date - timedelta(hours=3)).date()

        log(
            f"curr_date: {curr_date}",
            username=user.username,
            place="StoryGenerateView.get",
        )

        start_date = datetime(curr_date.year, curr_date.month, curr_date.day, 3)
        end_date = start_date + timedelta(hours=24)

        log(
            f"start and end date: {start_date}, {end_date}",
            username=user.username,
            place="StoryGenerateView.get",
        )

        moment_pairs = MomentPair.objects.filter(
            moment_created_at__range=(start_date, end_date),
            user=user,
        ).order_by("moment_created_at")

        moment_contents = [moment_pair.moment for moment_pair in moment_pairs]

        # log(f"Moments: {moment_contents}", place="StoryGenerateView.get")

        self.gpt_agent.reset_messages()
        prompt = StoryGenerateTemplate.get_prompt(moments=";".join(moment_contents))
        self.gpt_agent.add_message(prompt)

        # log(f"Prompt: {prompt}", place="StoryGenerateView.get")

        try:
            title_and_story = json.loads(
                self.gpt_agent.get_answer(timeout=20, max_trial=2)
            )

            title = title_and_story["title"]
            story = title_and_story["content"]

        except GPTAgent.GPTError:
            log(
                f"Error while calling GPT API",
                tag="error",
                username=user.username,
                place="StoryGenerateView.get",
            )

            return Response(
                data={"error": "GPT API call failed."},
                status=500,
            )

        except ValueError:
            log(
                f"Error of response format",
                tag="error",
                username=user.username,
                place="StoryGenerateView.get",
            )

            return Response(
                data={"error": "Please try again."},
                status=500,
            )

        except KeyError:
            log(
                f"Wrong key",
                tag="error",
                username=user.username,
                place="StoryGenerateView.get",
            )

            return Response(
                data={"error": "Please try again."},
                status=500,
            )

        except:
            log(
                f"Unknown error",
                tag="error",
                username=user.username,
                place="StoryGenerateView.get",
            )

            return Response(
                data={"error": "Please try again."},
                status=500,
            )

        log(
            f"Successfully generated story with AI",
            username=user.username,
            place="StoryGenerateView.get",
        )

        return Response(
            data={"title": title, "story": story},
            status=201,
        )


class DayCompletionView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = DayCompletionSerializer

    def post(self, request: Request) -> Response:
        body = DayCompletionSerializer(data=request.data)
        body.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        start = body.validated_data["start"]
        end = body.validated_data["end"]

        curr_time = int(datetime.now().timestamp())

        log(
            f"Current timestamp: {curr_time}",
            username=user.username,
            place="DayCompletionView.post",
        )

        if curr_time >= end:
            log(
                f"Current time exceeded intended time",
                tag="error",
                username=user.username,
                place="DayCompletionView.post",
            )

            return Response(
                data={"message": "Current time exceeded intended time"},
                status=400,
            )

        story = Story.objects.create(
            user=user,
            title="",
            content="",
            is_point_completed=True,
            created_at=datetime.fromtimestamp(curr_time),
        )
        story.save()

        log(
            f"Successfully created empty story",
            username=user.username,
            place="DayCompletionView.post",
        )

        return Response(
            data={"id": story.id, "message": "Success!"},
            status=201,
        )


class EmotionView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = EmotionCreateSerializer

    def get(self, request: Request) -> Response:
        params = EmotionQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        start_date = datetime.fromtimestamp(params.validated_data["start"])
        end_date = datetime.fromtimestamp(params.validated_data["end"])

        stories = Story.objects.filter(
            created_at__range=(start_date, end_date),
            user=user,
        ).order_by("created_at")
        serializer = self.get_serializer(stories, many=True)

        log(
            f"Successfully queried emotions (length: {len(serializer.data)})",
            username=user.username,
            place="EmotionView.get",
        )

        return Response(
            data={"emotions": serializer.data},
            status=200,
        )

    def post(self, request: Request) -> Response:
        body = EmotionCreateSerializer(data=request.data)
        body.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        emotion = body.validated_data["emotion"]

        if not emotion in Emotions:
            log(
                f"Emotion string is invalid",
                tag="error",
                username=user.username,
                place="EmotionView.post",
            )

            return Response(
                data={"message": "Provided emotion string is invalid!"},
                status=400,
            )

        story = Story.objects.filter(
            user=user,
        ).latest("created_at")

        story.emotion = emotion
        story.save()

        log(
            f"Successfully posted emotion",
            username=user.username,
            place="EmotionView.post",
        )

        return Response(
            data={"message": "Success!"},
            status=201,
        )


class ScoreView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request: Request) -> Response:
        body = ScoreCreateSerializer(data=request.data)
        body.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        score = body.validated_data["score"]
        story_id = body.validated_data["story_id"]

        if score < 1 or score > 5:
            log(
                f"Invalid score",
                tag="error",
                username=user.username,
                place="ScoreView.post",
            )

            return Response(
                data={"message": "Invalid score"},
                status=400,
            )
        # filter with user also to prevent others modifying irrelevant scores
        try:
            story = Story.objects.filter(
                id=story_id,
                user=user,
            ).latest("created_at")
        except Story.DoesNotExist:
            log(
                f"Cannot modify this score",
                tag="error",
                username=user.username,
                place="ScoreView.post",
            )

            return Response(
                data={"message": "Cannot modify this score"},
                status=400,
            )

        story.score = score
        story.is_point_completed = True
        story.save()

        log(f"Successfully saved score", username=user.username, place="ScoreView.post")

        return Response(
            data={"message": "Success!"},
            status=201,
        )


class HashtagView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = HashtagSerializer

    def get(self, request: Request) -> Response:
        params = HashtagQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        start_date = datetime.fromtimestamp(params.validated_data["start"])
        end_date = datetime.fromtimestamp(params.validated_data["end"])
        date_condition = Q(story__created_at__range=(start_date, end_date))
        user_condition = Q(story__user=user)
        hashtags = Hashtag.objects.filter((date_condition & user_condition))

        serializer = self.get_serializer(hashtags, many=True)
        serializer.data

        content_list = []
        for ser_data in serializer.data:
            content_list.append(ser_data["content"])

        ans = Counter(content_list)
        log(
            f"Successfully queried hashtags (length: {len(serializer.data)})",
            username=user.username,
            place="HashtagView.get",
        )

        return Response(
            data={"hashtags": dict(ans)},
            status=200,
        )

    def post(self, request: Request) -> Response:
        body = HashtagCreateSerializer(data=request.data)
        body.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        content = body.validated_data["content"]
        story_id = body.validated_data["story_id"]

        try:
            story = Story.objects.get(
                id=story_id,
            )
        except Story.DoesNotExist:
            log(
                f"Provided story id does not exist",
                tag="error",
                username=user.username,
                place="HashtagView.post",
            )
            return Response(
                data={"message": "Cannot update this hashtag"},
                status=400,
            )

        hashtags = [hashtag[1:] for hashtag in re.findall("#\\w+", content)]
        for hashtag in hashtags:
            curr_hashtag, _ = Hashtag.objects.get_or_create(content=hashtag)

            story.hashtags.add(curr_hashtag)
        story.save()

        log(
            f"Successfully saved hashtags",
            username=user.username,
            place="HashtagView.post",
        )

        return Response(
            data={"message": "Success!"},
            status=201,
        )
