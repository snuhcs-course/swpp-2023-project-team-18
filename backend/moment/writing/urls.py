from django.urls import path

from .views import MomentView, DayCompletionView

urlpatterns = [
    path("moments/", MomentView.as_view()),
    path("day-completion/", DayCompletionView.as_view()),
]
