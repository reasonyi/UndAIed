# temporarly deprecated


import re, json
from collections import defaultdict
from fastapi import HTTPException


#######################
# def AI_response_parser(response: str) -> dict:
#     # "content"의 값을 찾는 정규식 패턴
#     pattern = r'"content"\s*:\s*"([^"]*)'  # 끝 따옴표 매칭 제거

#     # 정규식으로 매칭
#     match = re.search(pattern, response)

#     if not match:
#         return {"content": "응답을 파싱할 수 없습니다"}

#     # 매칭된 값 반환
#     return {"content": match.group(1)}

def AI_response_parser(response: str) -> dict:
    # 3) 응답 문자열에서 {} 형태의 JSON 추출
    matched_json = re.search(r"\{.*\}", response, flags=re.DOTALL)
    if not matched_json:
        # JSON 형태를 찾지 못한 경우
        return {
            "error": "No valid JSON object found in the ChatGPT response.",
            "raw_response": response
        }

    # 4) 추출된 JSON 파싱 후 'content' 필드 반환
    extracted_json_str = matched_json.group(0).strip()
    try:
        extracted_json:dict = json.loads(extracted_json_str)
    except json.JSONDecodeError as e:
        # 혹시 추출된 부분이 JSON으로 파싱되지 않는 경우 예외 처리
        raise HTTPException(
            status_code=500,
            detail=f"Extracted string is not valid JSON: {str(e)}"
        )

    final_content = extracted_json.get("content", None)
    if final_content is None:
        return {
            "error": "'content' field not found in the extracted JSON.",
            "extracted_json": extracted_json
        }

    # 최종 응답
    return {
        "content": final_content
    }



#######################


# 맨 앞의 (, 맨 뒤의 )를 제거하고, "), ("  를 구분자로 해서 스테이지 구분 후 리스트로 반환
def stage_extractor(dialogue: str) -> list:
    stages = dialogue[1:-1].split("), (")
    for stage in stages:
        stage = stage.strip()
    return stages


# 스테이지 안에서 topic 내용만 추출
def topic_extractor(stage: str) -> str:
    pattern = r"\[topic\]\s*\((.*?)\)(?:\s*\[topic_debate\])?"
    result = re.search(pattern, stage)
    topic = result.group(1) if result else ""
    return topic.strip()


# 각 토론을 추출하는 함수
def debate_extractor(stage: str) -> tuple:
    pattern = r"\[topic_debate\](.*?)(?:\[event\] \{vote\}|$)"
    match = re.search(pattern, stage)
    if not match:
        return []

    # 매칭된 내용을 | 로 분할하고 각 항목의 앞뒤 공백 제거
    debates = [debate.strip() for debate in match.group(1).split("|")]
    # 빈 문자열 제거
    debates = [debate for debate in debates if debate]
    topic_debates = debates
    free_debates = []
    for i, debate in enumerate(debates):
        if debate.startswith("[free_debate]"):
            free_debates = debates[i:]
            topic_debates = debates[:i]
            break

    return topic_debates, free_debates


# 주제토론, 자유 토론을 파싱하는 함수
def debate_parser(debates: list) -> list:
    debate_list: list = []
    for debate in debates:
        pattern = r"<(\d+)>.*?\((.*?)\)"
        match = re.search(pattern, debate)
        if match:
            debate_list.append(
                {"user_id": int(match.group(1)), "content": match.group(2)}
            )

    return debate_list


# 이벤트를 추출하는 함수
def event_extractor(stage: str) -> str:
    pattern = r"\[event\] \{vote\}.*$"
    match = re.search(pattern, stage)
    return match.group(0) if match else ""


# 이벤트를 분리해주는 함수
def event_splitter(event: str) -> list:
    events = event.strip().split("|")
    events = [event.strip() for event in events if event]
    return events


# 이벤트를 파싱하는 함수
def event_parser(events) -> list:
    angle_pattern = r"<(\d+)>"
    tilde_pattern = r"~(\d+)~"
    name_pattern = r"\((.*?)\)"

    event_list = []
    vote_dict = {}

    for event in events:
        if "{vote}" in event:
            voter = int(re.search(angle_pattern, event).group(1))
            vote = int(re.search(tilde_pattern, event).group(1))
            vote_dict[voter] = vote

        elif "{vote_result}" in event:
            target = re.search(name_pattern, event).group(1)
            for vote_event in events:
                if "{vote}" in vote_event and target in vote_event:
                    voter = int(re.search(angle_pattern, vote_event).group(1))
                    event_list = [{"vote": vote_dict}, {"vote_result": voter}]
                    break

        elif "{infection}" in event:
            target = re.search(name_pattern, event).group(1)
            for vote_event in events:
                if target in vote_event:
                    voter = int(re.search(angle_pattern, vote_event).group(1))
                    event_list.append({"dead": voter})
                    break
            break

    return event_list or ([{"vote": vote_dict}] if vote_dict else [])


# 메인 파싱 함수
def dialogue_parser(full_str: str) -> defaultdict:
    result: defaultdict = {}
    stages = stage_extractor(full_str)
    for i, stage in enumerate(stages):
        topic_debate_list = [{"user_id": 0, "content": topic_extractor(stage)}]
        topic_debates, free_debates = debate_extractor(stage)
        topic_debate_list.extend(debate_parser(topic_debates))
        free_debates_list = debate_parser(free_debates)
        event_list = event_parser(event_splitter(event_extractor(stage)))
        result[f"{i + 1}"] = {
            "topic_debate": topic_debate_list,
            "free_debate": free_debates_list,
            "event": event_list,
        }
    return result


"""([topic] (AI가 인간의 일자리를 완전히 대체할까요?) [topic_debate] {12345} [JohnDoe] <1> (에이 말도 안돼) 2025-01-30T10:05:23 | {67890} [kdh] <2> (저는 AI가 도구일 뿐이라고 생각해요) 2025-01-30T10:06:15 | {11111} [Mike] <3> (AI가 인간의 일자리를 대체한들, 또 새로운 직업이 생길 거에요) 2025-01-30T10:07:30 | {12345} [sunny] <4> (AI 발전 속도가 너무 빨라서 걱정이네요) 2025-01-30T10:15:23 | {67890} [yuuri] <5> (우리가 잘 활용하면 될 것 같아요) 2025-01-30T10:16:45 | [free_debate] {1001} [JohnDoe] <1> (안녕하세요) 2025-02-14T10:00:00 | {1002} [kdh] <2> (반갑습니다) 2025-02-14T10:00:05 | [event] {vote} [JohnDoe] <1> (kdh) ~2~ 2024-02-14T15:30:45.123 | {vote} [Mike] <3> (kdh) ~2~ 2024-02-14T15:30:48.456 | {vote_result} [null] <null> (Mike) ~3~ 2024-02-14T15:31:15.012 | {infection} [null] <null> (JohnDoe) ~1~ 2024-02-14T15:31:00.789 | )"""
