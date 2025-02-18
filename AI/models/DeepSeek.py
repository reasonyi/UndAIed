from environs import Env
# 미구현

class DeepSeek:
    def __init__(self):
        env = Env()
        env.read_env()
        ...

    def generate_response(self, AI_INFO: dict, user_input: dict) -> str: 
        """채팅 세션 내에서 새로운 입력에 대한 응답을 생성합니다."""
        AI_NUM = AI_INFO[0]["number"]
        AI_ASSIST = AI_INFO[1]["number"]
        response = ""
        # deepseek api에 맞춘 대답
        # response = self.model.generate_content(
        #     f"- AI 정보 : \nAI_NUM : {AI_NUM}\nAI_ASSIST:{AI_ASSIST}\n\n- 현재 게임 상황:\n{user_input}\n\n- 당신의 응답:"
        # )
        return response