from fastapi import FastAPI

# from dotenv import dotenv_values

app = FastAPI()
# env = dotenv_values(".env")
# print(env["OPENAI_API_KEY"])

@app.get("/api/ai/")  # "/" -> "/api/ai/"로 변경
def read_root():
    return {"Hello": "World"}

@app.get("/api/ai/items/{item_id}")  # "/items/" -> "/api/ai/items/"로 변경
def read_item(item_id: int, q: str | None = None):
    return {"item_id": item_id, "q": q}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
