from typing import Optional
from datetime import datetime


def log(
    message: str,
    tag: Optional[str] = None,
    place: Optional[str] = None,
):
    header = " "
    if tag is not None:
        header += f"[{tag.upper()}] "
    if place is not None:
        header += f"({place}) "

    print(f"{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}{header}{message}")
