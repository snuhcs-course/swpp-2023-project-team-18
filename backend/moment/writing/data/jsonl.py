from typing import Any
import json

from pprint import pformat


def load_jsonl(path: str) -> list[dict[str, Any]]:
    """
    Load a JSONL file into a list of dictionaries.
    """
    with open(path, "r") as f:
        data = [
            json.loads(line)
            for line in f.readlines()
            if line.strip() != "" and not line.startswith("//")
        ]
    return data


def save_jsonl(path: str, data: list[dict[str, Any]]):
    """
    Save a list of dictionaries into a JSONL file.
    """
    with open(path, "w") as f:
        for line in data:
            f.write(json.dumps(line, ensure_ascii=False) + "\n")


def create_or_append_jsonl(path: str, data: list[dict[str, Any]]):
    """
    Save a list of dictionaries into a JSONL file.
    """
    with open(path, "a+") as f:
        for line in data:
            f.write(json.dumps(line, ensure_ascii=False) + "\n")


def fpprint(
    path: str,
    obj: Any,
    width: int = 80,
    iterate: bool = False,
):
    """
    Pretty print an object to a file.
    """
    with open(path, "w") as f:
        if iterate:
            for line in obj:
                f.write(pformat(line, width=width) + "\n\n")
        else:
            f.write(pformat(obj) + "\n")
