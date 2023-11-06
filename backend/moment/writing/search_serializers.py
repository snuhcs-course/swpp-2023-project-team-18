from rest_framework import serializers


class HashtagCompleteSerializer(serializers.Serializer):
    tag_query = serializers.CharField()
