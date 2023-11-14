import openai

from writing.utils.gpt import GPTAgent
import diaries


openai.api_key = "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"


PROMPT = """\
As an AI bot, you're part of a smartphone application that allows users to write short diary entries, \
and then sends them relevant responses.
Your job is to look at the daily diary entries made by the user and use the information gained to give them a message with warm words. \
As a preliminary step to achieving that goal, you must first extract information about what important events the user has experienced. \
When this is done, we should be able to look at this information and create a message to display to the user.
The user's diary will be given in Korean, and you will need to briefly summarize the diary in ENGLISH.
The summary should be very concise, NO MORE THAN THREE SENTENCES. \
It should only consist of relevant information about important events. \
If the given diary has no content that is worth summarizing, you should simply return a bullet point "*".

***

Here is a couple of examples to show how you should summarize diary entries:

[User Diary]
내일은 통사론 중간고사가 있다. 교수님은 통사론은 원래 암기를 해야 하는 과목이 아니라고, 지금까지 수업을 잘 들었다면 특별히 따로 공부를 하지 않아도 시험을 잘 볼 수 있을 거라고 했다. 정말일까? 아마도 문장이 주어지면 X-bar theory를 따라서 문장 구조 트리를 그리는 것이 가장 중요한 내용이겠지만, 그밖에 자잘한 용어 같은 알아두어야 할 것들이 꽤 된다. 아마도 거짓말일 것 같다. 그러니 공부를 해야 한다.
다음 주 일요일까지는 정보경제학 문제 풀이 과제를 제출해야 한다. 여섯 명이 팀을 이루어 제출해야 하는 거라서 금요일에는 팀원들과 학교에서 모이기로 했다. 그 전에 문제를 다 풀어봐야 하는데. 지금까지의 수업 내용조차 제대로 이해하지 못하고 있다. 마치 자꾸만 부채가 쌓여 가는 것 같다. 수업 시간에는 매번 칠판에 적힌 것들을 받아 적느라 바쁘고. 어서 따라잡아야 할 텐데.
그리고 여전히 계속되는 소개원실. 아까도 저녁 8시에 집에 도착해서 2시간 넘게 코딩을 했다. 너무 할 일이 많다.

[Summary]
* Mid-term of syntax class tomorrow.
* Information economics problem-solving assignment due next Sunday.
* Have too much work to do these days.

[User Diary]
오늘은 주제를 조금 더 구체화하는 시간을 가졌다. InstructPix2Pix라고 최근에 나온 논문이 있는데, 이 논문이 이미 자연어만으로 모든 image editing task를 가능하게 하고 있었다.. 이미 이런 논문이 있다는 사실에 조금 절망했지만, 모델을 돌려보며 개선의 여지가 없을지 면밀하게 살펴보았다. 다행히 성능이 엄청나게 완벽하지는 않았고, object-level task를 처리하는 것을 어려워하는 것으로 보였다. 즉 스타일 전체를 바꾸는 style-transfer task는 매우 잘 수행하는데, object-level task에서는 바꾸지 말아야 하는 부분을 자꾸 바꾸는 것으로 보였다. 이 논문의 저자들이 training data를 공개해 뒀길래 들여다보니.. 애초에 input과 output을 하나의 pipeline으로 만드는 게 아니라서 이러한 object-level task는 잘 수행하지 못하는 게 당연해 보였다. 데이터를 좀 더 추가하는 방식으로 해결해 볼 수 있을 것 같은데.. 한번 해봐야겠다! Instructpix2pix 저자들이 paper의 코드를 매우 잘 활용할 수 있도록 공개해 놔서 그걸 토대로 발전시켜 나가면 될 것 같다.
그런데 코드 진짜 깔끔하고 이용하기 쉽게 짜뒀다. 약간 간지난다는 생각이 들었고, 나도 나중에 연구 다 하고 내 paper의 코드를 사람들이 사용하기 쉽게 깃헙에 올려놔야겠다는 생각을 했다. Machine Learning Society가 이렇게 빨리 발전할 수 있었던 이유에 코드가 거의 다 공개되어 있다는 점이 한 몫을 한 것 같고, 나도 이 분야의 발전에 기여할 수 있도록 코드를 잘 정리하여 올려놔야겠다. Open-source의 학문을 연구할 수 있게 되어 기쁘다.

[Summary]
* Still working on the research topic.
* Consulted existing papers and codes to refine the research topic.
* Found some points to improve.

[User Diary]
오늘은 시험 공부를 했다. 시험 공부가 지속되다 보니 힘들었다. 또 몸이 좋지 않아 기침이 계속 나왔다. 그래도 약을 먹으니 상태가 호전되었다.

[Summary]
* Studied for the exam.
* Was sick and kept coughing.

[User Diary]
아아

[Summary]
*

***

Now, it's your turn to summarize the diary entry:

[User Diary]
{diary}

[Summary]
"""


def main():
    agent = GPTAgent()

    for diary in [diaries.ST]:
        agent.reset_messages()
        agent.add_message(PROMPT.format(diary=diary))

        print("--------Calling GPT API")
        print("[DIARY]\n" + diary)

        reply = agent.get_answer(timeout=20, max_trial=10, temperature=0.7)
        print(reply, "\n")


if __name__ == "__main__":
    main()
