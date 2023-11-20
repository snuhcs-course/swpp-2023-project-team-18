import datetime
from typing import List, Tuple

from user.models import User
from .constants import (
    Emotions,
    AUTO_COMPLETE_TIMEOUT,
    AUTO_COMPLETE_MAX_TRIAL,
    GPT_AUTOCOMPLETION_ERROR_TITLE,
    GPT_AUTOCOMPLETION_ERROR_CONTENT,
)
from .models import MomentPair, Story
from .utils.gpt import GPTAgent
from .utils.log import print_log
from .utils.prompt import StoryGenerateTemplate


def auto_completion_job():
    users = User.objects.all()
    now = datetime.datetime.now()
    gpt_agent = GPTAgent()
    print_log(message="starting auto completion job...", place="auto_completion_job")
    for user in users:
        print_log(message=f"processing {user.username}", place="auto_completion_job")
        last_days_moment_contents = get_last_days_moment_contents(user, now)
        last_days_story = get_last_days_story(user, now)
        if last_days_story is not None:
            print_log(
                message=f"{user.username} already finished the day",
                place="auto_completion_job",
            )
            continue
        if len(last_days_moment_contents) == 0:
            print_log(
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
                score=0,
            )
            story.save()
        else:
            ai_title, ai_story = get_ai_title_and_story_from_moments(
                last_days_moment_contents, gpt_agent
            )
            print_log(
                message=f"generated story for user {user.username}",
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
                score=0,
                emotion=Emotions.INVALID,
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

    for _ in range(AUTO_COMPLETE_MAX_TRIAL):
        try:
            parsed_answer = gpt_agent.get_parsed_answer(
                timeout=AUTO_COMPLETE_TIMEOUT,
                max_trial=1,
                required_keys=["title", "content"],
            )
            return parsed_answer["title"], parsed_answer["content"]
        except GPTAgent.GPTError as e:
            print_log(
                f"GPTError while calling GPT API; Cause={e.cause}, Received\n{e.answer}",
                tag="error",
                place="auto_completion_job",
            )
    else:
        return GPT_AUTOCOMPLETION_ERROR_TITLE, GPT_AUTOCOMPLETION_ERROR_CONTENT
