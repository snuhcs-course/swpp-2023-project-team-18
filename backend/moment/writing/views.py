from datetime import datetime

from rest_framework import permissions
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response

from .models import MomentPair
from .serializers import (
    MomentPairQuerySerializer,
    MomentPairSerializer,
    MomentPairCreateSerializer,
)
from user.models import User


# Create your views here.
class MomentView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = MomentPairSerializer

    def get(self, request):
        params = MomentPairQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        start_date = datetime.fromtimestamp(params.validated_data["start"])
        end_date = datetime.fromtimestamp(params.validated_data["end"])

        moment_pairs = MomentPair.objects.filter(
            moment_created_at__range=(start_date, end_date),
            user=user,
        )
        serializer = self.get_serializer(moment_pairs, many=True)

        return Response(
            {"moments": serializer.data},
        )

    def post(self, request):
        body = MomentPairCreateSerializer(data=request.data)
        body.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        # TODO: openai api 써서 reply 받아오기
        #   만약 실패하면 그냥 빈 문자열로 저장하나? 아니면 moment 저장 자체를 취소하나?

        moment_pair = MomentPair.objects.create(
            user=user,
            moment=body.data["moment"],
            reply="",  # TODO: 나중에 붙이기
            story=None,
            moment_created_at=datetime.now(),
            reply_created_at=datetime.now(),
        )
        moment_pair.save()

        serializer = self.get_serializer(moment_pair)

        return Response(
            {"moment": serializer.data},
        )
