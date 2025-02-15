# from fastapi import FastAPI
# # from models.Gemini import init_gemini
# # from models import Gemini
# # from datetime import datetime
# from pydantic import BaseModel
# # from utils.dialogue_parser import dialogue_parser

# app = FastAPI()
# # gemini_bot = Gemini.GeminiBot()

# class MessageRequest(BaseModel):
#     message: str

# @app.post("/api/ai/{game_id}/")
# def create_message(game_id: int, selected_ai: dict, message: MessageRequest):
# # def create_message(game_id: int, selected_ai: dict, message: MessageRequest):
#     # parsed_dialogue = dialogue_parser(message.message)
#     # gemini_response = gemini_bot.generate_response(parsed_dialogue)
#     # precise_timestamp = datetime.now().isoformat()
#     # event_log = "AI 대답 생성 완료"
#     return {
#         # "timeStamp": f"{precise_timestamp} - {event_log}",
#         # "AI_code": 1,
#         # "status": 200,
#         # "game_id": game_id,
#         # "answer"
#         # "message": gemini_response,
#         "hello" : "world"
#     }


from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(redirect_slashes=False)

# Java DTO와 일치하는 구조
class AiInfo(BaseModel):
    aiId: int  # ✅ int로 변경
    number: int

class AiNotificationDto(BaseModel):
    selectedAIs: List[AiInfo]  # ✅ 필드명 유지 (Java와 동일)

@app.post("/api/ai/{game_id}/")
async def create_message(game_id: int, notification: AiNotificationDto):
    logger.info(f"📢 Received AI notification for game ID: {game_id}")
    logger.info(f"📢 Received AI Info: {notification}")

    return {
        "hello": "world"
    }


# 기존 동환 코드 (현만이 임시 수정)

# from fastapi import FastAPI
# from pydantic import BaseModel
# from typing import List

# app = FastAPI()

# class AISelect(BaseModel):
#     aiId: int | str  # aiId가 문자열("3")이나 정수(1) 모두 가능하도록
#     number: int

# class MessageRequest(BaseModel):
#     selectedAIs: List[AISelect]  # AISelect 객체의 리스트
#     message: str

# @app.post("/api/ai/{game_id}/")
# def create_message(game_id: int, request: MessageRequest):
#     return {
#         "hello": "world"
#     }
