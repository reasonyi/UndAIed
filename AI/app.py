from fastapi import FastAPI

# from dotenv import dotenv_values

app = FastAPI()
# env = dotenv_values(".env")
# print(env["OPENAI_API_KEY"])

@app.get("/")
def read_root():
    return {"Hello": "World"}


@app.get("/items/{item_id}")
def read_item(item_id: int, q: str | None = None):
    return {"item_id": item_id, "q": q}
