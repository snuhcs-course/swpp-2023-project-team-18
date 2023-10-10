import os
from enum import Enum


MOMENT_MAX_LENGTH = 1000
STORY_MAX_LENGTH = 10000
STORY_TITLE_MAX_LENGTH = 100


class Emotions(str, Enum):
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


OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
