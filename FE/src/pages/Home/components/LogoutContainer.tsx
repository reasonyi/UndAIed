import axios from "axios";
import GoogleLoginButton from "./GoogleLoginBtn";

function LogoutContainer() {
  const handleTokenReceive = async (token: string) => {
    try {
      // 서버로 토큰 전송
      const response = await axios.post("http://localhost:8080/api/v1/user", {
        token,
      });
      // 서버에서 검증 후 필요한 로직 처리
      console.log("서버 응답:", response.data);
    } catch (error) {
      console.error("서버 전송 에러:", error);
    }
  };

  return (
    <div className="flex flex-col w-full items-center">
      <button className="w-[22.5rem] h-[5.5rem] flex justify-center items-center mb-10 bg-black text-white font-mono border-2 border-[#872341] rounded-sm text-3xl font-semibold">
        GAME START
      </button>
      <GoogleLoginButton onTokenReceive={handleTokenReceive} />
      <button className="w-[22.5rem] h-9 border border-[#dadce0] bg-white rounded-[20px] flex items-center justify-between px-3">
        <div></div>
        <div className="text-[#3c4043] text-sm font-medium font-['Roboto']">
          가입하기
        </div>
        <div></div>
      </button>
    </div>
  );
}

export default LogoutContainer;
