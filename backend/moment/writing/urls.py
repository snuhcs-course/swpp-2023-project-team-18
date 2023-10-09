from django.urls import path

from .views import MomentView

urlpatterns = [
    path("", MomentView.as_view()),
]
