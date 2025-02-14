import google.generativeai as genai
from environs import Env

def init_genimi() -> genai:
    env = Env()
    env.read_env()
    GEMINI_API_KEY = env.str("GEMINI_API_KEY")
    # GEMINI_API_KEY = dotenv_values("./AI_models/.env").get("GEMINI_API_KEY")

    genai.configure(api_key=f"{GEMINI_API_KEY}")
    
    # gemini_bot = genai.GenerativeModel("gemini-1.5-flash-8b")
    gemini_bot = genai.GenerativeModel("gemini-2.0-flash-exp")
    # gemini_bot = genai.GenerativeModel("gemini-1.5-flash")

    return gemini_bot
