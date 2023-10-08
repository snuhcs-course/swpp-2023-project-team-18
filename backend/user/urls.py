from django.urls import path

from user.views import RegisterView, LoginView, UserRetrieveUpdateView

urlpatterns = [
    path('register', RegisterView.as_view()),
    path('login', LoginView.as_view()),
    path('info', UserRetrieveUpdateView.as_view())
]