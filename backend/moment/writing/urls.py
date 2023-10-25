from django.urls import path

from .views import MomentView, DayCompletionView, StoryView

urlpatterns = [
    path("moments/", MomentView.as_view()),
    path("day-completion/", DayCompletionView.as_view()),
    path("stories/", StoryView.as_view()),
]
