from fastapi import FastAPI, status
from models import Gemini, ChatGPT
from datetime import datetime
from pydantic import BaseModel
from utils.dialogue_parser import dialogue_parser
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ai 봇 생성
geminiBot = Gemini.GeminiBot()
chatGPTBot = ChatGPT.ChatGPTBot()

# 요청 모델 정의
class Request(BaseModel):
    selectedAIs: list[dict[str, int]]  # [{"aiId": 1, "number": 7}, ...]
    message: str

# app 생성
app = FastAPI()

@app.post("/api/ai/{game_id}/", status_code=status.HTTP_201_CREATED)
async def create_message(game_id: int, request: Request):
    precise_timestamp = datetime.now().isoformat()
    
    # AI 정보와 메시지 각각 로깅
    # logger.info(f"[{precise_timestamp}] Game ID: {game_id}")
    # logger.info(f"Selected AIs: {request.selectedAIs}")
    # logger.info(f"Message: {request.message}")
    
    # 응답 생성
    parsed_dialogue = dialogue_parser(request.message)
    logger.info(f"\n\n")
    # logger.info(f"--------------------------------------------------------")
    # logger.info(f"{geminiBot.chat.history}")
    # logger.info(f"--------------------------------------------------------")
    geminiBotResponse = geminiBot.generate_response(parsed_dialogue)
    # chatGPTBotResponse = ...
    
    return {
        "timeStamp": f"{precise_timestamp}",
        "answeredAI": 1,
        "gameId": game_id,
        "message": f"{geminiBotResponse}",
    }