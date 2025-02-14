import re
import json


def dialogue_parser(dialogue: str) -> dict:
    round_list = dialogue.strip()[1:-1].split("), (")
    round_dict = {}

    for round_str in round_list:
        round_num = round_str[1]
        round_dialogue = round_str.strip()[4:-1]
        round_dict[round_num] = {}

        # [topic] 섹션 파싱
        topic_pattern = r"\[topic\]\s*\((.*?)\)"
        topic_match = re.search(topic_pattern, round_dialogue)
        if topic_match:
            round_dict[round_num]["topic"] = topic_match.group(1)

        # [topic_debate] 섹션 파싱
        topic_debate_pattern = r"\[topic_debate\](.*?)\[free_debate\]"
        topic_debate_match = re.search(topic_debate_pattern, round_dialogue)
        if topic_debate_match:
            debates = topic_debate_match.group(1).strip().split("|")
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
            round_dict[round_num]["topic_debate"] = parsed_debates

        # [free_debate] 섹션 파싱
        free_debate_pattern = r"\[free_debate\](.*?)\[event\]"
        free_debate_match = re.search(free_debate_pattern, round_dialogue)
        if free_debate_match:
            debates = free_debate_match.group(1).strip().split("|")
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
            round_dict[round_num]["free_debate"] = parsed_debates

    return json.dumps(round_dict, ensure_ascii=False, indent=2)
