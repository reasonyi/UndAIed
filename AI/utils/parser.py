import re
from collections import defaultdict


#######################
def AI_response_parser(response: str) -> dict:
    """AI의 대답을 파싱하는 함수"""
    # {} 안의 내용을 찾는 정규식 패턴
    pattern = r"{(.*?)}"

    # 정규식으로 {} 안의 내용 추출
    match = re.search(pattern, response)
    if not match:
        return None

    content = match.group(1)

    # key:value 쌍을 찾는 패턴
    pair_pattern = r'(\w+)\s*:\s*"([^"]*)"'

    # 모든 key:value 쌍을 찾아서 딕셔너리로 변환
    pairs = re.findall(pair_pattern, content)
    result = {key: value for key, value in pairs}

    return result


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


# 주제토론, 자유 토론을 파싱하는 함수수
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
