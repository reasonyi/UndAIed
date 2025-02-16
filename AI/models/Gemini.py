import google.generativeai as genai
from environs import Env
# from .history import initialize_history
from .history import history
from .GeminiNoHistoryChatSession import NoHistoryChatSession


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
        )

        # 커스텀 ChatSession으로 초기화
        self.chat = NoHistoryChatSession(
            model=self.model,
            # history=initialize_history(),  # 시스템 프롬프트만 담김, 이전 대화 기억하지 않음
            history=history,  # 시스템 프롬프트만 담김, 이전 대화 기억하지 않음
            enable_automatic_function_calling=False,
        )

    def generate_response(self, user_input: dict) -> str:
        """채팅 세션 내에서 새로운 입력에 대한 응답을 생성합니다."""
        response = self.chat.send_message(
            f"현재 게임 상황:\n{user_input}\n\n당신의 응답:"
        )
        return response.text
