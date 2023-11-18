import json
import random
from itertools import combinations
from typing import Dict, List

from writing.data.jsonl import save_jsonl, load_jsonl

# SUMMARY_PATH = "raw_data/summary_ST.json"
DATA_PATH = "ratio_data/ratio3.jsonl"

# PROCESSED_DATA_PATH = "improved_processed/sys04_{}.jsonl"
# TEST_SPLIT = 0.2

# NUDGES: List[List[str]] = [[], [], [], []]


# SYSTEM_MESSAGE_03 = """\
# As an AI bot, you're part of a smartphone application that allows users to write short diary entries, \
# and then sends them relevant responses.
# You will be provided with two or three summarized versions of diary entries, \
# which the user has written in the past couple of days.
# You should write a short message that will be shown to the user \
# when they open the app the next day.
# """

SYSTEM_MESSAGE_04 = """\
You will be provided with two or three summarized versions of diary entries, \
which the user has written in the past couple of days.
Your job is to write a short message that will be shown to the user \
when they open the app the next day.
You should try to write something - not a blank - unless you cannot find any appropriate contents to write.
"""


def preprocess():
    """
    데이터셋 파일을 읽어서 system message를 더하는 등 gpt 학습에 사용 가능한
    형태로 전처리 수행.
    """
    dataset: List[Dict] = load_jsonl(DATA_PATH)
    processed: List[Dict] = []

    for data in dataset:
        diaries: List[str] = data["diaries"]
        nudge: str = data["nudge"]
        concat = _process_diaries(diaries)

        p = {
            "messages": [
                {"role": "system", "content": SYSTEM_MESSAGE_04},
                {"role": "user", "content": concat},
                {"role": "assistant", "content": nudge},
            ]
        }
        processed.append(p)

    random.shuffle(processed)
    # sep_idx = int(len(processed) * (1 - TEST_SPLIT))
    # train = processed[:sep_idx]
    # test = processed[sep_idx:]

    # print(f"saving processed data in {PROCESSED_DATA_PATH.format('train/test')}")
    # save_jsonl(PROCESSED_DATA_PATH.format("train"), train)
    # save_jsonl(PROCESSED_DATA_PATH.format("test"), test)

    save_jsonl("ratio_data/processed3.jsonl", processed)


def _process_diaries(diaries: List[str]) -> str:
    results = []
    for diary in diaries:
        diary = diary.replace("* ", "").replace("\n", " ").strip()
        results.append(diary)
    return "\n---\n".join(results)


def split_dataset():
    """
    train : test로 split
    train에는 nudge 있는 것만 들어감
    """
    raw2 = load_jsonl("improved_data/nudge2.jsonl")
    raw3 = load_jsonl("improved_data/nudge.jsonl")
    with_nudge, without_nudge = [[], []], [[], []]
    for data in raw2:
        if data["nudge"] == "":
            without_nudge[0].append(data)
        else:
            with_nudge[0].append(data)
    for data in raw3:
        if data["nudge"] == "":
            without_nudge[1].append(data)
        else:
            with_nudge[1].append(data)

    return with_nudge, without_nudge


def sample_data(pool1: List, pool2: List, num1: int, num2: int):
    indices1 = random.sample(range(len(pool1)), num1)
    result1 = [pool1[i] for i in indices1]
    indices2 = random.sample(range(len(pool2)), num2)
    result2 = [pool2[i] for i in indices2]
    return result1, result2


def save_pair(*pools, filename: str):
    result = []
    for pool in pools:
        result += list(pool)
    print(len(result))
    save_jsonl(f"improved_data/{filename}.jsonl", result)


# def make_dataset():
#     """
#     파일에 저장되어 있는 스토리 요약과, global variable로 정의된 nudge를 조합해
#     jsonl 형식의 데이터셋 파일 생성.
#     """
#     with open(SUMMARY_PATH, "r") as f:
#         summaries: List[List[str]] = json.loads(f.read())
#     assert len(summaries) == 4
#     assert all(len(s) == 5 for s in summaries)
#     assert len(NUDGES) == 4
#     assert all(len(nudges) == 10 for nudges in NUDGES)

#     combi = list(combinations(range(5), 2))
#     print(combi)
#     result: List[dict] = []

#     for theme in range(4):
#         s = summaries[theme]

#         for combi_idx in range(10):
#             i, j = combi[combi_idx]
#             data = {
#                 "diaries": [s[i], s[j]],
#                 "nudge": NUDGES[theme][combi_idx],
#             }
#             result.append(data)

#     save_jsonl(DATA_PATH, result)
#     print("data saved in", DATA_PATH)
#     print("length:", len(result))


# def collect_summaries():
#     """
#     스토리 요약을 복붙해서 넣으면 파일로 만들어줌.
#     """
#     summaries: List[List[str]] = [[""] * 5 for _ in range(4)]

#     for i in range(4):
#         for j in range(5):
#             print(f"Theme {i}, diary {j}")
#             summary = _get_input()

#             summaries[i][j] = summary

#     json_string = json.dumps(summaries)
#     with open(SUMMARY_PATH, "w") as f:
#         f.write(json_string)


# def _get_input() -> str:
#     result = ""
#     while True:
#         line = input()
#         if line == "":
#             break
#         result += line + "\n"

#     return result


# def check_summaries():
#     """
#     요약이 잘 저장되었는지 print 해서 확인.
#     """
#     with open(SUMMARY_PATH, "r") as f:
#         summaries = json.loads(f.read())
#     for i in range(4):
#         for j in range(5):
#             print(f"Theme {i}, diary {j}")
#             print(summaries[i][j])
#             print()


if __name__ == "__main__":
    preprocess()
