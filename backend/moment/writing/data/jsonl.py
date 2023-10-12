from typing import Any
import json

from pprint import pformat


def load_jsonl(path: str) -> list[dict[str, Any]]:
    """
    Load a JSONL file into a list of dictionaries.
    """
    with open(path, "r") as f:
        data = [json.loads(line) for line in f.readlines()]
    return data


def save_jsonl(path: str, data: list[dict[str, Any]]):
    """
    Save a list of dictionaries into a JSONL file.
    """
    with open(path, "w") as f:
        for line in data:
            f.write(json.dumps(line, ensure_ascii=False) + "\n")


def fpprint(path: str, obj: Any):
    """
    Pretty print an object to a file.
    """
    with open(path, "w") as f:
        f.write(pformat(obj) + "\n")
