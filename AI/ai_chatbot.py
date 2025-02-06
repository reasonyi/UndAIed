from fastapi import FastAPI, WebSocket
from fastapi.templating import Jinja2Templates
from fastapi import Request
from AI_models.Gemini import init_genimi

app = FastAPI()
templates = Jinja2Templates(directory="templates")
gemini_bot = init_genimi() # gemini 모델 불러오기


@app.get("/")
async def get(request: Request):
    return templates.TemplateResponse("ai_chatbot.html", {"request": request})


# 포트번호 7070번
@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    while True:
        user_message = await websocket.receive_text()

        AI_message = gemini_bot.generate_content(user_message)
        await websocket.send_text(f"{AI_message.text}")
