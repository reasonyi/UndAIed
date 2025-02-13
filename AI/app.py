from fastapi import FastAPI
from models.Gemini import init_genimi
from datetime import datetime
from pydantic import BaseModel
from utils.dialogue_parser import dialogue_parser


app = FastAPI()
gemini_bot = init_genimi()

class message_request(BaseModel):
    message:str


@app.post("/api/ai/{room_number}/")
def create_message(room_number: int, message: message_request):
    
    user_dialogue = ...
    
    gemini_message = gemini_bot.generate_content(message.message)
    precise_timestamp = datetime.now().isoformat()
    event_log = "AI 대답 생성 완료"
    return {
        "timeStamp": f"{precise_timestamp} - {event_log}",
        "AI_code" : 1,
        "status": 200,
        "room_number": room_number,
        "message": gemini_message.text,
    }
