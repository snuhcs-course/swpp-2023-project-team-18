import datetime

from django.test import TestCase
from rest_framework.test import APIRequestFactory, force_authenticate

from user.models import User
from writing.models import Story
from writing.models import Hashtag
from writing.search_views import HashtagCompleteView


class HashtagCompleteTest(TestCase):
    def setUp(self):
        user1 = User.objects.create(username="test1")
        user2 = User.objects.create(username="test2")
        hashtag_1_1 = Hashtag.objects.create(content="우왕")
        hashtag_1_2 = Hashtag.objects.create(content="우와아아")
        hashtag_1_3 = Hashtag.objects.create(content="우아아")
        hashtag_2_1 = Hashtag.objects.create(content="우아아아")

        story1 = Story.objects.create(
            created_at=datetime.datetime.now(),
            user=user1,
            content="yay",
            is_point_completed=True,
        )
        story1.hashtags.add(hashtag_1_1)
        story1.hashtags.add(hashtag_1_2)
        story1.hashtags.add(hashtag_1_3)
        story2 = Story.objects.create(
            created_at=datetime.datetime.now(),
            user=user2,
            content="yay2",
            is_point_completed=True,
        )
        story2.hashtags.add(hashtag_2_1)

    def test_hashtag_completion(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        view = HashtagCompleteView.as_view()
        params = {"tag_query": "아아"}

        request = factory.get("writing/hashtags/complete/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(len(response.data["hashtags"]), 2)
        self.assertEqual(response.data["hashtags"][0], "우아아")
        self.assertEqual(response.data["hashtags"][1], "우와아아")

    def test_hashtag_empty(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        view = HashtagCompleteView.as_view()
        params = {"tag_query": "아아아"}

        request = factory.get("writing/hashtags/complete/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(len(response.data["hashtags"]), 0)
