from django.urls import path

from .views import MomentView

urlpatterns = [
    path("moments/", MomentView.as_view()),
]
