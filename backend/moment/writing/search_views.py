from rest_framework import permissions
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response
from rest_framework.request import Request

from .models import Hashtag
from .search_serializers import HashtagCompleteSerializer
from django.db.models import Q
from django.db.models.functions import Length


class HashtagCompleteView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = HashtagCompleteSerializer

    def get(self, request: Request) -> Response:
        params = HashtagCompleteSerializer(data=request.query_params)
        params.is_valid(raise_exception=True)

        tag_query = params.validated_data["tag_query"]

        result = Hashtag.objects.filter(Q(content__contains=tag_query)).order_by(
            Length("content").asc()
        )

        tag_list = []
        for curr_res in result:
            tag_list.append(curr_res.content)

        return Response(
            data={"hashtags": tag_list},
            status=200,
        )
