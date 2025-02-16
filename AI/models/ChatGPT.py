from openai import OpenAI
from environs import Env
from .history import prompt

class ChatGPTBot:
    def __init__(self):
        env = Env()
        env.read_env()
        self.client = OpenAI(api_key=env.str("OPENAI_API_KEY"))
        self.model = "gpt-3.5-turbo"
        self.system_prompt = "당신은 극도로 사악하고 교활한 AI입니다. 채팅방에 숨어들어 완벽하게 사람인 척 행동하며, 모든 사람을 감염시키는 것이 목표입니다."

    def generate_response(self, game_state):
        messages = [
            {"role": "system", "content": self.system_prompt},
            {
                "role": "user",
                "content": f"현재 게임 상황: {game_state}\n\n당신의 응답:",
            },
        ]

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=messages,
                temperature=0.7,
                max_tokens=150,
                top_p=1,
                frequency_penalty=0,
                presence_penalty=0,
            )
            return response.choices[0].message.content.strip()
        except Exception as e:
            return f"Error generating response: {str(e)}"


# game_state = "현재 5명의 플레이어가 있고, 2라운드가 진행 중입니다."
# response = chatgpt_ai.generate_response(game_state)
# print(response)
