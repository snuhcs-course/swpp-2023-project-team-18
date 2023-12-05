from django.db import models
from user.models import User
from .constants import (
    MOMENT_MAX_LENGTH,
    REPLY_MAX_LENGTH,
    STORY_MAX_LENGTH,
    STORY_TITLE_MAX_LENGTH,
    Emotions,
)

# Create your models here.


class Hashtag(models.Model):
    content = models.CharField(max_length=50)


class Story(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)

    emotion = models.CharField(
        max_length=100,
        choices=[(e.value, e.value) for e in Emotions],
        default=Emotions.NORMAL1,
    )
    score = models.IntegerField(default=3)
    is_point_completed = models.BooleanField(default=False)
    title = models.CharField(max_length=STORY_TITLE_MAX_LENGTH)
    content = models.CharField(max_length=STORY_MAX_LENGTH)
    hashtags = models.ManyToManyField(
        Hashtag,
    )

    created_at = models.DateTimeField(auto_now_add=False)


class MomentPair(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    moment = models.CharField(max_length=MOMENT_MAX_LENGTH)
    reply = models.CharField(max_length=REPLY_MAX_LENGTH, blank=True)
    story = models.ForeignKey(Story, null=True, blank=True, on_delete=models.CASCADE)

    moment_created_at = models.DateTimeField()
    reply_created_at = models.DateTimeField()


class Nudge(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)

    summarized_story = models.CharField(max_length=1000, default="")
    content = models.CharField(max_length=1000, default="")
    is_deleted = models.BooleanField(default=False)

    created_at = models.DateTimeField(auto_created=False)
