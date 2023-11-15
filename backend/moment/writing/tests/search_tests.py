import datetime

from django.test import TestCase
from rest_framework.test import APIRequestFactory, force_authenticate

from user.models import User
from writing.models import Story, MomentPair
from writing.models import Hashtag
from writing.search_views import (
    HashtagCompleteView,
    HashtagSearchView,
    ContentSearchView,
)
from writing.constants import SearchFields


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

    def test_hashtag_completion_korean_spread(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        view = HashtagCompleteView.as_view()
        params = {"tag_query": "아ㅇ"}

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


class HashtagSearchTest(TestCase):
    def setUp(self):
        user1 = User.objects.create(username="test1")
        user2 = User.objects.create(username="test2")

        hashtag_1_1 = Hashtag.objects.create(content="우왕")
        hashtag_1_2 = Hashtag.objects.create(content="우와아아")
        hashtag_1_3 = Hashtag.objects.create(content="우아아")

        hashtag_2_1 = Hashtag.objects.create(content="우아아아")
        hashtag_all = Hashtag.objects.create(content="와우")
        now = datetime.datetime.now()

        story1 = Story.objects.create(
            created_at=now - datetime.timedelta(days=1),
            user=user1,
            content="yay",
            is_point_completed=True,
        )
        story1.hashtags.add(hashtag_1_1)
        story1.hashtags.add(hashtag_1_2)
        story1.hashtags.add(hashtag_1_3)
        story1.hashtags.add(hashtag_all)
        story2 = Story.objects.create(
            created_at=now,
            user=user1,
            content="yay2",
            is_point_completed=True,
        )
        story2.hashtags.add(hashtag_2_1)
        story2.hashtags.add(hashtag_all)
        other_story = Story.objects.create(
            created_at=now,
            user=user2,
            content="yay3",
            is_point_completed=True,
        )
        other_story.hashtags.add(hashtag_all)

    def test_hashtag_search(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        view = HashtagSearchView.as_view()
        params = {"query": "와우"}

        request = factory.get("writing/hashtags/search/", params)
        force_authenticate(request, user=user)
        response = view(request)
        print(response.data)
        entries = response.data["searchentries"]
        self.assertEqual(len(entries), 2)
        self.assertEqual(entries[0]["content"], "yay2")
        self.assertEqual(entries[1]["content"], "yay")

    def test_hashtag_one(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        view = HashtagSearchView.as_view()
        params = {"query": "우왕"}

        request = factory.get("writing/search/hashtags/", params)
        force_authenticate(request, user=user)
        response = view(request)
        entries = response.data["searchentries"]
        self.assertEqual(len(entries), 1)
        self.assertEqual(entries[0]["content"], "yay")

    def test_hashtag_none(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        view = HashtagSearchView.as_view()
        params = {"query": "우"}

        request = factory.get("writing/search/hashtags/", params)
        force_authenticate(request, user=user)
        response = view(request)
        entries = response.data["searchentries"]
        self.assertEqual(len(entries), 0)


class ContentSearchTest(TestCase):
    def setUp(self):
        user1 = User.objects.create(username="test1")
        user2 = User.objects.create(username="test2")

        day_start = datetime.datetime(
            year=2023, month=11, day=3, hour=3, minute=0, second=0
        )
        day_end = datetime.datetime(
            year=2023, month=11, day=4, hour=2, minute=59, second=59
        )
        story1 = Story.objects.create(
            created_at=day_end,
            user=user1,
            content="검색내용",
            is_point_completed=True,
        )

        story2 = Story.objects.create(
            created_at=day_end + datetime.timedelta(days=1),
            user=user1,
            content="what",
            title="검색제목",
            is_point_completed=True,
        )
        story3 = Story.objects.create(
            created_at=day_end + datetime.timedelta(days=2),
            user=user1,
            content="what",
            title="검색모먼트",
            is_point_completed=True,
        )
        other_story = Story.objects.create(
            created_at=day_end,
            user=user2,
            content="검색",
            is_point_completed=True,
        )
        moment1 = MomentPair.objects.create(
            moment_created_at=day_start,
            reply_created_at=day_start,
            moment="검색",
            reply="검색",
            user=user1,
        )
        moment2 = MomentPair.objects.create(
            moment_created_at=day_start + datetime.timedelta(days=1),
            reply_created_at=day_start,
            moment="what",
            reply="검색",
            user=user1,
        )
        moment3 = MomentPair.objects.create(
            moment_created_at=day_start + datetime.timedelta(days=2),
            reply_created_at=day_start,
            moment="검색모먼트",
            reply="검색",
            user=user1,
        )
        other_moment = MomentPair.objects.create(
            moment_created_at=day_start + datetime.timedelta(days=2),
            reply_created_at=day_start,
            moment="검색",
            reply="검색",
            user=user2,
        )

    def test_content_search(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="test1")
        view = ContentSearchView.as_view()
        params = {"query": "검ㅅ"}

        request = factory.get("writing/search/contents/", params)
        force_authenticate(request, user=user)
        response = view(request)
        entries = response.data["searchentries"]
        self.assertEqual(len(entries), 3)
        self.assertEqual(entries[0]["content"], "검색모먼트")
        self.assertEqual(entries[0]["field"], SearchFields.MOMENT)
        self.assertEqual(
            entries[0]["created_at"],
            datetime.datetime(
                year=2023, month=11, day=6, hour=2, minute=59, second=59
            ).timestamp(),
        )
        self.assertEqual(entries[0]["title"], "검색모먼트")
        self.assertEqual(entries[1]["content"], "what")
        self.assertEqual(entries[1]["field"], SearchFields.TITLE)
        self.assertEqual(entries[1]["title"], "검색제목")
        self.assertEqual(entries[2]["content"], "검색내용")
        self.assertEqual(entries[2]["field"], SearchFields.STORY)
