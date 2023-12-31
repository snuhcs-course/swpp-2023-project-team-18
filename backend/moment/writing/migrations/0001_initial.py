# Generated by Django 4.2.5 on 2023-10-09 04:26

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):
    initial = True

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name="Hashtag",
            fields=[
                (
                    "id",
                    models.BigAutoField(
                        auto_created=True,
                        primary_key=True,
                        serialize=False,
                        verbose_name="ID",
                    ),
                ),
                ("created_at", models.DateTimeField(auto_created=True)),
                ("content", models.CharField(max_length=50)),
            ],
        ),
        migrations.CreateModel(
            name="Story",
            fields=[
                (
                    "id",
                    models.BigAutoField(
                        auto_created=True,
                        primary_key=True,
                        serialize=False,
                        verbose_name="ID",
                    ),
                ),
                ("created_at", models.DateTimeField(auto_created=True)),
                (
                    "emotion",
                    models.CharField(
                        choices=[
                            ("excited1", "excited1"),
                            ("excited2", "excited2"),
                            ("happy1", "happy1"),
                            ("happy2", "happy2"),
                            ("normal1", "normal1"),
                            ("normal2", "normal2"),
                            ("sad1", "sad1"),
                            ("sad2", "sad2"),
                            ("angry1", "angry1"),
                            ("angry2", "angry2"),
                        ],
                        default="normal1",
                        max_length=100,
                    ),
                ),
                ("score", models.IntegerField(default=3)),
                ("is_point_completed", models.BooleanField(default=False)),
                ("title", models.CharField(max_length=100)),
                ("content", models.CharField(max_length=1000)),
                ("hashtags", models.ManyToManyField(to="writing.hashtag")),
                (
                    "user",
                    models.ForeignKey(
                        on_delete=django.db.models.deletion.CASCADE,
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
            ],
        ),
        migrations.CreateModel(
            name="Nudge",
            fields=[
                (
                    "id",
                    models.BigAutoField(
                        auto_created=True,
                        primary_key=True,
                        serialize=False,
                        verbose_name="ID",
                    ),
                ),
                ("created_at", models.DateTimeField(auto_created=True)),
                ("date", models.DateField()),
                ("content", models.CharField(max_length=1000)),
                ("is_completed", models.BooleanField(default=False)),
                (
                    "user",
                    models.ForeignKey(
                        on_delete=django.db.models.deletion.CASCADE,
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
            ],
        ),
        migrations.CreateModel(
            name="MomentPair",
            fields=[
                (
                    "id",
                    models.BigAutoField(
                        auto_created=True,
                        primary_key=True,
                        serialize=False,
                        verbose_name="ID",
                    ),
                ),
                ("moment", models.CharField(max_length=1000)),
                ("reply", models.CharField(blank=True, max_length=1000)),
                ("moment_created_at", models.DateTimeField()),
                ("reply_created_at", models.DateTimeField()),
                (
                    "story",
                    models.ForeignKey(
                        null=True,
                        on_delete=django.db.models.deletion.CASCADE,
                        to="writing.story",
                    ),
                ),
                (
                    "user",
                    models.ForeignKey(
                        on_delete=django.db.models.deletion.CASCADE,
                        to=settings.AUTH_USER_MODEL,
                    ),
                ),
            ],
        ),
    ]
