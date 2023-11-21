import os
from enum import Enum, EnumMeta


MOMENT_MAX_LENGTH = 100
REPLY_MAX_LENGTH = 1000
STORY_MAX_LENGTH = 1000
STORY_TITLE_MAX_LENGTH = 20


class MetaEnum(EnumMeta):
    def __contains__(cls, item):
        try:
            cls(item)
        except ValueError:
            return False
        return True


class BaseEnum(Enum, metaclass=MetaEnum):
    pass


class Emotions(str, BaseEnum):
    EXCITED1 = "excited1"
    EXCITED2 = "excited2"
    HAPPY1 = "happy1"
    HAPPY2 = "happy2"
    NORMAL1 = "normal1"
    NORMAL2 = "normal2"
    SAD1 = "sad1"
    SAD2 = "sad2"
    ANGRY1 = "angry1"
    ANGRY2 = "angry2"
    INVALID = "invalid"


class SearchFields(int, BaseEnum):
    TITLE = 0
    MOMENT = 1
    STORY = 2


OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

NUDGE_SUMMARY_MAX_TRIAL = 5
NUDGE_SUMMARY_TIMEOUT = 30
NUDGE_GENERATE_MAX_TRIAL = 5
NUDGE_GENERATE_TIMEOUT = 30
AUTO_COMPLETE_MAX_TRIAL = 5
AUTO_COMPLETE_TIMEOUT = 30
MOMENT_REPLY_MAX_TRIAL = 2
MOMENT_REPLY_TIMEOUT = 15
STORY_GENERATION_MAX_TRIAL = 2
STORY_GENERATION_TIMEOUT = 20

GPT_AUTOCOMPLETION_ERROR_TITLE = ""
GPT_AUTOCOMPLETION_ERROR_CONTENT = "마무리하는 과정에서 문제가 발생했어요"
GPT_NUDGE_GENERATE_ERROR_CONTENT = ""
GPT_NUDGE_GENERATE_NONE_CONTENT = ""
GPT_NUDGE_SUMMARY_ERROR_CONTENT = ""
