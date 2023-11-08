import datetime, json
from unittest.mock import patch

from django.test import TestCase
from django.core.cache import cache
from rest_framework.test import APIRequestFactory, force_authenticate
from freezegun import freeze_time

from user.models import User
from writing.models import MomentPair, Story, Hashtag
from writing.utils.gpt import GPTAgent
from writing.utils.prompt import StoryGenerateTemplate, MomentReplyTemplate
from writing.views import (
    MomentView,
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
intended_gmt = datetime.datetime(
    year=2023, month=10, day=25, hour=18, minute=0, second=0
)
ai_sample_moment = "ai_sample_moment"
ai_sample_title = "ai_sample_title"
ai_sample_story = "ai_sample_story"


class SaveMomentTest(TestCase):
    content = "moment"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")

    @freeze_time(lambda: intended_day)
    @patch("writing.utils.gpt.GPTAgent.get_answer", return_value=f"{ai_sample_moment}")
    @patch(
        "writing.utils.gpt.GPTAgent.add_message",
    )
    def test_save_moment_success(self, mock_add, mock_get):
        cache.clear()  # to avoid throttling issues
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = MomentView.as_view()
        data = {"moment": self.content}

        request = factory.post("writing/moments/", data=data)
        force_authenticate(request, user)
        response = view(request)

        new_moment = MomentPair.objects.get(user=user)
        self.assertEqual(response.status_code, 201)
        self.assertEqual(new_moment.moment, self.content)
        self.assertEqual(new_moment.reply, ai_sample_moment)
        self.assertEqual(
            new_moment.moment_created_at.timestamp(), intended_day.timestamp()
        )
        self.assertEqual(
            new_moment.moment_created_at.timestamp(), intended_day.timestamp()
        )
        mock_add.assert_called_with(MomentReplyTemplate.get_prompt(moment=self.content))
        cache.clear()

    @patch("writing.utils.gpt.GPTAgent.get_answer", side_effect=GPTAgent.GPTError())
    def test_save_moment_api_fail(self, mock_get):
        cache.clear()
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = MomentView.as_view()
        data = {"moment": self.content}

        request = factory.post("writing/moments/", data=data)
        force_authenticate(request, user)
        response = view(request)

        self.assertEqual(len(MomentPair.objects.filter(user=user)), 0)
        self.assertEqual(response.status_code, 500)

    @patch("writing.utils.gpt.GPTAgent.get_answer", return_value=f"{ai_sample_moment}")
    def test_save_moment_throttle_fail(self, mock_get):
        cache.clear()
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = MomentView.as_view()
        data = {"moment": self.content}

        request = factory.post("writing/moments/", data=data)
        force_authenticate(request, user)
        with freeze_time(lambda: intended_day):
            view(request)
        with freeze_time(lambda: intended_day + datetime.timedelta(minutes=30)):
            view(request)
        with freeze_time(
            lambda: intended_day + datetime.timedelta(minutes=59, seconds=59)
        ):
            response = view(request)
        self.assertEqual(response.status_code, 429)
        self.assertEqual(len(MomentPair.objects.filter(user=user)), 2)

    @patch("writing.utils.gpt.GPTAgent.get_answer", return_value=f"{ai_sample_moment}")
    def test_save_moment_throttle_enabled_after_hour(self, mock_get):
        cache.clear()
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = MomentView.as_view()
        data = {"moment": self.content}

        request = factory.post("writing/moments/", data=data)
        force_authenticate(request, user)
        with freeze_time(lambda: intended_day):
            view(request)
        with freeze_time(lambda: intended_day):
            view(request)
        with freeze_time(lambda: intended_day + datetime.timedelta(hours=1)):
            response = view(request)
        self.assertEqual(response.status_code, 201)
        self.assertEqual(len(MomentPair.objects.filter(user=user)), 3)
        cache.clear()


class GetMomentTest(TestCase):
    timestamp1 = 1698200500
    timestamp2 = 1698200586
    content1 = "content1"
    content2 = "content2"
    reply1 = "reply1"
    reply2 = "reply2"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="otheruser", nickname="impri")
        other_user.set_password("123456")

        moment1 = MomentPair.objects.create(
            moment_created_at=datetime.datetime.fromtimestamp(self.timestamp1),
            reply_created_at=datetime.datetime.fromtimestamp(self.timestamp1),
            user=test_user,
            moment=self.content1,
            reply=self.reply1,
        )
        moment2 = MomentPair.objects.create(
            moment_created_at=datetime.datetime.fromtimestamp(self.timestamp2),
            reply_created_at=datetime.datetime.fromtimestamp(self.timestamp2),
            user=test_user,
            moment=self.content2,
            reply=self.reply2,
        )
        moment1.save()
        moment2.save()

    def test_get_moments_without_time(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = MomentView.as_view()

        request = factory.get("writing/moments/")
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.status_code, 400)

    def test_get_moments_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = MomentView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp2}

        request = factory.get("writing/moments/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(
            response.data["moments"][0]["moment_created_at"], self.timestamp1
        )
        self.assertEqual(
            response.data["moments"][1]["moment_created_at"], self.timestamp2
        )
        self.assertEqual(
            response.data["moments"][0]["reply_created_at"], self.timestamp1
        )
        self.assertEqual(
            response.data["moments"][1]["reply_created_at"], self.timestamp2
        )
        self.assertEqual(response.data["moments"][0]["moment"], self.content1)
        self.assertEqual(response.data["moments"][1]["moment"], self.content2)
        self.assertEqual(response.data["moments"][0]["reply"], self.reply1)
        self.assertEqual(response.data["moments"][1]["reply"], self.reply2)

    def test_get_only_one_story(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = MomentView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp1}

        request = factory.get("writing/moments/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(
            response.data["moments"][0]["moment_created_at"], self.timestamp1
        )
        self.assertEqual(len(response.data["moments"]), 1)

    def test_not_get_others_stories(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="otheruser")
        view = MomentView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp2}

        request = factory.get("writing/stories/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(len(response.data["moments"]), 0)


class AutoCompletionTest(TestCase):
    def setUp(self):
        self.test_user = User.objects.create(username="user1", nickname="user1")
        self.other_user = User.objects.create(username="other", nickname="other")

    @freeze_time(lambda: intended_gmt + datetime.timedelta(days=1, seconds=1))
    def test_already_completed_user(self):
        story = Story.objects.create(
            user=self.test_user, created_at=intended_gmt, emotion=Emotions.HAPPY1
        )
        story.save()
        auto_completion_job()
        self.assertEqual(
            Story.objects.get(user=self.test_user).emotion, Emotions.HAPPY1
        )
        self.assertEqual(
            intended_gmt.timestamp(),
            Story.objects.get(user=self.test_user).created_at.timestamp(),
        )

    @patch(
        "writing.utils.gpt.GPTAgent.get_answer",
        return_value=json.dumps(
            {
                "title": "",
                "content": "",
            }
        ),
    )
    @freeze_time(lambda: intended_gmt + datetime.timedelta(days=1, seconds=1))
    def test_completion_without_moments(self, mock_get):
        previous_moment = MomentPair.objects.create(
            user=self.test_user,
            moment_created_at=intended_gmt - datetime.timedelta(seconds=1),
            reply_created_at=intended_gmt - datetime.timedelta(seconds=1),
            moment="moment",
        )
        other_moment = MomentPair.objects.create(
            user=self.other_user,
            moment_created_at=intended_gmt,
            reply_created_at=intended_gmt,
            moment="moment",
        )
        previous_moment.save()
        other_moment.save()
        auto_completion_job()
        created_story = Story.objects.get(user=self.test_user)
        self.assertEqual(created_story.emotion, Emotions.INVALID)
        self.assertEqual(
            created_story.created_at.timestamp(),
            (datetime.datetime.now() - datetime.timedelta(seconds=2)).timestamp(),
        )

    @freeze_time(lambda: intended_gmt + datetime.timedelta(days=1, seconds=1))
    @patch(
        "writing.utils.gpt.GPTAgent.get_answer",
        return_value=json.dumps(
            {
                "title": ai_sample_title,
                "content": ai_sample_story,
            }
        ),
    )
    @patch(
        "writing.utils.gpt.GPTAgent.add_message",
    )
    def test_completion_with_moments_without_story(self, mock_add, mock_get):
        content1 = "moment1"
        content2 = "moment2"
        moment1 = MomentPair.objects.create(
            user=self.test_user,
            moment_created_at=intended_gmt,
            reply_created_at=intended_gmt,
            moment=content1,
        )
        moment2 = MomentPair.objects.create(
            user=self.test_user,
            moment_created_at=intended_gmt + datetime.timedelta(hours=21),
            reply_created_at=intended_gmt + datetime.timedelta(hours=21),
            moment=content2,
        )
        other_moment = MomentPair.objects.create(
            user=self.other_user,
            moment_created_at=intended_gmt,
            reply_created_at=intended_gmt,
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
    timestamp1 = 1698200500
    timestamp2 = 1698200586
    content1 = "content1"
    content2 = "content2"
    hashtag1 = "h2"
    hashtag2 = "h1"
    hashtag3 = "h3"
    other_hashtag = "h4"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        other_user = User.objects.create(username="otheruser", nickname="impri")
        other_user.set_password("123456")
        test_user_hashtag1 = Hashtag.objects.create(content=self.hashtag1)
        test_user_hashtag2 = Hashtag.objects.create(content=self.hashtag2)
        test_user_hashtag3 = Hashtag.objects.create(content=self.hashtag3)
        other_user_hashtag = Hashtag.objects.create(content=self.other_hashtag)
        test_user_hashtag1.save()
        test_user_hashtag2.save()
        test_user_hashtag3.save()
        other_user_hashtag.save()
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
        other_story = Story.objects.create(
            created_at=datetime.datetime.fromtimestamp(self.timestamp1),
            user=other_user,
            content=self.content1,
        )
        story1.hashtags.add(test_user_hashtag1)
        story1.hashtags.add(test_user_hashtag2)
        story2.hashtags.add(test_user_hashtag2)
        story2.hashtags.add(test_user_hashtag3)
        other_story.hashtags.add(test_user_hashtag1)
        other_story.hashtags.add(other_user_hashtag)
        story1.save()
        story2.save()
        other_story.save()

    def test_get_hashtags_without_time(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = HashtagView.as_view()

        request = factory.get("writing/hashtags/")
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.status_code, 400)

    def test_get_hashtags_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = HashtagView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp2}

        request = factory.get("writing/hashtags/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(len(response.data["hashtags"].keys()), 3)
        self.assertEqual(response.data["hashtags"][self.hashtag1], 1)
        self.assertEqual(response.data["hashtags"][self.hashtag2], 2)
        self.assertEqual(response.data["hashtags"][self.hashtag3], 1)

    def test_get_only_one_story_hashtags(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = HashtagView.as_view()
        params = {"start": self.timestamp1, "end": self.timestamp1}

        request = factory.get("writing/hashtags/", params)
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(len(response.data["hashtags"].keys()), 2)
        self.assertEqual(response.data["hashtags"][self.hashtag1], 1)
        self.assertEqual(response.data["hashtags"][self.hashtag2], 1)


class SaveHashtagTest(TestCase):
    hashtag_string = "#h2 dsjfl##h1 ## # 23"
    hashtag_string2 = "#h1#h1#h3"
    hashtag1 = "h2"
    hashtag2 = "h1"
    hashtag3 = "h3"

    def setUp(self):
        test_user = User.objects.create(username="impri", nickname="impri")
        test_user.set_password("123456")
        story = Story.objects.create(created_at=intended_day, user=test_user)
        story2 = Story.objects.create(
            created_at=intended_day + datetime.timedelta(days=1), user=test_user
        )
        self.story_pk = story.pk
        self.story2_pk = story2.pk
        story.save()
        story2.save()

    def test_post_hashtag_success(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="impri")
        view = HashtagView.as_view()
        data = {"story_id": self.story_pk, "content": self.hashtag_string}
        request = factory.post("writing/hashtags/", data=data)
        force_authenticate(request, user=user)
        response = view(request)
        data = {"story_id": self.story2_pk, "content": self.hashtag_string2}
        request = factory.post("writing/hashtags/", data=data)
        force_authenticate(request, user=user)
        response = view(request)
        added_hashtags1 = Story.objects.get(pk=self.story_pk).hashtags.all()
        added_hashtags2 = Story.objects.get(pk=self.story2_pk).hashtags.all()
        self.assertEqual(response.status_code, 201)
        self.assertEqual(len(added_hashtags1), 2)
        self.assertEqual(added_hashtags1[0].content, self.hashtag1)
        self.assertEqual(added_hashtags1[1].content, self.hashtag2)
        self.assertEqual(len(added_hashtags2), 2)
        self.assertEqual(added_hashtags2[0].content, self.hashtag2)
        self.assertEqual(added_hashtags2[1].content, self.hashtag3)
        self.assertEqual(len(Hashtag.objects.all()), 3)

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
        return_value=json.dumps(
            {
                "title": ai_sample_title,
                "content": ai_sample_story,
            }
        ),
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


class DeleteStoryTest(TestCase):
    def setUp(self):
        test_user = User.objects.create(username="ay_test", nickname="ay_test")
        test_user.set_password("123456")
        story = Story.objects.create(user=test_user, created_at=intended_day)
        story.save()
        self.story_id = story.id

    def test_delete_story(self):
        factory = APIRequestFactory()
        user = User.objects.get(username="ay_test")
        view = StoryView.as_view()
        data = {"story_id": self.story_id}

        request = factory.delete("writing/stories/", data=data)
        force_authenticate(request, user=user)
        response = view(request)

        self.assertEqual(response.status_code, 204)


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
