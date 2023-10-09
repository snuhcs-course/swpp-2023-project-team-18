from rest_framework import generics, status, permissions
from rest_framework.authtoken.models import Token
from rest_framework.response import Response
from .serializers import (
    RegisterSerializer,
    LoginSerializer,
    UserDetailSerializer,
    UserUpdateSerializer,
)


# Create your views here.
class RegisterView(generics.GenericAPIView):
    serializer_class = RegisterSerializer

    def post(self, request):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        user = serializer.save()
        token = Token.objects.create(user=user)
        return Response(
            {
                "user": RegisterSerializer(
                    user, context=self.get_serializer_context()
                ).data,
                "token": token.key,
            }
        )


class LoginView(generics.GenericAPIView):
    serializer_class = LoginSerializer

    def post(self, request):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        token = serializer.validated_data
        return Response({"token": token.key}, status=status.HTTP_200_OK)


class UserRetrieveUpdateView(generics.RetrieveUpdateAPIView):
    permission_classes = [permissions.IsAuthenticated]
    serializer_class = UserDetailSerializer

    def get_serializer_class(self):
        if self.request.method == "GET":
            return UserDetailSerializer
        return UserUpdateSerializer

    def get_object(self):
        return self.request.user
