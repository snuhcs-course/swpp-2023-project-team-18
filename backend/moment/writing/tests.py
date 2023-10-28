import datetime
from unittest.mock import patch

from django.test import TestCase
from rest_framework.test import APIRequestFactory, force_authenticate
from freezegun import freeze_time

from user.models import User
from writing.models import MomentPair, Story, Hashtag
from writing.utils.gpt import GPTAgent
from writing.utils.prompt import StoryGenerateTemplate
from writing.views import (
    DayCompletionView,
    StoryView,
    StoryGenerateView,
    ScoreView,
    EmotionView,
    HashtagView,
)
from writing.cron import auto_completion_job
from writing.constants import Emotions

# Create your tests here.
intended_day = datetime.datetime(
    year=2023, month=10, day=25, hour=3, minute=0, second=0
)  # class field not usable in decorators
ai_sample_title = "ai_sample_title"
ai_sample_story = "ai_sample_story"


class AutoCompletionTest(TestCase):
    def setUp(self):
        self.test_user = User.objects.create(username="user1", nickname="user1")
        self.other_user = User.objects.create(username="other", nickname="other")

    @freeze_time(lambda: intended_day + datetime.timedelta(days=1, seconds=1))
    def test_already_completed_user(self):
        story = Story.objects.create(
            user=self.test_user, created_at=intended_day, emotion=Emotions.HAPPY1
        )
        story.save()
        auto_completion_job()
        self.assertEqual(
            Story.objects.get(user=self.test_user).emotion, Emotions.HAPPY1
        )
        self.assertEqual(
            intended_day.timestamp(),
            Story.objects.get(user=self.test_user).created_at.timestamp(),
        )

    @patch("writing.utils.gpt.GPTAgent.get_answer", return_value="")
    @freeze_time(lambda: intended_day + datetime.timedelta(days=1, seconds=1))
    def test_completion_without_moments(self, mock_get):
        previous_moment = MomentPair.objects.create(
            user=self.test_user,
            moment_created_at=intended_day - datetime.timedelta(seconds=1),
            reply_created_at=intended_day - datetime.timedelta(seconds=1),
            moment="moment",
        )
        other_moment = MomentPair.objects.create(
            user=self.other_user,
            moment_created_at=intended_day,
            reply_created_at=intended_day,
            moment="moment",
        )
        previous_moment.save()
        other_moment.save()
        auto_completion_job()
        created_story = Story.objects.get(user=self.test_user)
        self.assertEqual(created_story.emotion, Emotions.INVALID)
        self.assertEqual(
            created_story.created_at.timestamp(),
            (datetime.datetime.now() + datetime.timedelta(hours=15,seconds=-2)).timestamp(),
        )

    @freeze_time(lambda: intended_day + datetime.timedelta(days=1, seconds=1))
    @patch(
        "writing.utils.gpt.GPTAgent.get_answer",
        return_value=f"{ai_sample_title};{ai_sample_story}",
    )
    @patch(
        "writing.utils.gpt.GPTAgent.add_message",
    )
    def test_completion_with_moments_without_story(self, mock_add, mock_get):
        content1 = "moment1"
        content2 = "moment2"
        moment1 = MomentPair.objects.create(
            user=self.test_user,
            moment_created_at=intended_day,
            reply_created_at=intended_day,
            moment=content1,
        )
        moment2 = MomentPair.objects.create(
            user=self.test_user,
            moment_created_at=intended_day + datetime.timedelta(hours=21),
            reply_created_at=intended_day + datetime.timedelta(hours=21),
            moment=content2,
        )
        other_moment = MomentPair.objects.create(
            user=self.other_user,
            moment_created_at=intended_day,
            reply_created_at=intended_day,
            moment=content1,
        )
        moment1.save()
        moment2.save()
        other_moment.save()

        auto_completion_job()
        created_story = Story.objects.get(user=self.test_user)
        self.assertEqual(created_story.emotion, Emotions.NORMAL1)
        self.assertEqual(created_story.title, ai_sample_title)
        self.assertEqual(created_story.content, ai_sample_story)
        mock_add.assert_any_call(
            StoryGenerateTemplate.get_prompt(moments=f"{content1};{content2}")
        )


class GetHashtagTest(TestCase):
    hashtag1 = "h2"
    hashtag2 = "h1"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="other_user", nickname="other_user")
        other_user.set_password("123456")
        story = Story.objects.create(created_at=intended_day, user=test_user)
        self.story_pk = story.pk
        hashtag1 = Hashtag(content=self.hashtag1)
        hashtag1.save()
        hashtag2 = Hashtag(content=self.hashtag2)
        hashtag2.save()
        story.hashtags.add(hashtag1)
        story.hashtags.add(hashtag2)
        story.save()

    def test_get_hashtag_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = HashtagView.as_view()

        params = {"story_id": self.story_pk}
        request = factory.get("writing/hashtags/", params)
        force_authenticate(request, user)
        response = view(request)

        self.assertEquals(response.status_code, 200)
        self.assertEqual(response.data["hashtags"][0]["content"], self.hashtag1)
        self.assertEqual(response.data["hashtags"][1]["content"], self.hashtag2)

    """  def test_get_hashtag_wrong_story(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = HashtagView.as_view()

        params = {"story_id": self.story_pk + 1}
        request = factory.get("writing/hashtags/", params)
        force_authenticate(request, user)
        response = view(request)
        self.assertEqual(response.status_code, 400)

    def test_get_hashtag_other_user(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="other_user")
        view = HashtagView.as_view() 

        params = {"story_id": self.story_pk}
        request = factory.get("writing/hashtags/", params)
        force_authenticate(request, user)
        response = view(request)
        self.assertEqual(response.status_code, 400)"""


class SaveHashtagTest(TestCase):
    hashtag_string = "# h2#h1 ##"
    hashtag1 = "h2"
    hashtag2 = "h1"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        story = Story.objects.create(created_at=intended_day, user=test_user)
        self.story_pk = story.pk
        story.save()

    def test_post_hashtag_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = HashtagView.as_view()
        data = {"story_id": self.story_pk, "content": self.hashtag_string}
        request = factory.post("writing/hashtags/", data=data)
        force_authenticate(request, user=user)
        response = view(request)
        added_hashtags = Story.objects.get(pk=self.story_pk).hashtags.all()
        self.assertEqual(response.status_code, 201)
        self.assertEqual(len(added_hashtags), 2)
        self.assertEqual(added_hashtags[0].content, self.hashtag1)
        self.assertEqual(added_hashtags[1].content, self.hashtag2)

    def test_post_hashtag_wrong_story_id(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = EmotionView.as_view()

        data = {"story_id": self.story_pk, "content": self.hashtag_string}
        request = factory.post("writing/hashtags/", data=data)
        force_authenticate(request, user=user)
        response = view(request)
        added_hashtags = Story.objects.get(pk=self.story_pk).hashtags.all()
        self.assertEqual(response.status_code, 400)
        self.assertEqual(len(added_hashtags), 0)


class GetEmotionTest(TestCase):
    emotion1 = "happy1"
    emotion2 = "sad2"
    emotion3 = "angry1"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="otheruser", nickname="impri")
        other_user.set_password("123456")
        story3 = Story.objects.create(
            created_at=intended_day,
            user=test_user,
            emotion=self.emotion3,
        )
        story1 = Story.objects.create(
            created_at=intended_day - datetime.timedelta(days=2),
            user=test_user,
            emotion=self.emotion1,
        )
        story2 = Story.objects.create(
            created_at=intended_day - datetime.timedelta(days=1),
            user=test_user,
            emotion=self.emotion2,
        )

        story3.save()
        story1.save()
        story2.save()

    def test_get_emotions_without_time(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = EmotionView.as_view()

        request = factory.get("writing/emotions/")
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.status_code, 400)

    def test_get_stories_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = EmotionView.as_view()
        params = {
            "start": (intended_day - datetime.timedelta(days=2)).timestamp(),
            "end": intended_day.timestamp(),
        }

        request = factory.get("writing/emotions/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["emotions"][0]["emotion"], self.emotion1)
        self.assertEqual(response.data["emotions"][1]["emotion"], self.emotion2)
        self.assertEqual(response.data["emotions"][2]["emotion"], self.emotion3)

    def test_get_only_one_story(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = EmotionView.as_view()
        params = {"start": intended_day.timestamp(), "end": intended_day.timestamp()}

        request = factory.get("writing/emotions/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["emotions"][0]["emotion"], self.emotion3)
        self.assertEqual(len(response.data["emotions"]), 1)

    def test_not_get_others_stories(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="otheruser")
        view = EmotionView.as_view()
        params = {
            "start": (intended_day - datetime.timedelta(days=2)).timestamp(),
            "end": intended_day.timestamp(),
        }
        request = factory.get("writing/emotions/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(len(response.data["emotions"]), 0)


class SaveEmotionTest(TestCase):
    default_emotion = "normal1"
    new_emotion = "happy1"
    wrong_emotion = "something"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="otheruser", nickname="otheruser")
        other_user.set_password("123456")
        story1 = Story.objects.create(user=test_user, created_at=intended_day)
        self.story1_pk = story1.pk
        story2 = Story.objects.create(
            user=test_user, created_at=intended_day - datetime.timedelta(days=1)
        )
        self.story2_pk = story2.pk
        other_user_story = Story.objects.create(
            user=other_user, created_at=intended_day + datetime.timedelta(hours=21)
        )
        story1.save()
        story2.save()
        other_user_story.save()

    def test_save_emotion_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = EmotionView.as_view()

        data = {"emotion": self.new_emotion}
        request = factory.post("/writing/emotions/", data=data)
        force_authenticate(request, user)
        response = view(request)

        self.assertEqual(response.status_code, 201)
        self.assertEqual(Story.objects.get(pk=self.story1_pk).emotion, self.new_emotion)
        self.assertEqual(
            Story.objects.get(pk=self.story2_pk).emotion, self.default_emotion
        )

    def test_save_wrong_emotion_fail(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = ScoreView.as_view()

        data = {"emotion": self.wrong_emotion}
        request = factory.post("/writing/emotions/", data=data)
        force_authenticate(request, user)
        response = view(request)

        self.assertEqual(response.status_code, 400)
        self.assertEqual(
            Story.objects.get(pk=self.story1_pk).emotion, self.default_emotion
        )


class SaveScoreTest(TestCase):
    default_score = 3
    new_score = 5
    wrong_score = 10

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="otheruser", nickname="otheruser")
        other_user.set_password("123456")
        story1 = Story.objects.create(user=test_user, created_at=intended_day)
        self.story1_pk = story1.pk
        story2 = Story.objects.create(
            user=test_user, created_at=intended_day - datetime.timedelta(days=1)
        )
        self.story2_pk = story2.pk
        story1.save()
        story2.save()

    def test_save_score_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = ScoreView.as_view()

        data = {"story_id": self.story1_pk, "score": self.new_score}
        request = factory.post("/writing/score/", data=data)
        force_authenticate(request, user)
        response = view(request)

        self.assertEqual(response.status_code, 201)
        self.assertEqual(Story.objects.get(pk=self.story1_pk).score, self.new_score)
        self.assertEqual(Story.objects.get(pk=self.story1_pk).is_point_completed, True)
        self.assertEqual(Story.objects.get(pk=self.story2_pk).score, self.default_score)

    def test_save_others_score_fail(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="otheruser")
        view = ScoreView.as_view()

        data = {"story_id": self.story1_pk, "score": self.new_score}
        request = factory.post("/writing/score/", data=data)
        force_authenticate(request, user)
        response = view(request)

        self.assertEqual(response.status_code, 400)
        self.assertEqual(Story.objects.get(pk=self.story1_pk).score, self.default_score)

    def test_save_wrong_score_fail(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = ScoreView.as_view()

        data = {"story_id": self.story1_pk, "score": self.wrong_score}
        request = factory.post("/writing/score/", data=data)
        force_authenticate(request, user)
        response = view(request)

        self.assertEqual(response.status_code, 400)
        self.assertEqual(Story.objects.get(pk=self.story1_pk).score, self.default_score)


class StoryGenerateTest(TestCase):
    moment1_content = "moment1"
    moment2_content = "moment2"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="otheruser", nickname="otheruser")
        other_user.set_password("123456")

        moment1 = MomentPair.objects.create(
            user=test_user,
            moment=self.moment1_content,
            moment_created_at=intended_day + datetime.timedelta(hours=12),
            reply_created_at=intended_day,
        )
        moment2 = MomentPair.objects.create(
            user=test_user,
            moment=self.moment2_content,
            moment_created_at=intended_day,
            reply_created_at=intended_day,
        )
        previous_moment = MomentPair.objects.create(
            user=test_user,
            moment="moment3",
            moment_created_at=intended_day - datetime.timedelta(hours=1),
            reply_created_at=intended_day,
        )
        other_user_moment = MomentPair.objects.create(
            user=other_user,
            moment="moment4",
            moment_created_at=intended_day,
            reply_created_at=intended_day,
        )
        story = Story.objects.create(
            user=test_user, created_at=intended_day + datetime.timedelta(hours=21)
        )
        moment1.save()
        moment2.save()
        previous_moment.save()
        other_user_moment.save()

    @patch(
        "writing.utils.gpt.GPTAgent.get_answer",
        return_value=f"{ai_sample_title};{ai_sample_story}",
    )
    @patch("writing.utils.gpt.GPTAgent.add_message")
    def test_story_generate_success(self, mock_add, mock_get):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryGenerateView.as_view()

        request = factory.get("/writing/ai-story/")
        force_authenticate(request, user)
        response = view(request)

        mock_add.assert_called_with(
            StoryGenerateTemplate.get_prompt(
                moments=f"{self.moment2_content};{self.moment1_content}"
            )
        )
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.data["title"], ai_sample_title)
        self.assertEqual(response.data["story"], ai_sample_story)

    @patch("writing.utils.gpt.GPTAgent.get_answer", return_value=ai_sample_title)
    def test_story_generate_wrong_format(self, mock_get):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryGenerateView.as_view()

        request = factory.get("/writing/ai-story/")
        force_authenticate(request, user)
        response = view(request)

        self.assertEqual(response.status_code, 500)

    @patch("writing.utils.gpt.GPTAgent.get_answer", side_effect=GPTAgent.GPTError())
    def test_story_generate_api_fail(self, mock_get):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryGenerateView.as_view()

        request = factory.get("/writing/ai-story/")
        force_authenticate(request, user)
        response = view(request)

        self.assertEqual(response.status_code, 500)


class SaveStoryTest(TestCase):
    title = "title"
    content = "content"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        story1 = Story.objects.create(user=test_user, created_at=intended_day)
        story2 = Story.objects.create(
            user=test_user, created_at=intended_day - datetime.timedelta(days=1)
        )
        story1.save()
        story2.save()

    def test_save_story_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryView.as_view()
        data = {
            "title": self.title,
            "content": self.content,
        }

        request = factory.post("writing/stories/", data=data)
        force_authenticate(request, user=user)
        response = view(request)
        modified_story = Story.objects.get(created_at=intended_day)

        self.assertEqual(modified_story.title, self.title)
        self.assertEqual(modified_story.content, self.content)
        self.assertEqual(response.status_code, 201)


class GetStoryTest(TestCase):
    timestamp1 = 1698200500
    timestamp2 = 1698200586
    content1 = "content1"
    content2 = "content2"
    hashtag1 = "h2"
    hashtag2 = "h1"
    hashtag3 = "h3"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="otheruser", nickname="impri")
        other_user.set_password("123456")
        test_user_hashtag1 = Hashtag.objects.create(content=self.hashtag1)
        test_user_hashtag2 = Hashtag.objects.create(content=self.hashtag2)
        test_user_hashtag1.save()
        test_user_hashtag2.save()
        story1 = Story.objects.create(
            created_at=datetime.datetime.fromtimestamp(self.timestamp1),
            user=test_user,
            content=self.content1,
        )
        story2 = Story.objects.create(
            created_at=datetime.datetime.fromtimestamp(self.timestamp2),
            user=test_user,
            content=self.content2,
            is_point_completed=True,
        )
        story1.hashtags.add(test_user_hashtag1)
        story1.hashtags.add(test_user_hashtag2)
        story1.save()
        story2.save()

    def test_get_stories_without_time(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryView.as_view()

        request = factory.get("writing/stories/")
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.status_code, 400)

    def test_get_stories_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp2}

        request = factory.get("writing/stories/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["stories"][0]["created_at"], self.timestamp1)
        self.assertEqual(response.data["stories"][1]["created_at"], self.timestamp2)
        self.assertEqual(response.data["stories"][0]["content"], self.content1)
        self.assertEqual(response.data["stories"][1]["content"], self.content2)
        self.assertEqual(response.data["stories"][0]["is_point_completed"], False)
        story1_hashtags = response.data["stories"][0]["hashtags"]
        self.assertEqual(len(story1_hashtags), 2)
        self.assertEqual(story1_hashtags[0]["content"], self.hashtag1)
        self.assertEqual(story1_hashtags[1]["content"], self.hashtag2)
        self.assertEqual(response.data["stories"][1]["is_point_completed"], True)

        story2_hashtags = response.data["stories"][1]["hashtags"]
        self.assertEqual(len(story2_hashtags), 0)

    def test_get_only_one_story(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = StoryView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp1}

        request = factory.get("writing/stories/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.data["stories"][0]["created_at"], self.timestamp1)

        story1_hashtags = response.data["stories"][0]["hashtags"]
        self.assertEqual(len(story1_hashtags), 2)
        self.assertEqual(story1_hashtags[0]["content"], self.hashtag1)
        self.assertEqual(story1_hashtags[1]["content"], self.hashtag2)

    def test_not_get_others_stories(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="otheruser")
        view = StoryView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp2}

        request = factory.get("writing/stories/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(len(response.data["stories"]), 0)


class DayCompletionTest(TestCase):
    intended_day = datetime.datetime(
        year=2023, month=10, day=25, hour=3, minute=0, second=0
    )

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")

    def test_day_complete_without_time(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = DayCompletionView.as_view()

        request = factory.post("writing/day-completion/")
        force_authenticate(request, user=user)
        response = view(request)

        self.assertEqual(response.status_code, 400)

    @freeze_time(lambda: intended_day)
    def test_day_complete_success_at_start_of_day(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = DayCompletionView.as_view()
        data = {
            "start": DayCompletionTest.intended_day.timestamp(),
            "end": (
                DayCompletionTest.intended_day + datetime.timedelta(days=1)
            ).timestamp(),
        }

        request = factory.post("writing/day-completion/", data=data)
        force_authenticate(request=request, user=user)
        response = view(request)
        created_story = Story.objects.all()[0]

        self.assertEqual(response.status_code, 201)
        self.assertEquals(
            created_story.created_at.timestamp(),
            DayCompletionTest.intended_day.timestamp(),
        )

    @freeze_time(lambda: intended_day + datetime.timedelta(days=1))
    def test_day_complete_fail_after_end_of_day(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = DayCompletionView.as_view()
        data = {
            "start": DayCompletionTest.intended_day.timestamp(),
            "end": (
                DayCompletionTest.intended_day + datetime.timedelta(days=1)
            ).timestamp(),
        }

        request = factory.post("writing/day-completion/", data=data)
        force_authenticate(request=request, user=user)
        response = view(request)
        self.assertEqual(response.status_code, 400)
