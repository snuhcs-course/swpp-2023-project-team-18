from rest_framework.serializers import ModelSerializer
from rest_framework.serializers import IntegerField
from .models import MomentPair


class MomentPairSerializer(ModelSerializer):
    class Meta:
        model = MomentPair
        fields = ["id", "moment", "reply", "moment_created_at", "reply_created_at"]

    def to_representation(self, instance: MomentPair):
        res = super().to_representation(instance)
        res["moment_created_at"] = int(instance.moment_created_at.timestamp())
        res["reply_created_at"] = int(instance.reply_created_at.timestamp())
        return res
