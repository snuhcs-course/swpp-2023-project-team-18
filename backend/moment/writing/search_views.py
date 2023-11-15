import datetime
from typing import Tuple

from rest_framework import permissions
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response
from rest_framework.request import Request

from .models import MomentPair, Story
from .search_serializers import (
    HashtagCompleteSerializer,
    HashtagSearchSerializer,
    SearchQuerySerializer,
    ContentSearchSerializer,
)
from .utils.log import print_log
from .utils.search import spread_korean
from .constants import SearchFields


class HashtagCompleteView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = HashtagCompleteSerializer

    def get(self, request: Request) -> Response:
        params = HashtagCompleteSerializer(data=request.query_params)
        params.is_valid(raise_exception=True)

        tag_query = spread_korean(params.validated_data["tag_query"])

        stories = Story.objects.filter(user_id=request.user.id)
        tag_set = set()
        for story in stories:
            for tag in story.hashtags.all():
                if tag_query not in spread_korean(tag.content):
                    continue
                tag_set.add(tag.content)

        tag_list = sorted(list(tag_set), key=lambda x: len(x))

        print_log(
            f"Successfully queried tags for auto completion (length: {len(tag_list)})",
            username=request.user.username,
            place="HashtagCompleteView.get",
        )

        return Response(
            data={"hashtags": tag_list},
            status=200,
        )


class HashtagSearchView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request: Request):
        params = SearchQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)

        query = params.validated_data["query"]
        stories = (
            Story.objects.prefetch_related("hashtags")
            .filter(user_id=request.user.id)
            .filter(hashtags__content__exact=query)
            .order_by("-created_at")
        )
        print_log(
            f"Successfully searched by hashtag (length: {len(stories)})",
            username=request.user.username,
            place="HashtagSearchView.get",
        )

        return Response(
            data={"searchentries": HashtagSearchSerializer(stories, many=True).data},
            status=200,
        )


class ContentSearchView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def day_start_end(
        self, time: datetime.datetime
    ) -> Tuple[datetime.datetime, datetime.datetime]:
        if time.hour < 3:
            time = time - datetime.timedelta(days=1)
        start_time = time.replace(hour=3, minute=0, second=0)
        end_time = (
            start_time + datetime.timedelta(days=1) - datetime.timedelta(seconds=1)
        )
        return (start_time, end_time)

    def search_moments(self, moments, query, start_time, end_time) -> str:
        for moment in moments:
            if (
                start_time <= moment.moment_created_at
                and moment.moment_created_at <= end_time
            ):
                if query in spread_korean(moment.moment):
                    return moment.moment
        return None

    def get(self, request: Request) -> Response:
        params = SearchQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)

        query = spread_korean(params.validated_data["query"])
        results = []
        moments = MomentPair.objects.filter(user_id=request.user.id).order_by(
            "-moment_created_at"
        )

        stories = Story.objects.filter(user_id=request.user.id).order_by("-created_at")
        for story in stories:
            if query in spread_korean(story.content):
                results.append(
                    ContentSearchSerializer(
                        story,
                        context={"content": story.content, "field": SearchFields.STORY},
                    ).data
                )
            elif (
                moment := self.search_moments(
                    moments, query, *self.day_start_end(story.created_at)
                )
            ) is not None:
                results.append(
                    ContentSearchSerializer(
                        story, context={"content": moment, "field": SearchFields.MOMENT}
                    ).data
                )
            elif query in spread_korean(story.title):
                results.append(
                    ContentSearchSerializer(
                        story,
                        context={"content": story.content, "field": SearchFields.TITLE},
                    ).data
                )
            print_log(
                f"Successfully searched by contents (length: {len(results)})",
                username=request.user.username,
                place="ContentSearchView.get",
            )
        return Response({"searchentries": results}, status=200)
