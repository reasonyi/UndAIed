from fastapi import FastAPI, WebSocket
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from fastapi import Request

app = FastAPI()
templates = Jinja2Templates(directory="templates")


@app.get("/")
async def get(request: Request):
    return templates.TemplateResponse("websocket.html", {"request": request})


@app.websocket("/ws")
# @app.websocket("/ws://localhost:9090") << 이거 손대봤자임, html쪽에서 포트번호를 조정해야함함
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    while True:
        data = await websocket.receive_text()
        await websocket.send_text(f"Message text was {data}")
