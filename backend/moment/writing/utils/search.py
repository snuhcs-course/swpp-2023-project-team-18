from jamo import h2j, j2hcj


def spread_korean(string: str) -> str:
    return j2hcj(h2j(string))


def process_query(string: str) -> str:
    return spread_korean(string).lower()
