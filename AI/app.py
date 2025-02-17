from fastapi import FastAPI, status
from models import Gemini, ChatGPT, DeepSeek
from pydantic import BaseModel
from utils.parser import dialogue_parser, parse_nunchi_status
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

    logger.info(f"[Request] Selected AIs: {request.selectedAIs}")
    logger.info(f"[Request] Message: {request.message}")

   
    selected_AI = {ai["aiId"]: ai["number"] for ai in request.selectedAIs}
    logger.info(f"[Processing] Selected AI mapping: {selected_AI}")
    parsed_dialogue = dialogue_parser(request.message)
    logger.info(f"[Processing] Parsed dialogue: {parsed_dialogue}")

    response:list[dict] = []

    ## 디버깅용 로깅
    # logger.info(f"\n\n")
    # logger.info(f"--------------------------------------------------------")
    # logger.info(f"--------------------------------------------------------")
    
    for ai_id, bot in AI_BOTS.items():
        if ai_id in selected_AI:
            logger.info(f"[AI Processing] Starting AI {ai_id}")

            bot_response = bot.generate_response(request.selectedAIs, parsed_dialogue)
            logger.info(f"[AI Response] AI {ai_id} generated response: {bot_response}")

            logger.info(bot_response)  
            # if parse_nunchi_status(bot_response):
            
            response.append({"number": selected_AI[ai_id], "content": bot_response})

            logger.info(f"[Response] Final response list: {response}")

    return response
