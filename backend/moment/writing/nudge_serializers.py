from rest_framework import serializers


class NudgeQuerySerializer(serializers.Serializer):
    start = serializers.IntegerField()
    end = serializers.IntegerField()
