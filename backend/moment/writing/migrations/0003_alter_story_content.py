# Generated by Django 4.2.5 on 2023-10-25 01:10

from django.db import migrations, models


class Migration(migrations.Migration):
    dependencies = [
        ("writing", "0002_alter_momentpair_story"),
    ]

    operations = [
        migrations.AlterField(
            model_name="story",
            name="content",
            field=models.CharField(max_length=10000),
        ),
    ]
