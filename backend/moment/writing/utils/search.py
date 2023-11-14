from jamo import h2j, j2hcj


def spread_korean(string: str) -> str:
    return j2hcj(h2j(string))
