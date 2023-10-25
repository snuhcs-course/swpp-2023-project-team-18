from datetime import datetime

from django.test import TestCase
from rest_framework.test import APIRequestFactory, force_authenticate

from user.models import User
from writing.models import MomentPair, Story
from writing.views import DayCompletionView, StoryView


# Create your tests here.
class GetStoryTest(TestCase):
    timestamp1 = 1698200500
    timestamp2 = 1698200586
    content1 = "content1"
    content2 = "content2"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="otheruser", nickname="impri")
        other_user.set_password("123456")
        story1 = Story.objects.create(
            created_at=datetime.fromtimestamp(self.timestamp1),
            user=test_user,
            content=self.content1,
        )
        story2 = Story.objects.create(
            created_at=datetime.fromtimestamp(self.timestamp2),
            user=test_user,
            content=self.content2,
        )
        story1.save()
        story2.save()

    def test_get_stories_without_time(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryView.as_view()

        request = factory.post("/writing/day-completion/")
        response = view(request)
        self.assertEqual(response.status_code, 401)

    def test_get_stories_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp2}

        request = factory.get(view, params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["stories"][0]["created_at"], self.timestamp1)
        self.assertEqual(response.data["stories"][1]["created_at"], self.timestamp2)
        self.assertEqual(response.data["stories"][0]["content"], self.content1)
        self.assertEqual(response.data["stories"][1]["content"], self.content2)

    def test_get_only_one_story(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp1}

        request = factory.get(view, params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["stories"][0]["created_at"], self.timestamp1)
        self.assertEqual(len(response.data["stories"]), 1)

    def test_not_get_others_stories(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="otheruser")
        view = StoryView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp2}

        request = factory.get(view, params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(len(response.data["stories"]), 0)


class DayCompletionTest(TestCase):
    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")

    def test_day_complete_without_time(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = DayCompletionView.as_view()

        request = factory.post("/writing/day-completion/")
        force_authenticate(request, user=user)
        response = view(request)

        self.assertEqual(response.status_code, 400)

    def test_day_complete_succes(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = DayCompletionView.as_view()
        timestamp = 1698200586
        data = {"created_at": timestamp}

        request = factory.post("/writing/day-completion/", data=data)
        force_authenticate(request=request, user=user)
        response = view(request)
        created_story = Story.objects.all()[0]

        self.assertEqual(response.status_code, 201)
        self.assertEquals(created_story.created_at.timestamp(), timestamp)
