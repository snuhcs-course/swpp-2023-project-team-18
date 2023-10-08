from django.urls import path

from user.views import RegisterView

urlpatterns = [
    path('register', RegisterView.as_view()),
]