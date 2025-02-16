from openai import OpenAI
from environs import Env
from .history import history, prompt


class ChatGPTBot:
    def __init__(self):
        env = Env()
        env.read_env()
        self.client = OpenAI(api_key=env.str("OPENAI_API_KEY"))
        self.model = "gpt-4o-mini"
        self.system_prompt = prompt

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
