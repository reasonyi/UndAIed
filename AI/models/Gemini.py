import google.generativeai as genai
from environs import Env
from .prompt import prompt


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
            system_instruction=prompt,
        )

    def generate_response(self, AI_INFO: dict, user_input: dict) -> str:
        """채팅 세션 내에서 새로운 입력에 대한 응답을 생성합니다."""
        AI_NUM = AI_INFO[0]["number"]
        AI_ASSIST = AI_INFO[1]["number"]
        response = self.model.generate_content(
            f"- AI 정보 : \nAI_NUM : {AI_NUM}\nAI_ASSIST:{AI_ASSIST}\n\n- 현재 게임 상황:\n{user_input}\n\n- 당신의 응답:"
        )
        return response.text
