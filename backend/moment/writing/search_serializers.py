from rest_framework import serializers

from .models import Story


class HashtagCompleteSerializer(serializers.Serializer):
    tag_query = serializers.CharField()


class SearchQuerySerializer(serializers.Serializer):
    query = serializers.CharField()


class HashtagSearchSerializer(serializers.ModelSerializer):
    class Meta:
        model = Story
        fields = [
            "id",
            "emotion",
            "title",
            "content",
            "created_at",
        ]

    def to_representation(self, instance: Story):
        res = super().to_representation(instance)
        res["created_at"] = int(instance.created_at.timestamp())
        return res


class ContentSearchSerializer(serializers.ModelSerializer):
    content = serializers.SerializerMethodField()
    field = serializers.SerializerMethodField()

    class Meta:
        model = Story
        fields = ["id", "emotion", "title", "created_at", "content", "field"]

    def get_content(self, obj):
        print(self.context)
        return self.context["content"]

    def get_field(self, obj):
        return self.context["field"]

    def to_representation(self, instance: Story):
        res = super().to_representation(instance)
        res["created_at"] = int(instance.created_at.timestamp())
        return res
