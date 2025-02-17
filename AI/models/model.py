from environs import Env

class Model:
    # 챗봇 생성자
    def __init__(self):
        env = Env()
        env.read_env()
    
    def generate_response(self, AI_INFO:dict, user_input: dict) -> str:
        
        response = ... # 입력값이 들어가면 챗봇이 대답
        
        return response