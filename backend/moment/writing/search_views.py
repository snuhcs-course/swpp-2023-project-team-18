from rest_framework import permissions
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response
from rest_framework.request import Request

from .search_serializers import HashtagCompleteSerializer


class HashtagCompleteView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = HashtagCompleteSerializer
