# Generated by Django 4.2.5 on 2023-10-25 05:14

from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [
        ("writing", "0006_alter_story_created_at"),
    ]

    operations = [
        migrations.AlterField(
            model_name="hashtag",
            name="created_at",
            field=models.DateTimeField(),
        ),
        migrations.AlterField(
            model_name="nudge",
            name="created_at",
            field=models.DateTimeField(),
        ),
        migrations.AlterField(
            model_name="story",
            name="created_at",
            field=models.DateTimeField(),
        ),
    ]
