from .auto_cron import auto_completion_job
from .nudge_cron import nudge_creation_job


def execute_cron_jobs():
    auto_completion_job()
    nudge_creation_job()
