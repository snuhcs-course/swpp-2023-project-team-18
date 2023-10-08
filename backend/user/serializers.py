from rest_framework import serializers

from user.models import User


class RegisterSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['username', 'password', 'nickname']
        extra_kwargs = {'password': {'write_only': True}}

    def to_internal_value(self, data):
        internal_value = super().to_internal_value(data)
        return {**internal_value, 'email': "dummy@example.com"}

    def create(self, validated_data):
        user = User(
            nickname=validated_data['nickname'],
            username=validated_data['username'],
            email=validated_data['email'],
        )
        user.set_password(validated_data['password'])
        user.save()
        return user