from rest_framework.serializers import ModelSerializer
from models import MomentPair


class MomentPairSerializer(ModelSerializer):
    class Meta:
        model = MomentPair
        fields = ["id", "moment", "reply", "moment_created_at", "reply_created_at"]
