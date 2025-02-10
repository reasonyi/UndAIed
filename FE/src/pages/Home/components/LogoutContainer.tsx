import axios from "axios";
// import GoogleLoginButton from "./GoogleLoginBtn";
import { useSetRecoilState } from "recoil";
import { useNavigate } from "react-router";
import { userState } from "../../../store/userState";
import GoogleLoginButton2 from "./GoogleLoginBtn2";

function LogoutContainer() {
  const setUser = useSetRecoilState(userState);
  const navigate = useNavigate();

  const handleTokenReceive = async (token: string) => {
    try {
      // 서버로 토큰 전송
      const response = await axios.post("http://localhost:8080/api/v1/user", {
        token,
      });
      // 서버 응답 확인
      console.log("서버 응답:", response.data);

      // 응답이 성공인 경우
      if (response.data.isSuccess) {
        // 서버에서 내려준 유저 정보를 구조분해 할당
        const {
          token: serverToken,
          email,
          nickname,
          totalWin,
          totalLose,
        } = response.data.data;

        // Recoil userState에 사용자 정보 저장
        setUser({
          isLogin: true,
          token: serverToken,
          email,
          nickname,
          totalWin,
          totalLose,
        });

        // 신규 회원가입(201)과 기존 회원 로그인(200) 분기 처리
        if (response.status === 201) {
          // 회원가입 직후, 성별/나이 등의 추가 정보 입력을 받아야 함
          // 라우팅 예시
          navigate("/signup");

          // 혹은 모달로 처리하는 예시
          // setShowSignUpModal(true);
        } else if (response.status === 200) {
          // 이미 회원가입된 사용자이므로 바로 메인 페이지 등으로 이동
          navigate("/");
        }
      }
    } catch (error: any) {
      console.error("서버 전송 에러:", error);

      // 에러 응답 상태별로 분기 처리 가능
      if (error.response) {
        if (error.response.status === 400) {
          // 클라이언트 데이터 누락
          alert("잘못된 요청입니다. 다시 시도해주세요.");
        } else if (error.response.status === 500) {
          // 서버 내부 에러
          alert("서버 에러가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
      }
    }
  };

  return (
    <div className="flex flex-col w-full items-center">
      <button className="w-[22.5rem] h-[5.5rem] flex justify-center items-center mb-10 bg-black text-white font-mono border-2 border-[#872341] rounded-sm text-3xl font-semibold">
        GAME START
      </button>
      <GoogleLoginButton2 onTokenReceive={handleTokenReceive} />
      {/* <GoogleLoginButton onTokenReceive={handleTokenReceive} /> */}
      <button className="w-[22.5rem] h-9 border border-[#dadce0] bg-white rounded-[20px] flex items-center justify-between px-3 mt-5">
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
