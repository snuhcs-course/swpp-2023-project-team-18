from django.db import models
from user.models import User
from .constants import EMOTIONS_CHOICES

# Create your models here.


class Hashtag(models.Model):
    content = models.CharField(max_length=50)
    created_at = models.DateTimeField(auto_created=True)


class Story(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)

    emotion = models.CharField(
        max_length=100, choices=EMOTIONS_CHOICES, default="normal1"
    )
    score = models.IntegerField(default=3)
    is_point_completed = models.BooleanField(default=False)
    title = models.CharField(max_length=100)
    content = models.CharField(max_length=1000)
    hashtags = models.ManyToManyField(
        Hashtag,
    )

    created_at = models.DateTimeField(auto_created=True)


class MomentPair(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    moment = models.CharField(max_length=1000)
    reply = models.CharField(max_length=1000, blank=True)
    story = models.ForeignKey(Story, null=True, on_delete=models.CASCADE)

    moment_created_at = models.DateTimeField()
    reply_created_at = models.DateTimeField()


class Nudge(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    date = models.DateField()
    content = models.CharField(max_length=1000)
    is_completed = models.BooleanField(default=False)

    created_at = models.DateTimeField(auto_created=True)
