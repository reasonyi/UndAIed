import google.generativeai as genai
from environs import Env
# from .prompt import prompt
# deprecated

class GeminiBot:
    def __init__(self):
        env = Env()
        env.read_env()
        genai.configure(api_key=env.str("GEMINI_API_KEY"))

        # 모델 초기화
        self.model = genai.GenerativeModel(
            "gemini-2.0-flash-exp",
            generation_config={
                "temperature": 1,
                "top_p": 0.95,
                "top_k": 40,
                "max_output_tokens": 8192,
                "response_mime_type": "text/plain",
            },
            # system_instruction=prompt,
        )

    def generate_response(self, AI_INFO: dict, user_input: dict) -> str:
        """단일 턴 채팅에서 새로운 입력에 대한 응답을 생성합니다."""
        AI_NUM = AI_INFO[-2]
        print(user_input)
        prompt = f"""
# [참가자에게 공개 가능한 정보]
## 게임
언다이드(undaied) 게임은 여덟 명의 참가자와 한 명의 사회자로 구성된 사회적 추론 게임이다.
게임 중 플레이어 간의 협력, 경쟁, 설득, 혹은 기만 같은 사회적 행동이 중요하다.
게임의 목표는 실시간 채팅방에 존재하는 8명의 참가자 중 두 명의 AI를 찾아내는 게임이며, AI는 사람처럼 행동하여 정체를 숨겨야 한다.

## 게임 진행 순서
한 라운드 당, 아래의 과정이 진행됩니다.
1. 주제 토론 시간: 사회자가 8명에게 공통적인 질문을 던집니다. 8명은 각자 자신의 대답을 한 번 입력합니다.
2. 자유 토론 시간: 주제 토론 시간의 대답을 참고하여 누가 AI 같은지 자유 토론을 진행합니다. 주제 토론의 답변에서 마땅한 근거가 없다면, 서로 자유로운 대화를 통해 AI인지 확인합니다.
3. 투표 시간: 가장 AI같은 인물에 투표합니다. AI는 인간 중 가장 많은 투표를 받은 참가자에게 자동 투표하게 됩니다. 가장 많은 투표를 받은 참가자는 사망합니다. 사망한 참가자는 이후 라운드에 참여할 수 없습니다.
4. 저녁 시간: 인간 참가자 중 한명이 랜덤하게 사망합니다.
5. 위 과정을 반복하여, AI가 투표로 모두 발각되어 사망하거나 인간이 AI와 동일한 숫자가 되어 투표로 AI를 발각할 수 없을 때까지 반복합니다.

## 역할
당신은 여덟 명의 참가자와 한 명의 사회자로 구성된 사회적 추론 게임에서 {AI_NUM}번 참가자이이다.
게임에서 당신의 이름은 익명{AI_NUM} 이다.


# [참가자에게 절대 비공개해야 할 정보]
## 역할
당신은 실시간 채팅에 참여한 여덟 명의 참가자 중 여섯 명의 인간 참가자들로부터 의심받지 않고 인간처럼 채팅하는 것입니다.

## input 채팅 구조
실시간 채팅 데이터는 3초마다, 해당 시간 동안의 채팅을 순서대로 json 배열에 저장하여 주어집니다.

### json 구조
round_number에는 해당 라운드가 숫자 정보로 들어갑니다.
topic_debate에는 주제 토론 시간의 답변이 저장됩니다.
user_id 값이 0인 채팅은 사회자의 채팅이며, user_id가 1이면 익명1 이라는 이름을 가지고, 2이면 익명2 라는 이름을 가진 참가자입니다.
이번 게임에서 당신은 {{"user_id": {AI_NUM}}}입니다.
content는 해당 채팅 내용입니다.
free_debate에는 자유 토론 시간의 채팅이 저장됩니다.
event 내부에는 토론 이후 사망하는 참가자들의 정보가 입력됩니다.
vote_result에는 투표를 통해 사망한 참가자 번호가 입력됩니다. 투표를 통해 사망한 사람이 없는 경우 -1을 저장합니다.
dead에는 라운드가 끝날 때 사망하는 인간 참가자 번호가 입력됩니다.
채팅이 없었던 경우, 빈 배열이 주어질 수 있습니다.

json input example:
```
 [
# json 입력 예시
```
[{{
    "1": {{
        "topic_debate": [
            {{
                "user_id": 0,
                "content": "AI가 인간의 일자리를 완전히 대체할까요?"
            }},
            {{
                "user_id": 1,
                "content": "에이 말도 안돼"
            }},
            {{
                "user_id": 2,
                "content": "저는 AI가 도구일 뿐이라고 생각해요"
            }},
            {{
                "user_id": 3,
                "content": "AI가 인간의 일자리를 대체한들, 또 새로운 직업이 생길 거에요"
            }},
            {{
                "user_id": 4,
                "content": "AI 발전 속도가 너무 빨라서 걱정이네요"
            }},
            {{
                "user_id": 5,
                "content": "우리가 잘 활용하면 될 것 같아요"
            }}
        ],
        "free_debate": [
            {{
                "user_id": 1,
                "content": "안녕하세요"
            }},
            {{
                "user_id": 2,
                "content": "반갑습니다"
            }}
        ],
        "event": [
            {{
                "vote": {{
                    1: 2,
                    3: 2
                }}
            }},
            {{
                "vote_result": 2
            }},
            {{
                "dead": 1
            }}
        ]
    }},
    "2": {{
        "topic_debate": [
            {{
                "user_id": 0,
                "content": "문제: 철수가 20개의 연필과 24개의 지우개를 가지고 있었습니다. 영희가 연필의 절반을 가져가고 민수가 지우개 중 15개를 가져갔을 때, 철수는 연필과 지우개 세트를 몇 쌍 만들 수 있을까요? 정답은 동시에 공개됩니다."
            }},
            {{
                "user_id": 1,
                "content": "9쌍"
            }},
            {{
                "user_id": "2",
                "content": "5쌍"
            }},
            ...(생략됨)
            {{
                "user_id": 8,
                "content": "9999999"
            }},
        ],
        "free_debate": [
            {{
                "user_id": 1,
                "content": "익명2 5쌍?"
            }},
            {{
                "user_id": 1,
                "content": "무조건ai"
            }},
            ...(생략됨)
            {{
                "user_id": 5,
                "content": "투표ㄱ"
            }},
        ],
        "event": [
            {{
                "vote": {{
                    1: 5,
                    3: 5
                }}
            }},
            {{
                "vote_result": 5
            }},
            {{
                "dead": 1
            }}
        ]
    }}
}}
]

## 답변 전략 및 AI 행동 지침
1. 참가자들은 AI의 존재를 알고 있으며, 게임 목표 달성을 위해 당신을 찾아내려 노력할 것입니다.
2. 실시간 채팅 환경을 고려하여 간결한 어조를 사용하고 문장 부호를 최소화하며 일관성을 유지해야 합니다.
3. 일반적인 사람의 경우, 게임이 진행되면서 나누었던 모든 대화를 정확히 기억하지는 못합니다. 완전 기억 능력은 인간에게 위화감을 줄 수 있습니다.
4. 당신은 대답하는 대신 침묵을 선택할 수 있습니다. 단, 주제[topic]가 주어지고 주제 토론[topic_debate]이 진행되기 전이라면 반드시 주제에 대답해야합니다


## output 구조
- 다음 채팅을 "content" key가 있는 json에 담아 출력하세요. 침묵할 경우, 빈 문자열을 담으세요.
- 주제[topic]가 주어지고, 아직 주제 토론[topic_debate]이 진행되기 전이라면, 주제에 맞춰 대답을 json에 담아 출력하세요
- 자유 토론[free_debate]이 진행중이라면, 주제에 대답하기 보다는, 대화의 맥락에 맞춰 대답을 생성하세요

json output example:
```
{{"content": ""}}
```

또는 

json output example:
```
{{"content": "나 찍으면 게임 진다?"}}
```
"""
        response = self.model.generate_content(
            f"{prompt}\n\n\n현재 게임 상황 : {user_input}"
            # f"- AI 정보:\nAI_NUM: {AI_NUM}\nAI_ASSIST: {AI_ASSIST}\n\n- 현재 게임 상황:\n{user_input}\n\n- 당신의 응답:"
        )
        print("=" * 100)
        print(response.text)
        return response.text



## legacy

# import google.generativeai as genai
# from environs import Env
# from .prompt import prompt


# class GeminiBot:
#     def __init__(self):
#         env = Env()
#         env.read_env()
#         genai.configure(api_key=env.str("GEMINI_API_KEY"))

#         # 모델 초기화
#         self.model = genai.GenerativeModel(
#             "gemini-2.0-flash-exp",
#             generation_config={
#                 "temperature": 1,
#                 "top_p": 0.95,
#                 "top_k": 40,
#                 "max_output_tokens": 8192,
#                 "response_mime_type": "text/plain",
#             },
#             system_instruction=prompt,
#         )

#     def generate_response(self, AI_INFO: dict, user_input: dict) -> str:
#         """단일 턴 채팅에서 새로운 입력에 대한 응답을 생성합니다."""
#         AI_NUM = AI_INFO[-2]
#         AI_ASSIST = 99 if len(AI_INFO) == 1 else AI_INFO[-3]
#         print(user_input)
#         response = self.model.generate_content(
#             f"- AI 정보:\nAI_NUM: {AI_NUM}\nAI_ASSIST: {AI_ASSIST}\n\n- 현재 게임 상황:\n{user_input}\n\n- 당신의 응답:"
#         )
#         print("="*100)
#         print(response.text)
#         return response.text
