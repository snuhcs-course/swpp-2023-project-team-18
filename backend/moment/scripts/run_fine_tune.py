import os, sys

sys.path.append(os.path.join(os.path.dirname(__file__), ".."))

from openai import OpenAI

from writing.data.jsonl import load_jsonl, save_jsonl, fpprint
from writing.utils.prompt import NUDGE_GENERATE_STEP_TWO

client = OpenAI()

TEST_NAME = "nudge_FT_1"
OUTPUT_JSONL_PATH = f"results/results_nudge_FT/{TEST_NAME}.jsonl"
OUTPUT_TXT_PATH = f"results/results_nudge_FT/{TEST_NAME}.txt"


def save(results):
    save_jsonl(OUTPUT_JSONL_PATH, results)
    fpprint(OUTPUT_TXT_PATH, results, width=50, iterate=True)


testsets = load_jsonl("writing/data/fine_tune_test.jsonl")

results = []
for i in range(len(testsets)):
    response = client.chat.completions.create(
        model="ft:gpt-3.5-turbo-1106:personal::8JeLvnaF",
        messages=[
            {"role": "system", "content": NUDGE_GENERATE_STEP_TWO},
            {"role": "user", "content": testsets[i]["messages"][1]["content"]},
        ],
    )
    print(response.choices[0].message.content)

    results.append(
        {
            "prompt": testsets[i]["messages"][1]["content"],
            "answer": testsets[i]["messages"][2]["content"],
            "reply": response.choices[0].message.content,
        }
    )

save(results)
