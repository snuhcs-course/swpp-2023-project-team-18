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
from .utils import call_gpt, GPTError, MomentReplyThrottle


class MomentView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = MomentPairSerializer

    # Override
    def get_throttles(self):
        if self.request.method.lower() == "post":
            self.throttle_scope = "moment-reply"

        return super(MomentView, self).get_throttles()

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
            data={"moments": serializer.data},
            status=200,
        )

    def post(self, request):
        body = MomentPairCreateSerializer(data=request.data)
        body.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        try:
            reply = call_gpt(body.data["moment"], timeout=5)  # TODO: 프롬프팅 처리 하기
        except GPTError:
            for throttle in self.get_throttles():
                history = throttle.cache.get(throttle.get_cache_key(request, self), [])

                throttle.cache.set(
                    throttle.get_cache_key(request, self),
                    history[1:],
                )
            return Response(
                data={"error": "GPT3 API call failed"},
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

        serializer = self.get_serializer(moment_pair)

        return Response(
            data={"moment": serializer.data},
            status=201,
        )
