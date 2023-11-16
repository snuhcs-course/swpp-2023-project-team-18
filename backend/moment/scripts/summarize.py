import openai

from writing.utils.gpt import GPTAgent


# FIXME: Put OpenAI API key
openai.api_key = "..."


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

DIARIES = [
    """\
할아버지네 집에 가서 저녁 만찬을 즐겼다. 사실 즐겼다고 하기 애매한 수준이기도 하다. 우리 가족들은 가족이라고 부르기도 민망한 수준이기에.. 왜 삼촌과 누나, 형들과 매번 이렇게 기싸움하면서 살아야 하는지 모르겠다. 친구들은 내가 돈 많은 집 아들이라서 부럽다고들 했다. 그런데 사실 나는 전혀 행복하지 않다. 저녁 만찬을 즐기러 간다고 하니 영찬이가 그런 것도 하는게 너무 부럽다고 하던데.. 나는 그 숨 막히는 자리가 매달 반복된다는게 너무 괴로울 뿐이다. 사실 돈은 이미 충분히 많은데, 왜 자꾸 이 사업을 따내려고 노력하는지 모르겠다. 사업을 더 벌려서 도대체 뭘 하려고.. 아버지는 그냥 능력을 과시하고 싶으신 것 뿐인 것 같다. 하.. 주변의 평범하게 화목한 가정들이 부럽다.
집에 돌아와 누워서 곰곰히 생각해보니, 나는 너무 행복이라는 것의 역치가 높은 것 같았다. 원래 결핍과 부족함과 더불어 간절한 염원이 있어야 미친듯한 행복을 느낄 수 있는 건데, 나는 어려서부터 가질 수 있는 것들을 모두 가졌기 때문에 그것이 불가능했던 것 같다. 서민놀이라도 해 봐야 하나? 모르겠다. 내일 선자 아줌마한테 물어봐야지.""",
    """\
오늘은 내가 바뀌기로 마음 먹은 첫째 날이다. 선자 아줌마한테 물어보니 서민들의 삶을 경험하기 위해서는 당연하게 주어지는 돈을 거부할 줄 알아야 한다고 한다. 나는 이미 한 달에 용돈을 100만원을 받고 있으니, 그것부터 확 줄여서 받겠다고 말하고 아껴쓰는 재미를 느껴보아야 한다고 말하셨다. 그래서 20만원으로 줄여서 받는 게 어떠냐고 물어보니 그것도 많다고 하셨다. 10만원 정도로 시작하는 것이 좋겠다고 하셨는데.. 너무 적은 것 아닌가? 영찬이 같은 애들은 이렇게 생활하는 건가? 이렇게 말하니까 선자 아줌마가 그렇게 말하면 못 쓴다고 화내셨다. 그 친구들의 삶을 이해해야 좋은 사람이 되는 거라고 하셨는데.. 좋은 사람 되기 참 어렵네.
학교에 가니 지민이가 나를 반갑게 맞아줬다. 맨날 돈 주면서 뭐 사오라고 한 다음에 거스름돈을 가지라고 했는데.. 그게 그렇게 좋았는지 매번 이렇게 나를 반긴다. 오늘은 돈 없다고 하니 급격히 시무룩해지는 것이 좀 그렇네.. 나를 그냥 돈줄로만 봤던 건가? 좀 현타가 왔다. 그 외에도 다른 친구들이 기웃거리다 가버렸다. 내가 진짜 돈 말고는 별 볼일 없는 놈인가 싶어서 자괴감이 들려는 찰나, 영찬이가 나에게 다가와서 같이 있어줬다. 영찬이.. 좀 멍청하기는 하지만 착한 것 같기는 하다. 원래 힘들 때 같이 있어주는 친구가 진짜 친구라는데, 그럼 영찬이가 내 진짜 친구인건가? 흠.. 좀 덜 떨어지는 것 같기는 하지만, 그래도 받아준다! 내 베프 1호로 임명하기로 했다.
돈이 없는 생활 1일차, 사람을 걸러낼 수 있는 방법을 찾은 것 같다. 더 행복해지는 방법에 대해서는 잘 모르겠지만.. 일단 뭔가를 얻은 것 같긴 하니까 만족이다. 내일 영찬이한테 고오급 빵을 하나 선물해줘야지. 아 맞다.. 나 돈 없는데.. 집 냉장고에서 하나 훔쳐서 가야겠다. 후후.""",
    """\
""",
]


def main():
    agent = GPTAgent()

    for diary in DIARIES:
        agent.reset_messages()
        agent.add_message(PROMPT.format(diary=diary))

        print("--------Calling GPT API")
        print("[DIARY]\n" + diary)

        reply = agent.get_answer(timeout=20, max_trial=10, temperature=0.7)
        print(reply, "\n")


if __name__ == "__main__":
    main()
