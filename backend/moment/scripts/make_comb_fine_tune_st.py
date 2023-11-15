# to make all combinations of fine tuning dataset
import os, sys

sys.path.append(os.path.join(os.path.dirname(__file__), ".."))

import itertools, random
from writing.data.jsonl import load_jsonl, create_or_append_jsonl
from writing.utils.prompt import NUDGE_GENERATE_STEP_TWO
from scripts.constants import FT_TRAIN_FILE_PTH, FT_TEST_FILE_PTH

# FIXME Change the SIZE of combination (2 or 3).
# All the combinations in the row index range [BASE_NUM, END_NUM) according to the SIZE will be returned.
# Fill in the NUDGES as intended. Remember, the order of the combinations would be lexicographical.
# ----------------------------------------------
NUDGE_FILE_PTHS = ["writing/data/nudge_data.jsonl", "writing/data/nudge_data_2.jsonl"] 
# ----------------------------------------------

TEST_SPLIT = 0.2

if __name__ == "__main__":

    for nudge_file in NUDGE_FILE_PTHS:
        nudge_dataset = load_jsonl(nudge_file)
        test_idxs = random.sample(range(len(nudge_dataset)), int(TEST_SPLIT * len(nudge_dataset)))

        ft_train_dataset = []
        ft_test_dataset = []

        system_string = NUDGE_GENERATE_STEP_TWO

        for idx, row in enumerate(nudge_dataset):
            user_string = ""
            for curr_diary in row["diaries"]:
                user_string += curr_diary + ";"

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
                        "content": row["nudge"],
                    },
                ]
            }
            if idx in test_idxs:
                ft_test_dataset.append(result_dict)
            else:
                ft_train_dataset.append(result_dict)

        create_or_append_jsonl(FT_TRAIN_FILE_PTH, ft_train_dataset)
        create_or_append_jsonl(FT_TEST_FILE_PTH, ft_test_dataset)
