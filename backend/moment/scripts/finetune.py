from itertools import combinations
from typing import Optional

import openai

from scripts.pack_dataset import _process_diaries
from writing.data.jsonl import load_jsonl
from writing.utils.gpt import GPTAgent

# FIXME: Put OpenAI API key
API_KEY = "..."


def finetune(data_path: str):
    client = openai.OpenAI(api_key=API_KEY)

    print("uploading file...")
    file_result = client.files.create(
        file=open(data_path, "rb"),
        purpose="fine-tune",
    )

    print("creating job...")
    ft_result = client.fine_tuning.jobs.create(
        training_file=file_result.id,
        model="gpt-3.5-turbo-1106",
        hyperparameters=dict(
            n_epochs=5,
        ),
    )

    print("done")
    return {
        "file": file_result,
        "ft": ft_result,
    }


def print_jobs(idx: Optional[int] = None):
    client = openai.OpenAI(api_key=API_KEY)
    jobs = list(client.fine_tuning.jobs.list())

    for i in range(len(jobs)):
        if idx is not None and i != idx:
            continue
        print(jobs[i])
        print()


def get_job(job_id: str):
    client = openai.OpenAI(api_key=API_KEY)
    return client.fine_tuning.jobs.retrieve(job_id)


TEST_DATASET = "test_data/processed.jsonl"
MODEL1 = "ft:gpt-3.5-turbo-1106:snu-swpp-2023f::8LUtmpp5"
MODEL2 = "ft:gpt-3.5-turbo-1106:snu-swpp-2023f::8LVIBzaa"
MODEL3 = "ft:gpt-3.5-turbo-1106:snu-swpp-2023f::8LUva8SH"


def eval():
    openai.api_key = API_KEY
    agent = GPTAgent(model=MODEL3)

    dataset = load_jsonl(TEST_DATASET)
    print(f"loaded eval dataset (len={len(dataset)})")

    for data in dataset:
        print("#" * 50)

        system_message = data["messages"][0]["content"]
        message = data["messages"][1]["content"]
        print(f">>> message\n{message}")

        agent.reset_messages()
        agent.add_message(content=system_message, role="system")
        agent.add_message(content=message)
        answer = agent.get_answer(timeout=20, temperature=0.3)
        print(f">>> answer\n{answer}")

        orig_answer = data["messages"][2]["content"]
        print(f">>> combination\n{orig_answer}")
        print()


TEST_SUMMARIES = [
    """* Arrived in Osaka and explored Dotonbori for famous local foods.
* Enjoyed kushikatsu and takoyaki, both of which had delicious flavors.
* Took a walk in Tenjinbashisuji Shopping Street and admired the vibrant atmosphere of Osaka.""",
    """* Visited iconic tourist attractions in Osaka: Osaka Castle, Shitennoji Temple, and Abeno Harukas 300.
* Learned about the history and culture of Osaka through various exhibitions and temples.
* Enjoyed the panoramic view of Osaka city from the observation decks of Shitennoji Temple and Abeno Harukas 300."
* Visited Universal Studio Japan in Osaka and experienced various attractions such as Harry Potter, Jurassic Park, Minions, and Superman. Enjoyed feeling like a movie protagonist and was impressed by the attention to detail. Excited to explore Kyoto the next day and visit famous temples and traditional streets.""",
    """* Moved from Osaka to Kyoto, visited Arashiyama bamboo forest and Kiyomizu-dera temple.
* Had Kyoto udon for dinner.
* Enjoyed the beauty and charm of Kyoto.""",
    """* Visited Kinkakuji (Golden Pavilion) and Chishakuin (Blue-green Pavilion) in Kyoto.
* Enjoyed the beautiful golden and blue-green roofs of the temples.
* Had a taste of Kyoto curry and reflected on the historical and cultural experiences in Kyoto.""",
    """* Last day in Kyoto.
* Visited Kiyomizu-dera Ryokan and enjoyed traditional cuisine and hot springs.
* Had a relaxing time in the Ryokan's garden and enjoyed the beautiful view of Kiyomizu-dera.
* Heading back to Korea tomorrow.""",
    """* Last day of a trip to Japan, visited Osaka and Kyoto.
* Explored various attractions and experienced Japanese culture.
* Enjoyed the trip and gained valuable experiences.""",
]


def test():
    openai.api_key = API_KEY
    agent = GPTAgent(model="ft:gpt-3.5-turbo-0613:snu-swpp-2023f::8L7dtRGh")

    combi = list(combinations(TEST_SUMMARIES, 2))

    for data in combi:
        print("#" * 50)

        concat = _process_diaries(data)
        print(f">>> message\n{concat}")

        agent.reset_messages()
        agent.add_message(content=concat)
        answer = agent.get_answer(timeout=None, temperature=0.7)
        print(f">>> answer\n{answer}")


if __name__ == "__main__":
    eval()
