from rest_framework import serializers

from .models import Hashtag


class HashtagCompleteSerializer(serializers.Serializer):
    tag_query = serializers.CharField()
