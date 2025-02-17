import re


def parse_debate(debate_content: str) -> list[dict]:
    """debate 내용을 파싱하는 공통 함수"""
    debates = debate_content.strip().split("|")
    parsed_debates = []

    for debate in debates:
        debate = debate.strip()
        if debate:
            user_pattern = r"\{(\d+)\}\s*<[^>]+>\s*\(([^)]+)\)"
            user_match = re.search(user_pattern, debate)
            if user_match:
                parsed_debates.append(
                    {
                        "user_id": user_match.group(1),
                        "content": user_match.group(2),
                    }
                )
    return parsed_debates


def parse_topic(dialogue: str) -> str:
    """topic 섹션 파싱"""
    topic_pattern = r"\[topic\]\s*\((.*?)\)"
    topic_match = re.search(topic_pattern, dialogue)
    return topic_match.group(1) if topic_match else ""


def parse_topic_debate(dialogue: str) -> list[dict]:
    """topic_debate 섹션 파싱"""
    topic_debate_pattern = r"\[topic_debate\](.*?)\[free_debate\]"
    topic_debate_match = re.search(topic_debate_pattern, dialogue)
    if topic_debate_match:
        return parse_debate(topic_debate_match.group(1))
    return []


def parse_free_debate(dialogue: str) -> list[dict]:
    """free_debate 섹션 파싱"""
    free_debate_pattern = r"\[free_debate\](.*?)\[event\]"
    free_debate_match = re.search(free_debate_pattern, dialogue)
    if free_debate_match:
        return parse_debate(free_debate_match.group(1))
    return []


def parse_event(dialogue: str) -> list[dict]:
    """
    event 섹션 파싱
    vote의 경우 {유저:타겟} 구조의 딕셔너리 배열,
    execute, dead의 경우 타겟값을 정수로 반환
    """
    event_pattern = r"\[event\]\s*\{(.*?)\}\s*(?:<([^>]+)>)?\s*\(([^)]+)\)"
    event_matches = re.finditer(event_pattern, dialogue)

    result = []
    vote_dict = {"vote": []}
    has_vote = False

    for match in event_matches:
        event_type = match.group(1)
        user = match.group(2)  # vote인 경우에만 사용
        target = match.group(3).replace("타겟", "")  # "타겟1" -> "1"

        if event_type == "vote":
            has_vote = True
            # 유저 ID 추출 (예: "인게임 1번유저" -> "1")
            user_id = re.search(r"(\d+)번유저", user).group(1)
            vote_dict["vote"].append({user_id: target})
        elif event_type in ["execute", "dead"]:
            result.append({event_type: target})

    # vote 딕셔너리가 있는 경우에만 추가
    if has_vote:
        result.insert(0, vote_dict)

    return result


def parse_round(round_str: str) -> tuple:
    """각 라운드 문자열 파싱"""
    round_num = round_str[1]
    round_dialogue = round_str.strip()[4:-1]
    return round_num, round_dialogue


def dialogue_parser(dialogue: str) -> dict:
    """메인 파싱 함수"""
    round_list = dialogue.strip()[1:-1].split("), (")
    round_dict = {}

    for round_str in round_list:
        round_num, round_dialogue = parse_round(round_str)
        round_dict[round_num] = {
            "topic": parse_topic(round_dialogue),
            "topic_debate": parse_topic_debate(round_dialogue),
            "free_debate": parse_free_debate(round_dialogue),
            "event": parse_event(round_dialogue),
        }

    return round_dict




def parse_nunchi_status(text:str) -> bool:
    # "nunchi": "X" 또는 "nunchi": "O" 패턴을 찾는 정규식
    pattern = r'"nunchi":\s*"(O|)"'
    
    # 정규식으로 매칭 찾기
    match = re.search(pattern, text)
    
    # 해당 AI가 대화에 참여해야한다면 True를 반환
    if match:
        return match.group(1) == "O"
    return False