from fastapi import FastAPI, status
from models import Gemini, ChatGPT, DeepSeek
from pydantic import BaseModel
from utils.parser import dialogue_parser, AI_response_parser
import logging

# 로깅 설정
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class Request(BaseModel):
    selectedAIs: list[dict[str, int]]  # [{"aiId": 1, "number": 7}, ...]
    message: str


# AI 봇 매핑
AI_BOTS = {-1: DeepSeek.DeepSeek(), -2: Gemini.GeminiBot(), -3: ChatGPT.ChatGPTBot()}

app = FastAPI()


@app.post("/api/ai/{game_id}/", status_code=status.HTTP_201_CREATED)
async def create_message(request: Request):
    selected_AI = {ai["aiId"]: ai["number"] for ai in request.selectedAIs}
    parsed_dialogue = dialogue_parser(request.message)
    response: list[dict] = []

    for ai_id, bot in AI_BOTS.items():
        if ai_id in selected_AI:
            logger.info("-"*100)  # 디버깅 용 로그
            bot_response = AI_response_parser(bot.generate_response(selected_AI, parsed_dialogue))
            logger.info(bot_response)  # 디버깅 용 로그
            response.append({"number": selected_AI[ai_id], "content": bot_response["content"]})

    return response
