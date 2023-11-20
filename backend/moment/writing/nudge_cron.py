import datetime
from typing import List

from user.models import User
from .constants import (
    NUDGE_SUMMARY_MAX_TRIAL,
    NUDGE_SUMMARY_TIMEOUT,
    GPT_NUDGE_SUMMARY_ERROR_CONTENT,
    GPT_NUDGE_GENERATE_ERROR_CONTENT,
    NUDGE_GENERATE_MAX_TRIAL,
    NUDGE_GENERATE_TIMEOUT,
    GPT_NUDGE_GENERATE_NONE_CONTENT,
)
from .models import Story, Nudge
from .utils.gpt import GPTAgent
from .utils.log import print_log
from .utils.prompt import NudgeGenerateStepOneTemplate, NUDGE_GENERATE_STEP_TWO


def nudge_creation_job():
    users = User.objects.all()
    now = datetime.datetime.now()

    print_log(message="starting nudge creation job...", place="nudge_creation_job")
    for user in users:
        print_log(message=f"Processing {user.username}", place="nudge_creation_job")
        summarize_yesterday_nudge(user, now)
        generate_nudge(user, now)


def summarize_yesterday_nudge(user: User, now: datetime.datetime):
    yesterday_end = datetime.datetime(
        year=now.year, month=now.month, day=now.day, hour=17, minute=59, second=59
    )
    yesterday_start = (
        yesterday_end - datetime.timedelta(days=1) + datetime.timedelta(seconds=1)
    )
    # Should be only one object filtered
    yesterday_nudge = Nudge.objects.filter(
        created_at__range=(yesterday_start, yesterday_end),
        user=user,
    )[0]

    yesterday_story = Story.objects.filter(
        created_at__range=(yesterday_start, yesterday_end),
        user=user,
    )[0]

    gpt_agent = GPTAgent()
    gpt_agent.reset_messages()
    gpt_agent.add_message(
        NudgeGenerateStepOneTemplate.get_prompt(diary=yesterday_story.content)
    )

    try:
        answer = gpt_agent.get_answer(
            timeout=NUDGE_SUMMARY_TIMEOUT,
            temperature=0.7,
            max_trial=NUDGE_SUMMARY_MAX_TRIAL,
        )
    except GPTAgent.GPTError as e:
        print_log(
            f"GPTError while calling GPT API; Cause={e.cause}, Received\n{e.answer}",
            tag="error",
            place="nudge_creation_job - summarize",
        )
        answer = GPT_NUDGE_SUMMARY_ERROR_CONTENT

    yesterday_nudge.summarized_story = answer
    yesterday_nudge.save()


# Summarized stories are sorted by created_at
def should_create_nudge(summ_stories: List[str]) -> bool:
    summ_stories_formatted = [
        summ_story.replace("*", "").replace("\n", "").replace(" ", "").strip()
        for summ_story in summ_stories
    ]
    if len(summ_stories_formatted) < 3:
        return False
    if summ_stories_formatted[2] == "":
        return False
    elif summ_stories_formatted[1] == "" and summ_stories_formatted[0] == "":
        return False
    else:
        return True


def format_summ_stories(summ_stories: List[str]) -> str:
    results = []
    for summ_story in summ_stories:
        summ_story = summ_story.replace("* ", "").replace("\n", " ").strip()
        results.append(summ_story)
    return "\n---\n".join(results)


def generate_nudge(user: User, now: datetime.datetime):
    yesterday_end = datetime.datetime(
        year=now.year, month=now.month, day=now.day, hour=17, minute=59, second=59
    )
    three_before_start = datetime.datetime(
        yesterday_end - datetime.timedelta(days=3) + datetime.timedelta(seconds=1)
    )

    prev_nudges = Nudge.objects.filter(
        created_at__range=(three_before_start, yesterday_end),
        user=user,
    ).order_by("created_at")

    summ_stories = [prev_nudge.summarized_story for prev_nudge in prev_nudges]

    if should_create_nudge(summ_stories):
        gpt_agent = GPTAgent(model="ft:gpt-3.5-turbo-1106:snu-swpp-2023f::8LVIBzaa")
        gpt_agent.reset_messages()
        gpt_agent.add_message(content=NUDGE_GENERATE_STEP_TWO, role="system")
        gpt_agent.add_message(format_summ_stories(summ_stories))

        try:
            answer = gpt_agent.get_answer(
                timeout=NUDGE_GENERATE_TIMEOUT,
                temperature=0.3,
                max_trial=NUDGE_GENERATE_MAX_TRIAL,
            )
        except GPTAgent.GPTError as e:
            print_log(
                f"GPTError while calling GPT API; Cause={e.cause}, Received\n{e.answer}",
                tag="error",
                place="nudge_creation_job - create",
            )
            answer = GPT_NUDGE_GENERATE_ERROR_CONTENT
    else:
        answer = GPT_NUDGE_GENERATE_NONE_CONTENT

    nudge = Nudge.objects.create(
        user=user,
        content=answer,
        created_at=datetime.datetime(
            year=now.year,
            month=now.month,
            day=now.day,
            hour=18,
            minute=0,
            second=1,
        ),
    )
    nudge.save()
