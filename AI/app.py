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
class AINumberDto(BaseModel):
    aiId: int
    number: int

class AIInputDataDto(BaseModel):
    selectedAIs: list[AINumberDto]
    message: str

app = FastAPI()

@app.post("/api/ai/{game_id}/", status_code=status.HTTP_201_CREATED)
async def create_message(game_id: int, request: AIInputDataDto):
    # 요청에서 AI 번호들 추출
    ai_numbers = [ai.number for ai in request.selectedAIs]
    
    # 간단한 응답 생성
    responses = []
    for ai_info in request.selectedAIs:
        responses.append({
            "number": ai_info.number,
            "content": f"AI-{ai_info.number}의 테스트 응답입니다"
        })
    
    return responses