from rest_framework import serializers


class NudgeQuerySerializer(serializers.ModelSerializer):
    start = serializers.IntegerField()
    end = serializers.IntegerField()
