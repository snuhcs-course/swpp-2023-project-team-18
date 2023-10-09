from django.db import models
from moment.user.models import User
from constants import EMOTIONS_CHOICES

# Create your models here.
class Story(models.Model):
    user_id = models.ForeignKey(User, on_delete=models.CASCADE)
    created_at = models.DateTimeField(auto_created=True)
    emotion = models.CharField(
        max_length=100, choices=EMOTIONS_CHOICES, default="normal1"
    )
    score = models.IntegerField(default=3)
    is_point_completed = models.BooleanField(default=False)
    title = models.CharField(max_length=100)
    content = models.CharField(max_length=1000)


class MomentPair(models.Model):
    user_id = models.ForeignKey(User, on_delete=models.CASCADE)
    moment = models.CharField(max_length=1000)
    reply = models.CharField(max_length=1000, blank=True)
    moment_created_at = models.DateTimeField()
    reply_created_at = models.DateTimeField()
    story_id = models.ForeignKey(Story, null=True)
