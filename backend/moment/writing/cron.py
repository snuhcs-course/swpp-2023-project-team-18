import datetime
from typing import List, Tuple

from .models import MomentPair, Story
from user.models import User
from .constants import Emotions
from .utils.gpt import GPTAgent
from .utils.prompt import StoryGenerateTemplate
from .utils.log import log


def auto_completion_job():
    users = User.objects.all()
    now = datetime.datetime.now()
    gpt_agent = GPTAgent()
    log(message="starting auto completion job...", place="auto_completion_job")
    for user in users:
        log(message=f"processing {user.username}", place="auto_completion_job")
        last_days_moment_contents = get_last_days_moment_contents(user, now)
        last_days_story = get_last_days_story(user, now)
        if last_days_story != None:
            log(
                message=f"{user.username} already finished the day",
                place="auto_completion_job",
            )
            continue
        if len(last_days_moment_contents) == 0:
            log(
                message=f"{user.username} hasn't written any moments",
                place="auto_completion_job",
            )

            story = Story.objects.create(
                user=user,
                emotion=Emotions.INVALID,
                created_at=datetime.datetime(
                    year=now.year,
                    month=now.month,
                    day=now.day,
                    hour=17,
                    minute=59,
                    second=59,
                ),
            )
            story.save()
        else:
            ai_title, ai_story = get_ai_title_and_story_from_moments(
                last_days_moment_contents, gpt_agent
            )
            log(
                message="generated story for user {user.username}",
                place="auto_completion_job",
            )
            story = Story.objects.create(
                user=user,
                title=ai_title,
                content=ai_story,
                created_at=datetime.datetime(
                    year=now.year,
                    month=now.month,
                    day=now.day,
                    hour=17,
                    minute=59,
                    second=59,
                ),
            )
            story.save()


def get_last_days_moment_contents(user: User, now: datetime.datetime) -> List[str]:
    last_day_end = datetime.datetime(
        year=now.year, month=now.month, day=now.day, hour=17, minute=59, second=59
    )
    last_day_start = (
        last_day_end - datetime.timedelta(days=1) + datetime.timedelta(seconds=1)
    )
    moments = MomentPair.objects.filter(
        moment_created_at__range=(last_day_start, last_day_end),
        user=user,
    ).order_by("moment_created_at")
    return [moment.moment for moment in moments]


def get_last_days_story(user: User, now: datetime.datetime) -> Story:
    last_day_end = datetime.datetime(
        year=now.year, month=now.month, day=now.day, hour=17, minute=59, second=59
    )
    last_day_start = (
        last_day_end - datetime.timedelta(days=1) + datetime.timedelta(seconds=1)
    )
    try:
        return Story.objects.filter(
            created_at__range=(last_day_start, last_day_end), user=user
        )[0]
    except:
        return None


def get_ai_title_and_story_from_moments(
    moment_contents: List[str], gpt_agent: GPTAgent
) -> Tuple[str, str]:
    gpt_agent.reset_messages()
    gpt_agent.add_message(
        StoryGenerateTemplate.get_prompt(moments=";".join(moment_contents))
    )
    try:
        title_and_story = gpt_agent.get_answer(
            timeout=30, max_trial=5
        )  # TODO: need more testing
        title, story = title_and_story.split(";")
        return title, story
    except GPTAgent.GPTError:
        log("Error while calling GPT API", place="auto_completion_job")
        return ("", "마무리하는 과정에서 문제가 발생했어요")
    except ValueError:
        log("Invalid format", place="auto_completion_job")
        return ("", "마무리하는 과정에서 문제가 발생했어요")
