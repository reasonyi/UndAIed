import google.generativeai as genai
from dotenv import dotenv_values


def init_genimi() -> genai:
    # GEMINI_API_KEY = dotenv_values(".env").get("GEMINI_API_KEY")
    GEMINI_API_KEY = dotenv_values("./AI_models/.env").get("GEMINI_API_KEY")

    genai.configure(api_key=f"{GEMINI_API_KEY}")
    
    # gemini_bot = genai.GenerativeModel("gemini-1.5-flash-8b")
    gemini_bot = genai.GenerativeModel("gemini-2.0-flash-exp")
    # gemini_bot = genai.GenerativeModel("gemini-1.5-flash")

    return gemini_bot
