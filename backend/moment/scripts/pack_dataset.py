import json
import random
from itertools import combinations

from writing.data.jsonl import save_jsonl, load_jsonl

# SUMMARY_PATH = "summary_ST.json"
DATA_PATH = "nudge_data.jsonl"

# NUDGES: list[list[str]] = []

PROCESSED_DATA_PATH = "nudge_processed_{}.jsonl"
TEST_SPLIT = 0.2


SYSTEM_MESSAGE = """\
As an AI bot, you're part of a smartphone application that allows users to write short diary entries, \
and then sends them relevant responses.
Your job is to look at the daily diary entries made by the user and use the information gained to give them a message with warm words. \
Briefly summarized version of two or three user diaries will be given, which the user wrote in the past three days. \
Each diary entry is separated by a line (---).
You have to write a message with heartwarming words based on the given diaries, \
and the user will see the message when they open the app in the fourth day. \
You might focus on an event or the user's feeling that lasts for several days.
"""


def preprocess():
    """
    데이터셋 파일을 읽어서 system message를 더하는 등 gpt 학습에 사용 가능한
    형태로 전처리 수행.
    """
    dataset: list[dict] = load_jsonl(DATA_PATH)
    processed: list[dict] = []

    for data in dataset:
        diaries: list[str] = data["diaries"]
        nudge: str = data["nudge"]
        concat = "\n---\n".join(diaries)

        p = {
            "messages": [
                {"role": "system", "content": SYSTEM_MESSAGE},
                {"role": "user", "content": concat},
                {"role": "assistant", "content": nudge},
            ]
        }
        processed.append(p)

    random.shuffle(processed)
    sep_idx = int(len(processed) * (1 - TEST_SPLIT))
    train = processed[:sep_idx]
    test = processed[sep_idx:]

    save_jsonl(PROCESSED_DATA_PATH.format("train"), train)
    save_jsonl(PROCESSED_DATA_PATH.format("test"), test)


# def make_dataset():
#     """
#     파일에 저장되어 있는 스토리 요약과, global variable로 정의된 nudge를 조합해
#     jsonl 형식의 데이터셋 파일 생성.
#     """
#     with open(SUMMARY_PATH, "r") as f:
#         summaries: list[list[str]] = json.loads(f.read())
#     assert len(summaries) == 4
#     assert all(len(s) == 5 for s in summaries)
#     assert len(NUDGES) == 4
#     assert all(len(nudges) == 10 for nudges in NUDGES)
#
#     combi = list(combinations(range(5), 2))
#     print(combi)
#     result: list[dict] = []
#
#     for theme in range(4):
#         s = summaries[theme]
#
#         for combi_idx in range(10):
#             i, j = combi[combi_idx]
#             data = {
#                 "diaries": [s[i], s[j]],
#                 "nudge": NUDGES[theme][combi_idx],
#             }
#             result.append(data)
#
#     save_jsonl(DATA_PATH, result)
#     print("data saved in", DATA_PATH)
#     print("length:", len(result))
#
#
# def collect_summaries():
#     """
#     스토리 요약을 복붙해서 넣으면 파일로 만들어줌.
#     """
#     summaries: list[list[str]] = [[""] * 5 for _ in range(4)]
#
#     for i in range(4):
#         for j in range(5):
#             print(f"Theme {i}, diary {j}")
#             summary = _get_input()
#
#             summaries[i][j] = summary
#
#     json_string = json.dumps(summaries)
#     with open(SUMMARY_PATH, "w") as f:
#         f.write(json_string)
#
#
# def _get_input() -> str:
#     result = ""
#     while True:
#         line = input()
#         if line == "":
#             break
#         result += line + "\n"
#
#     return result
#
#
# def check_summaries():
#     """
#     요약이 잘 저장되었는지 print 해서 확인.
#     """
#     with open(SUMMARY_PATH, "r") as f:
#         summaries = json.loads(f.read())
#
#     for i in range(4):
#         for j in range(5):
#             print(f"Theme {i}, diary {j}")
#             print(summaries[i][j])
#             print()


if __name__ == "__main__":
    preprocess()
