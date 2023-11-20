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
    GPT_NUDGE_GENERATE_NONE_CONTENT,
)
from writing.cron import auto_completion_job
from writing.nudge_cron import (
    nudge_creation_job,
    summarize_yesterday_nudge,
    should_create_nudge,
    generate_nudge,
)
from writing.models import MomentPair, Story, Nudge
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


class NudgeGenerateTest(TestCase):
    def setUp(self):
        self.test_user = User.objects.create(username="user1", nickname="user1")
        self.other_user = User.objects.create(username="other", nickname="other")

    @patch(
        "writing.utils.gpt.GPTAgent.get_answer",
        return_value="summary",
    )
    def test_summarize_yesterday(self, mock_get):
        yesterday_story = Story.objects.create(
            user=self.test_user,
            created_at=intended_gmt - datetime.timedelta(seconds=2),
            title="title",
            content="story",
        )
        yesterday_nudge = Nudge.objects.create(
            user=self.test_user,
            summarized_story="yay",
            content="nudge",
            created_at=intended_gmt - datetime.timedelta(seconds=2),
        )
        yesterday_story.save()
        yesterday_nudge.save()
        summarize_yesterday_nudge(
            self.test_user, intended_gmt + datetime.timedelta(seconds=1)
        )
        yesterday_nudge = Nudge.objects.get(user=self.test_user)
        self.assertEqual(yesterday_nudge.summarized_story, "summary")

    def test_should_create_nudge_false(self):
        self.assertEqual(should_create_nudge(["yay1", "yay2", ""]), False)
        self.assertEqual(should_create_nudge(["", "", "yay"]), False)
        self.assertEqual(should_create_nudge(["", "yay", ""]), False)
        self.assertEqual(should_create_nudge(["yay", "", ""]), False)
        self.assertEqual(should_create_nudge(["", "", ""]), False)

        self.assertEqual(should_create_nudge(["yay1", "yay2"]), False)
        self.assertEqual(should_create_nudge(["yay1"]), False)
        self.assertEqual(should_create_nudge([]), False)

    def test_should_create_nudge_true(self):
        self.assertEqual(should_create_nudge(["yay1", "", "yay2"]), True)
        self.assertEqual(should_create_nudge(["", "yay1", "yay2"]), True)
        self.assertEqual(should_create_nudge(["yay1", "yay2", "yay3"]), True)

    @freeze_time(lambda: intended_gmt + datetime.timedelta(seconds=1))
    def test_generate_nudge_not_should_create(self):
        yesterday_nudge = Nudge.objects.create(
            user=self.test_user,
            summarized_story="yay1",
            content="nudge1",
            created_at=intended_gmt - datetime.timedelta(seconds=2),
        )
        yesterday_nudge.save()
        generate_nudge(self.test_user, intended_gmt + datetime.timedelta(seconds=1))
        now = datetime.datetime.now()
        created_nudge = Nudge.objects.get(
            created_at=datetime.datetime(
                year=now.year,
                month=now.month,
                day=now.day,
                hour=18,
                minute=0,
                second=1,
            )
        )
        self.assertEqual(created_nudge.content, GPT_NUDGE_GENERATE_NONE_CONTENT)

    @freeze_time(lambda: intended_gmt + datetime.timedelta(seconds=1))
    @patch(
        "writing.utils.gpt.GPTAgent.get_answer",
        return_value="nudge",
    )
    def test_generate_nudge(self, mock_get):
        yesterday_nudge = Nudge.objects.create(
            user=self.test_user,
            summarized_story="yay1",
            content="nudge1",
            created_at=intended_gmt - datetime.timedelta(seconds=2),
        )
        two_nudge = Nudge.objects.create(
            user=self.test_user,
            summarized_story="yay2",
            content="nudge2",
            created_at=intended_gmt
            - datetime.timedelta(days=1)
            - datetime.timedelta(seconds=2),
        )
        three_nudge = Nudge.objects.create(
            user=self.test_user,
            summarized_story="yay3",
            content="nudge3",
            created_at=intended_gmt
            - datetime.timedelta(days=2)
            - datetime.timedelta(seconds=2),
        )
        yesterday_nudge.save()
        two_nudge.save()
        three_nudge.save()
        generate_nudge(self.test_user, intended_gmt + datetime.timedelta(seconds=1))
        now = datetime.datetime.now()
        created_nudge = Nudge.objects.get(
            created_at=datetime.datetime(
                year=now.year,
                month=now.month,
                day=now.day,
                hour=18,
                minute=0,
                second=1,
            )
        )
        self.assertEqual(created_nudge.content, "nudge")
