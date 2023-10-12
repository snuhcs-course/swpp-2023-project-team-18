from tqdm import tqdm
import openai

from writing.data.jsonl import load_jsonl, save_jsonl, fpprint
from writing.utils.gpt import GPTAgent
from writing.utils.prompt import ExamplePromptTemplate


openai.api_key = ""  # FIXME: API key here

DATA_PATH = "writing/data/reply_test.jsonl"
TEST_NAME = "reply1"
OUTPUT_JSONL_PATH = f"results/{TEST_NAME}.jsonl"
OUTPUT_TXT_PATH = f"results/{TEST_NAME}.txt"

agent = GPTAgent()
results = []


def run():
    dataset = load_jsonl(DATA_PATH)

    for entry in tqdm(dataset[:4]):
        prompt = ExamplePromptTemplate.get_prompt(moment=entry["moment"])
        agent.reset_messages()
        agent.add_message(prompt)

        reply = agent.get_answer(timeout=10)
        entry["reply"] = reply
        results.append(entry)


def save():
    save_jsonl(OUTPUT_JSONL_PATH, results)
    fpprint(OUTPUT_TXT_PATH, results)


def main():
    try:
        run()
    except KeyboardInterrupt:
        ...
    finally:
        save()


if __name__ == "__main__":
    main()
