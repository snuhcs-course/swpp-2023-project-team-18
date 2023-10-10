from rest_framework import serializers

from .models import MomentPair
from .constants import MOMENT_MAX_LENGTH


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
