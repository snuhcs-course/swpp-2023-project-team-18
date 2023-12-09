# Generated by Django 4.2.5 on 2023-11-20 07:36

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):
    dependencies = [
        ("writing", "0010_alter_momentpair_reply"),
    ]

    operations = [
        migrations.RemoveField(
            model_name="nudge",
            name="date",
        ),
        migrations.RemoveField(
            model_name="nudge",
            name="is_completed",
        ),
        migrations.AddField(
            model_name="nudge",
            name="is_deleted",
            field=models.BooleanField(default=False),
        ),
        migrations.AddField(
            model_name="nudge",
            name="story",
            field=models.ForeignKey(
                null=True,
                on_delete=django.db.models.deletion.CASCADE,
                to="writing.story",
            ),
        ),
        migrations.AddField(
            model_name="nudge",
            name="summarized_story",
            field=models.CharField(default="", max_length=1000),
        ),
        migrations.AlterField(
            model_name="nudge",
            name="content",
            field=models.CharField(default="", max_length=1000),
        ),
    ]