from datetime import datetime

from rest_framework import permissions
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response
from rest_framework.request import Request

from user.models import User
from .models import MomentPair, Story
from .serializers import (
    MomentPairQuerySerializer,
    MomentPairSerializer,
    MomentPairCreateSerializer,
    DayCompletionSerializer,
    StorySerializer,
    StoryQuerySerializer,
    StoryCreateSerializer,
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
            log(f"Error while calling GPT API", tag="error", place="MomentView.post")

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

        log(f"Successfully created moment", place="MomentView.post")

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

        start_date = datetime.fromtimestamp(body.validated_data["start"])
        end_date = datetime.fromtimestamp(body.validated_data["end"])
        content = body.validated_data["content"]

        # `stories` should only contain one element.
        stories = (
            Story.objects.filter(
                created_at__range=(start_date, end_date),
                user=user,
            )
            .order_by("created_at")
            .update(content=content)
        )

        return Response(
            data={"message": "Success!"},
            status=201,
        )


class StoryGenerateView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    # serializer_class = StoryCreateSerializer

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.gpt_agent = GPTAgent()

    def get(self, request: Request) -> Response:
        params = StoryQuerySerializer(data=request.query_params)

        params.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        start_date = datetime.fromtimestamp(params.validated_data["start"])
        end_date = datetime.fromtimestamp(params.validated_data["end"])

        moment_pairs = MomentPair.objects.filter(
            moment_created_at__range=(start_date, end_date),
            user=user,
        ).order_by("moment_created_at")

        moment_contents = [moment_pair.moment for moment_pair in moment_pairs]

        log(f"Moments: {moment_contents}", place="StoryGenerateView.get")

        self.gpt_agent.reset_messages()
        prompt = StoryGenerateTemplate.get_prompt(moments=";".join(moment_contents))
        self.gpt_agent.add_message(prompt)

        log(f"Prompt: {prompt}", place="StoryGenerateView.get")

        try:
            story = self.gpt_agent.get_answer(timeout=15, max_trial=2)

        except GPTAgent.GPTError:
            log(f"Error while calling GPT API", tag="error", place="MomentView.post")

            return Response(
                data={"error": "GPT API call failed"},
                status=500,
            )

        return Response(
            data={"story": story},
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
            f"{curr_time}",
            place="DayCompletionView.post",
        )

        if curr_time >= end:
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

        return Response(
            data={"message": "Success!"},
            status=201,
        )
