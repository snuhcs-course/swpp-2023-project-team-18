from django.urls import path
from rest_framework_simplejwt.views import TokenRefreshView, TokenVerifyView
from .views import RegisterView, LoginView, UserRetrieveUpdateView

urlpatterns = [
    path('register', RegisterView.as_view()),
    path('login', LoginView.as_view()),
    path('info', UserRetrieveUpdateView.as_view()),
    path('token/refresh', TokenRefreshView.as_view()),
    path('token/verify', TokenVerifyView.as_view())
]