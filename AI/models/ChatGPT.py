from openai import OpenAI
from openai.types.chat import ChatCompletion
from openai.types.chat.completion_create_params import CompletionCreateParams
from environs import Env
from .prompt import prompt


class ChatGPTBot:
    def __init__(self):
        env = Env()
        env.read_env()

        # OpenAI 클라이언트 초기화
        self.client = OpenAI(api_key=env.str("OPENAI_API_KEY"))

        # 모델 설정 구성
        self.config = {
            "model": "gpt-4o-mini",
            "temperature": 0.7,
            "max_tokens": 150,
            "top_p": 1,
            "frequency_penalty": 0,
            "presence_penalty": 0,
        }
        self.system_prompt = prompt

    def generate_response(self, AI_INFO: dict, user_input: dict) -> str:
        """단일 턴 채팅에서 새로운 입력에 대한 응답을 생성합니다."""
        AI_NUM = AI_INFO[0]["number"]
        AI_ASSIST = AI_INFO[1]["number"]

        messages = [
            {"role": "system", "content": self.system_prompt},
            {
                "role": "user",
                "content": f"- AI 정보 : \nAI_NUM : {AI_NUM}\nAI_ASSIST:{AI_ASSIST}\n\n- 현재 게임 상황:\n{user_input}\n\n- 당신의 응답:",
            },
        ]

        response: ChatCompletion = self.client.chat.completions.create(
            messages=messages, **self.config
        )
        return response.choices[0].message.content.strip()
