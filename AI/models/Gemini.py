import google.generativeai as genai
from environs import Env
from prompt import prompt

class GeminiBot:
    def __init__(self):
        env = Env()
        env.read_env()
        GEMINI_API_KEY = env.str("GEMINI_API_KEY")
        
        genai.configure(api_key=GEMINI_API_KEY)
        self.model = genai.GenerativeModel("gemini-2.0-flash-exp")
        self.prompt = self.load_prompt()

    def load_prompt(self):
        return prompt

    def generate_response(self, user_input:dict):
        full_prompt = f"{self.prompt}\n\n현재 게임 상황:\n{user_input}\n\n당신의 응답:"
        response = self.model.generate_content(full_prompt)
        return response.text

def init_gemini():
    return GeminiBot()
