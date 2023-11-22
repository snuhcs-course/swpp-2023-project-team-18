from datetime import datetime

from rest_framework import permissions
from rest_framework.generics import GenericAPIView
from rest_framework.response import Response
from rest_framework.request import Request

from .models import Nudge, User
from .nudge_serializers import NudgeQuerySerializer
from .utils.log import print_log


class NudgeView(GenericAPIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request: Request) -> Response:
        params = NudgeQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)
        user = User.objects.get(pk=request.user.id)

        start = datetime.fromtimestamp(params.validated_data["start"])
        end = datetime.fromtimestamp(params.validated_data["end"])

        nudges = Nudge.objects.filter(
            created_at__range=(start, end),
            user=user,
        )

        assert len(nudges) < 2, "Filtered nudges should be less than 2."

        nudge_content = ""
        if len(nudges) == 0 or nudges[0].content == "" or nudges[0].is_deleted == True:
            nudge_content = nudges[0].content

        print_log(
            f"Successfully queried today's nudge",
            username=user.username,
            place="NudgeView.get",
        )

        return Response(
            data={"nudge": nudge_content},
            status=200,
        )

    def post(self, request: Request) -> Response:
        user = User.objects.get(pk=request.user.id)

        nudge = Nudge.objects.filter(
            user=user,
        ).latest("created_at")

        nudge.is_deleted = True
        nudge.save()

        print_log(
            f"Successfully marked is_deleted",
            username=user.username,
            place="NudgeView.post",
        )

        return Response(
            data={"message": "Success!"},
            status=201,
        )
