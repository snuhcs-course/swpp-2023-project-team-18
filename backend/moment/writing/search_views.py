import datetime
from typing import Tuple, Optional

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
from .utils.search import process_query
from .constants import SearchFields


class HashtagCompleteView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = HashtagCompleteSerializer

    def get(self, request: Request) -> Response:
        params = HashtagCompleteSerializer(data=request.query_params)
        params.is_valid(raise_exception=True)

        tag_query = process_query(params.validated_data["tag_query"])

        stories = Story.objects.filter(user_id=request.user.id)
        tag_set = set()
        for story in stories:
            for tag in story.hashtags.all():
                content = process_query(tag.content)
                if tag_query not in content:
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

    def get(self, request: Request) -> Response:
        params = SearchQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)

        query = process_query(params.validated_data["query"])
        results = []
        moments = MomentPair.objects.filter(user_id=request.user.id).order_by(
            "-moment_created_at"
        )

        stories = Story.objects.filter(user_id=request.user.id).order_by("-created_at")
        for story in stories:
            # 1순위: story content
            if query in process_query(story.content):
                results.append(
                    ContentSearchSerializer(
                        story,
                        context={"content": story.content, "field": SearchFields.STORY},
                    ).data
                )
            # 2순위: moment content
            elif (
                moment := ContentSearchView._search_moments(
                    moments, query, *ContentSearchView._day_start_end(story.created_at)
                )
            ) is not None:
                results.append(
                    ContentSearchSerializer(
                        story, context={"content": moment, "field": SearchFields.MOMENT}
                    ).data
                )
            # 3순위: story title
            elif query in process_query(story.title):
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

    @staticmethod
    def _day_start_end(
        time: datetime.datetime,
    ) -> Tuple[datetime.datetime, datetime.datetime]:
        HOUR_DIFF = 9
        local_time = time + datetime.timedelta(hours=HOUR_DIFF)
        if local_time.hour < 3:
            local_time = local_time - datetime.timedelta(days=1)
        local_start_time = local_time.replace(hour=3, minute=0, second=0)
        local_end_time = (
            local_start_time
            + datetime.timedelta(days=1)
            - datetime.timedelta(seconds=1)
        )
        return local_start_time - datetime.timedelta(
            hours=HOUR_DIFF
        ), local_end_time - datetime.timedelta(hours=HOUR_DIFF)

    @staticmethod
    def _search_moments(
        moments,
        query: str,
        start_time: datetime.datetime,
        end_time: datetime.datetime,
    ) -> Optional[str]:
        query = process_query(query)
        for moment in moments:
            if (
                start_time <= moment.moment_created_at <= end_time
                and query in process_query(moment.moment)
            ):
                return moment.moment
        return None
