from django.urls import path

from .views import (
    MomentView,
    DayCompletionView,
    StoryView,
    StoryGenerateView,
    EmotionView,
    ScoreView,
    HashtagView,
)

from .search_views import HashtagCompleteView

urlpatterns = [
    path("moments/", MomentView.as_view()),
    path("day-completion/", DayCompletionView.as_view()),
    path("stories/", StoryView.as_view()),
    path("ai-story/", StoryGenerateView.as_view()),
    path("emotions/", EmotionView.as_view()),
    path("score/", ScoreView.as_view()),
    path("hashtags/", HashtagView.as_view()),
    path("hashtags/complete/", HashtagCompleteView.as_view()),
]
