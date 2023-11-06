from rest_framework import permissions
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response
from rest_framework.request import Request

from .models import Story
from .search_serializers import HashtagCompleteSerializer
from .utils import log


class HashtagCompleteView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = HashtagCompleteSerializer

    def get(self, request: Request) -> Response:
        params = HashtagCompleteSerializer(data=request.query_params)
        params.is_valid(raise_exception=True)

        tag_query = params.validated_data["tag_query"]

        stories = Story.objects.filter(user_id=request.user.id)
        tag_set = set()
        for story in stories:
            for tag in story.hashtags.all():
                if tag_query not in tag.content:
                    continue
                tag_set.add(tag.content)

        tag_list = sorted(list(tag_set), key=lambda x: len(x))

        log(
            f"Successfully queried tags for auto completion (length: {len(tag_list)})",
            username=request.user.username,
            place="HashtagCompleteView.get",
        )

        return Response(
            data={"hashtags": tag_list},
            status=200,
        )
