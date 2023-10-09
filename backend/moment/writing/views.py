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


# Create your views here.
class MomentView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = MomentPairSerializer

    def get(self, request):
        params = MomentPairQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)
        user = request.user

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
        user = request.user

        moment_pair = MomentPair.objects.create(
            user=user,
            moment=body.data["moment"],
            reply="",  # FIXME: 나중에 붙이기
            story=None,
            moment_created_at=datetime.now(),
            reply_created_at=datetime.now(),
        )
        moment_pair.save()

        serializer = self.get_serializer(moment_pair)

        return Response(
            {"moment": serializer.data},
        )
