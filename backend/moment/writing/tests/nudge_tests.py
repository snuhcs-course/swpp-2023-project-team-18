import datetime

from django.test import TestCase
from rest_framework.test import APIRequestFactory, force_authenticate

from user.models import User
from writing.models import Nudge
from writing.nudge_views import (
    NudgeView,
    NudgeMarkView,
)


class NudgeTest(TestCase):
    nudge_created_at = datetime.datetime(
        year=2023, month=10, day=25, hour=3, minute=0, second=1
    )
    nudge_end_date = datetime.datetime(
        year=2023, month=10, day=26, hour=2, minute=59, second=59
    )
    params = {
        "start": int(nudge_created_at.timestamp()) - 1,
        "end": int(nudge_end_date.timestamp()),
    }
    nudge_content = "I am a nudge"

    def setUp(self):
        User.objects.create(username="test1")

    def test_nudge_none(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        view = NudgeView.as_view()
        params = self.params

        request = factory.get("writing/nudge/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["nudge"], "")

    def test_nudge_content_none(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        nudge = Nudge.objects.create(
            user=user,
            content="",
            created_at=self.nudge_created_at,
        )
        nudge.save()

        view = NudgeView.as_view()
        params = self.params

        request = factory.get("writing/nudge/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["nudge"], "")

    def test_nudge_is_deleted(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        nudge = Nudge.objects.create(
            user=user,
            content="yayyay",
            is_deleted=True,
            created_at=self.nudge_created_at,
        )
        nudge.save()

        view = NudgeView.as_view()
        params = self.params

        request = factory.get("writing/nudge/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["nudge"], "")

    def test_nudge(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        nudge = Nudge.objects.create(
            user=user,
            content=self.nudge_content,
            is_deleted=False,
            created_at=self.nudge_created_at,
        )
        nudge.save()

        view = NudgeView.as_view()
        params = self.params

        request = factory.get("writing/nudge/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["nudge"], self.nudge_content)

    def test_update_deleted(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        nudge1 = Nudge.objects.create(
            user=user,
            content="nudge1",
            is_deleted=False,
            created_at=self.nudge_created_at,
        )
        nudge2 = Nudge.objects.create(
            user=user,
            content="nudge2",
            is_deleted=False,
            created_at=self.nudge_created_at + datetime.timedelta(days=1),
        )
        nudge1.save()
        nudge2.save()

        view = NudgeMarkView.as_view()

        request = factory.post("writing/nudge/")
        force_authenticate(request, user=user)
        response = view(request)

        nudges = Nudge.objects.filter(
            user=user,
        ).order_by("created_at")
        self.assertEqual(nudges[0].content, "nudge1")
        self.assertEqual(nudges[0].is_deleted, False)
        self.assertEqual(nudges[1].content, "nudge2")
        self.assertEqual(nudges[1].is_deleted, True)
