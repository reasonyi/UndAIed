from fastapi import FastAPI
# from models.Gemini import init_gemini
from models import Gemini
from datetime import datetime
from pydantic import BaseModel
from utils.dialogue_parser import dialogue_parser

app = FastAPI()
# gemini_bot = Gemini.GeminiBot()

class MessageRequest(BaseModel):
    message: str

@app.post("/api/ai/{game_id}/")
def create_message(game_id: int, selected_ai: dict, message: MessageRequest):
    # parsed_dialogue = dialogue_parser(message.message)
    # gemini_response = gemini_bot.generate_response(parsed_dialogue)
    # precise_timestamp = datetime.now().isoformat()
    # event_log = "AI 대답 생성 완료"
    return {
        # "timeStamp": f"{precise_timestamp} - {event_log}",
        # "AI_code": 1,
        # "status": 200,
        # "game_id": game_id,
        # "answer"
        # "message": gemini_response,
        "hello" : "world"
    }
