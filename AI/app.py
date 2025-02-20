import json, re
import google.generativeai as genai
from fastapi import FastAPI, status, HTTPException
from pydantic import BaseModel
from openai import OpenAI
from environs import Env
from models.prompt import load_prompt

app = FastAPI()
env = Env()
env.read_env()


class Request(BaseModel):
    ai_num: int
    ai_assist: int
    message: dict


@app.post("/api/ai/{game_id}/", status_code=status.HTTP_200_OK)
async def create_message(request: Request):
    return request.message


@app.post("/api/chatgpt/{game_id}", status_code=status.HTTP_201_CREATED)
async def chatgpt_api(request: Request):
    #     # 1) request로 전달된 문자열(JSON 형식) 파싱
    #     try:
    #         parsed_request_data = json.loads(request.message)
    #     except json.JSONDecodeError as e:
    #         raise HTTPException(
    #             status_code=400, detail=f"Request message is not valid JSON: {str(e)}"
    #         )

    # 환경변수 로딩 및 클라이언트 생성
    client = OpenAI(api_key=env.str("OPENAI_API_KEY"))

    # client 초기 설정
    # ...

    # user_prompt와 round 번호
    game_progress = str(request.message)
    round = int(max(request.message.keys()))
    phase:dict = request.message[f"{round}"]
    topic = phase.get("topic")  # 현재 라운드의 주제
    topic_debate = phase.get("topic_debate")  # 현재 라운드의 주제토론 내용
    # free_debate = phase["free_debate"]  # 현재 라운드의 자유토론 내용
    event = phase.get("event")

    if not topic:
        return {"content": "주제가 아직 주어지지 않았습니다.", "flag": False}
    elif not topic_debate:
        current_situation = f"현재 {round}라운드 주제토론[topic_debate] 시간입니다. output 구조에 맞춰서 주제에 대한 대답을 반드시 생성하세요.\n이번 라운드 주제 : {topic}"
    else:
        current_situation = f"현재 {round}라운드 자유토론[free_debate] 시간입니다. output 구조에 맞춰서 대답을 생성하세요"

    try:
        # 2) ChatGPT API 호출 및 응답
        response = (
            client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "system", "content": load_prompt(request.ai_num, request.ai_assist)},
                    {
                        "role": "user",
                        "content": f"- 게임 진행 상황:\n{game_progress}\n\n- 현재 상황:\n{current_situation}",
                    },
                ],
                response_format={"type": "json_object"},
                temperature=1
            )
            .choices[0]
            .message.content
        )
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Error calling OpenAI API: {str(e)}"
        )
    # 3) 응답 문자열에서 {} 형태의 JSON 추출
    matched_json = re.search(r"\{.*\}", response, flags=re.DOTALL)
    if not matched_json:
        # JSON 형태를 찾지 못한 경우
        return {
            "error": "No valid JSON object found in the ChatGPT response.",
            "raw_response": response,
        }

    # 4) 추출된 JSON 파싱 후 'content' 필드 반환
    extracted_json_str = matched_json.group(0).strip()
    try:
        extracted_json: dict = json.loads(extracted_json_str)
    except json.JSONDecodeError as e:
        # 혹시 추출된 부분이 JSON으로 파싱되지 않는 경우 예외 처리
        raise HTTPException(
            status_code=500, detail=f"Extracted string is not valid JSON: {str(e)}"
        )

    final_content = extracted_json.get("content", None)
    if final_content is None:
        return {
            "error": "'content' field not found in the extracted JSON.",
            "extracted_json": extracted_json,
        }

    # 최종 응답
    return {"content": final_content}


@app.post("/api/gemini/{game_id}", status_code=status.HTTP_201_CREATED)
async def gemini_api(request: Request):
    #     # 1) request로 전달된 문자열(JSON 형식) 파싱
    #     try:
    #         parsed_request_data = json.loads(request.message)
    #     except json.JSONDecodeError as e:
    #         raise HTTPException(
    #             status_code=400, detail=f"Request message is not valid JSON: {str(e)}"
    #         )

    # 환경변수 로딩 및 클라이언트 생성
    genai.configure(api_key=env.str("GEMINI_API_KEY"))

    # client 초기 설정
    client = genai.GenerativeModel(
        "gemini-2.0-flash-exp",
        generation_config={
            "temperature": 1,
            "top_p": 0.95,
            "top_k": 40,
            "max_output_tokens": 8192,
            "response_mime_type": "application/json",
        },
        system_instruction=load_prompt(request.ai_num),
    )

    # user_prompt와 round 번호
    game_progress = str(request.message)
    round = int(max(request.message.keys()))
    phase:dict = request.message[f"{round}"]
    topic = phase.get("topic")  # 현재 라운드의 주제
    topic_debate = phase.get("topic_debate")  # 현재 라운드의 주제토론 내용
    # free_debate = phase["free_debate"]  # 현재 라운드의 자유토론 내용
    event = phase.get("event")

    if not topic:
        return {"content": "주제가 아직 주어지지 않았습니다.", "flag": False}
    elif not topic_debate:
        current_situation = f"현재 {round}라운드 주제토론[topic_debate] 시간입니다. output 구조에 맞춰서 주제에 대한 대답을 반드시 생성하세요.\n이번 라운드 주제 : {topic}"
    elif not event:
        current_situation = f"현재 {round}라운드 자유토론[free_debate] 시간입니다. output 구조에 맞춰서 대답을 생성하세요"

    try:
        # 2) gemini API 호출 및 응답
        response = client.generate_content(
            f"- 게임 진행 상황:\n{game_progress}\n\n- 현재 상황:\n{current_situation}"
        ).text
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Error calling OpenAI API: {str(e)}"
        )

    # 3) 응답 문자열에서 {} 형태의 JSON 추출
    matched_json = re.search(r"\{.*\}", response, flags=re.DOTALL)
    if not matched_json:
        # JSON 형태를 찾지 못한 경우
        return {
            "error": "No valid JSON object found in the gemini response.",
            "raw_response": response,
        }

    # 4) 추출된 JSON 파싱 후 'content' 필드 반환
    extracted_json_str = matched_json.group(0).strip()
    try:
        extracted_json: dict = json.loads(extracted_json_str)
    except json.JSONDecodeError as e:
        # 혹시 추출된 부분이 JSON으로 파싱되지 않는 경우 예외 처리
        raise HTTPException(
            status_code=500, detail=f"Extracted string is not valid JSON: {str(e)}"
        )

    final_content = extracted_json.get("content", None)
    if final_content is None:
        return {
            "error": "'content' field not found in the extracted JSON.",
            "extracted_json": extracted_json,
        }

    # 최종 응답
    return {"content": final_content}


"""
{
    "content": "어휴.. 제가 뭘 그렇게 잘못했습니까"
}
"""


"""
어째서인지 리팩토링 전이 대답이 더 기깔나는 것 같다
"""


# # 실험적 리팩토링

# import json
# import re
# from typing import Dict, Optional

# import google.generativeai as genai
# from fastapi import FastAPI, HTTPException, status
# from openai import OpenAI
# from pydantic import BaseModel
# from environs import Env
# from models.prompt import load_prompt

# app = FastAPI()
# env = Env()
# env.read_env()


# class Request(BaseModel):
#     ai_num: int
#     ai_assist: int
#     message: dict


# class GamePhase:
#     def __init__(self, phase_data: Dict):
#         self.topic = phase_data.get("topic")
#         self.topic_debate = phase_data.get("topic_debate")
#         self.event = phase_data.get("event")


# def get_current_situation(round_num: int, phase: GamePhase) -> Optional[str]:
#     """현재 게임 상황에 따른 메시지를 반환합니다."""
#     if not phase.topic:
#         return None
#     elif not phase.topic_debate:
#         return f"현재 {round_num}라운드 주제토론[topic_debate] 시간입니다. output 구조에 맞춰서 주제에 대한 대답을 반드시 생성하세요.\n이번 라운드 주제 : {phase.topic}"
#     elif not phase.event:
#         return f"현재 {round_num}라운드 자유토론[free_debate] 시간입니다. output 구조에 맞춰서 대답을 생성하세요"
#     return None


# def extract_json_content(response: str) -> Dict:
#     """API 응답에서 JSON 컨텐츠를 추출합니다."""
#     matched_json = re.search(r"\{.*\}", response, flags=re.DOTALL)
#     if not matched_json:
#         raise HTTPException(
#             status_code=500,
#             detail={
#                 "error": "No valid JSON object found in the response.",
#                 "raw_response": response,
#             }
#         )

#     try:
#         extracted_json: dict = json.loads(matched_json.group(0).strip())
#     except json.JSONDecodeError as e:
#         raise HTTPException(
#             status_code=500,
#             detail=f"Extracted string is not valid JSON: {str(e)}"
#         )

#     final_content = extracted_json.get("content")
#     if final_content is None:
#         raise HTTPException(
#             status_code=500,
#             detail={
#                 "error": "'content' field not found in the extracted JSON.",
#                 "extracted_json": extracted_json,
#             }
#         )

#     return {"content": final_content}


# @app.post("/api/chatgpt/{game_id}", status_code=status.HTTP_201_CREATED)
# async def chatgpt_api(request: Request):
#     #     # 1) request로 전달된 문자열(JSON 형식) 파싱
#     #     try:
#     #         parsed_request_data = json.loads(request.message)
#     #     except json.JSONDecodeError as e:
#     #         raise HTTPException(
#     #             status_code=400, detail=f"Request message is not valid JSON: {str(e)}"
#     #         )

#     # 환경변수 로딩 및 클라이언트 생성
#     client = OpenAI(api_key=env.str("OPENAI_API_KEY"))

#     # client 초기 설정
#     # ...

#     # 게임 상태 분석
#     game_progress = str(request.message)
#     round_num = int(max(request.message.keys()))
#     phase = GamePhase(request.message[f"{round_num}"])

#     current_situation = get_current_situation(round_num, phase)
#     if current_situation is None:
#         return {"content": "주제가 아직 주어지지 않았습니다.", "flag": False}

#     try:
#         # ChatGPT API 호출 및 응답
#         response = (
#             client.chat.completions.create(
#                 model="gpt-4o-mini",
#                 messages=[
#                     {"role": "system", "content": load_prompt(request.ai_num)},
#                     {
#                         "role": "user",
#                         "content": f"- 게임 진행 상황:\n{game_progress}\n\n- 현재 상황:\n{current_situation}",
#                     },
#                 ],
#                 response_format={"type": "json_object"},
#             )
#             .choices[0]
#             .message.content
#         )
#     except Exception as e:
#         raise HTTPException(
#             status_code=500,
#             detail=f"Error calling OpenAI API: {str(e)}"
#         )

#     return extract_json_content(response)


# @app.post("/api/gemini/{game_id}", status_code=status.HTTP_201_CREATED)
# async def gemini_api(request: Request):
#     #     # 1) request로 전달된 문자열(JSON 형식) 파싱
#     #     try:
#     #         parsed_request_data = json.loads(request.message)
#     #     except json.JSONDecodeError as e:
#     #         raise HTTPException(
#     #             status_code=400, detail=f"Request message is not valid JSON: {str(e)}"
#     #         )

#     # 환경변수 로딩 및 클라이언트 생성
#     genai.configure(api_key=env.str("GEMINI_API_KEY"))

#     # Gemini 클라이언트 설정
#     client = genai.GenerativeModel(
#         "gemini-2.0-flash-exp",
#         generation_config={
#             "temperature": 1,
#             "top_p": 0.95,
#             "top_k": 40,
#             "max_output_tokens": 8192,
#             "response_mime_type": "application/json",
#         },
#         system_instruction=load_prompt(request.ai_num),
#     )

#     # 게임 상태 분석
#     game_progress = str(request.message)
#     round_num = int(max(request.message.keys()))
#     phase = GamePhase(request.message[f"{round_num}"])

#     current_situation = get_current_situation(round_num, phase)
#     if current_situation is None:
#         return {"content": "주제가 아직 주어지지 않았습니다.", "flag": False}

#     try:
#         # Gemini API 호출 및 응답
#         response = client.generate_content(
#             f"- 게임 진행 상황:\n{game_progress}\n\n- 현재 상황:\n{current_situation}"
#         ).text
#     except Exception as e:
#         raise HTTPException(
#             status_code=500,
#             detail=f"Error calling Gemini API: {str(e)}"
#         )

#     return extract_json_content(response)


# """
# {
#     "content": "어휴.. 제가 뭘 그렇게 잘못했습니까"
# }
# """
