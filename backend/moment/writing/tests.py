from datetime import datetime

from django.test import TestCase
from rest_framework.test import APIRequestFactory, force_authenticate

from user.models import User
from writing.models import MomentPair, Story
from writing.views import DayCompletionView


# Create your tests here.
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
