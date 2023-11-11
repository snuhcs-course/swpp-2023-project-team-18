# to make all combinations of fine tuning dataset
import os, sys

sys.path.append(os.path.join(os.path.dirname(__file__), ".."))

import itertools, random
from writing.data.jsonl import load_jsonl, create_or_append_jsonl
from writing.utils.prompt import NUDGE_GENERATE_STEP_TWO

# FIXME Change the SIZE (2 or 3) and range of replies
# All the combinations of [BASE_NUM, END_NUM) according to the SIZE will be returned
# Fill in the NUDGES as intended. Remember, the order of combinations would be lexicographical.
# ----------------------------------------------
SIZE = 3
BASE_NUM = 15
END_NUM = 20
NUDGES = [
    "X",
    "X",
    "X",
    "X",
    "X",
    "X",
    "X",
    "X",
    "X",
    "요즘 열애설 때문에 스트레스를 많이 받으시는 것 같아요. 오늘은 기분이 좀 어떤가요?",
]
# ----------------------------------------------

TEST_SPLIT = 0.2
SUMMARY_FILE_PTH = "results/results_nudge/nudge_ST_2.jsonl"
SUMMARY_COL = "reply"
SAVE_TRAIN_FILE_PTH = "writing/data/fine_tune_train.jsonl"
SAVE_TEST_FILE_PTH = "writing/data/fine_tune_test.jsonl"

idx_groups = sorted(list(itertools.combinations(range(BASE_NUM, END_NUM), SIZE)))
assert len(NUDGES) == len(idx_groups)

test_idxs = random.sample(range(len(idx_groups)), int(TEST_SPLIT * len(idx_groups)))

summary_dataset = load_jsonl(SUMMARY_FILE_PTH)
ft_train_dataset = []
ft_test_dataset = []


system_string = NUDGE_GENERATE_STEP_TWO

for idx, curr_group in enumerate(idx_groups):
    user_string = ""
    for curr_idx in curr_group:
        user_string += summary_dataset[curr_idx][SUMMARY_COL] + ";"

    result_dict = {
        "messages": [
            {
                "role": "system",
                "content": system_string,
            },
            {
                "role": "user",
                "content": user_string,
            },
            {
                "role": "assistant",
                "content": NUDGES[idx],
            },
        ]
    }
    if idx in test_idxs:
        ft_test_dataset.append(result_dict)
    else:
        ft_train_dataset.append(result_dict)

create_or_append_jsonl(SAVE_TRAIN_FILE_PTH, ft_train_dataset)
create_or_append_jsonl(SAVE_TEST_FILE_PTH, ft_test_dataset)
