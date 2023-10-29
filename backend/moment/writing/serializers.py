from rest_framework import serializers

from .models import MomentPair, Story, Hashtag
from .constants import MOMENT_MAX_LENGTH, STORY_MAX_LENGTH, STORY_TITLE_MAX_LENGTH


# Serializer for moments
class MomentPairSerializer(serializers.ModelSerializer):
    class Meta:
        model = MomentPair
        fields = ["id", "moment", "reply", "moment_created_at", "reply_created_at"]

    def to_representation(self, instance: MomentPair):
        res = super().to_representation(instance)
        res["moment_created_at"] = int(instance.moment_created_at.timestamp())
        res["reply_created_at"] = int(instance.reply_created_at.timestamp())
        return res


class MomentPairQuerySerializer(serializers.Serializer):
    start = serializers.IntegerField()
    end = serializers.IntegerField()


class MomentPairCreateSerializer(serializers.Serializer):
    moment = serializers.CharField(max_length=MOMENT_MAX_LENGTH)


# serializer for hashtag
class HashtagSerializer(serializers.ModelSerializer):
    class Meta:
        model = Hashtag
        fields = ["id", "content"]


class HashtagQuerySerializer(serializers.Serializer):
    story_id = serializers.IntegerField()


class HashtagCreateSerializer(serializers.Serializer):
    story_id = serializers.IntegerField()
    content = serializers.CharField()


# Serializer for stories
class StorySerializer(serializers.ModelSerializer):
    hashtags = HashtagSerializer(many=True, read_only=True)

    class Meta:
        model = Story
        fields = [
            "id",
            "emotion",
            "score",
            "title",
            "content",
            "created_at",
            "hashtags",
            "is_point_completed",
        ]

    def to_representation(self, instance: Story):
        res = super().to_representation(instance)
        res["created_at"] = int(instance.created_at.timestamp())
        return res


class StoryQuerySerializer(serializers.Serializer):
    start = serializers.IntegerField()
    end = serializers.IntegerField()


class StoryCreateSerializer(serializers.Serializer):
    title = serializers.CharField(max_length=STORY_TITLE_MAX_LENGTH)
    content = serializers.CharField(max_length=STORY_MAX_LENGTH)


class StoryDeleteSerializer(serializers.Serializer):
    story_id = serializers.IntegerField()


class DayCompletionSerializer(serializers.Serializer):
    start = serializers.IntegerField()
    end = serializers.IntegerField()


class EmotionQuerySerializer(serializers.Serializer):
    start = serializers.IntegerField()
    end = serializers.IntegerField()


class EmotionCreateSerializer(serializers.Serializer):
    emotion = serializers.CharField()


class ScoreCreateSerializer(serializers.Serializer):
    story_id = serializers.IntegerField()
    score = serializers.IntegerField()
