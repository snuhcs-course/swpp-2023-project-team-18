import datetime
import json
from unittest.mock import patch

from django.test import TestCase
from freezegun import freeze_time

from user.models import User
from writing.constants import (
    Emotions,
    GPT_AUTOCOMPLETION_ERROR_CONTENT,
    GPT_AUTOCOMPLETION_ERROR_TITLE,
)
from writing.cron import auto_completion_job
from writing.models import MomentPair, Story
from writing.utils.prompt import StoryGenerateTemplate

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
        self.assertEqual(created_story.score, 0)
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
        self.assertEqual(created_story.emotion, Emotions.INVALID)
        self.assertEqual(created_story.score, 0)
        self.assertEqual(created_story.title, ai_sample_title)
        self.assertEqual(created_story.content, ai_sample_story)
        mock_add.assert_any_call(
            StoryGenerateTemplate.get_prompt(moments=f"{content1};{content2}")
        )

    @freeze_time(lambda: intended_gmt + datetime.timedelta(days=1, seconds=1))
    @patch(
        "writing.utils.gpt.GPTAgent.get_answer",
        return_value=json.dumps(
            {
                "title": ai_sample_title,
            }
        ),
    )
    @patch(
        "writing.utils.gpt.GPTAgent.add_message",
    )
    def test_completion_with_moments_format_failure(self, mock_add, mock_get):
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
        self.assertEqual(created_story.emotion, Emotions.INVALID)
        self.assertEqual(created_story.score, 0)
        self.assertEqual(created_story.title, GPT_AUTOCOMPLETION_ERROR_TITLE)
        self.assertEqual(created_story.content, GPT_AUTOCOMPLETION_ERROR_CONTENT)
        mock_add.assert_any_call(
            StoryGenerateTemplate.get_prompt(moments=f"{content1};{content2}")
        )
