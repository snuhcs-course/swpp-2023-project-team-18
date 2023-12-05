# Generated by Django 4.2.5 on 2023-10-27 03:09

from django.db import migrations, models
import writing.constants


class Migration(migrations.Migration):
    dependencies = [
        ("writing", "0008_remove_hashtag_created_at"),
    ]

    operations = [
        migrations.AlterField(
            model_name="momentpair",
            name="moment",
            field=models.CharField(max_length=100),
        ),
        migrations.AlterField(
            model_name="momentpair",
            name="reply",
            field=models.CharField(blank=True, max_length=100),
        ),
        migrations.AlterField(
            model_name="story",
            name="content",
            field=models.CharField(max_length=1000),
        ),
        migrations.AlterField(
            model_name="story",
            name="emotion",
            field=models.CharField(
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
                    ("invalid", "invalid"),
                ],
                default=writing.constants.Emotions["NORMAL1"],
                max_length=100,
            ),
        ),
        migrations.AlterField(
            model_name="story",
            name="title",
            field=models.CharField(max_length=20),
        ),
    ]
