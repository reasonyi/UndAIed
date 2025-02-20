import React, { useEffect } from "react";
import GoogleIcon from "../../../assets/svg-icon/google_logo.svg";

interface GoogleLoginButtonProps {
  onTokenReceive: (token: string) => void;
}

const GoogleLoginButton: React.FC<GoogleLoginButtonProps> = ({
  onTokenReceive,
}) => {
  // 구글에서 로그인이 완료되었을 때 ID 토큰을 받아오는 콜백
  const handleCallbackResponse = (
    response: google.accounts.id.CredentialResponse
  ) => {
    const token = response.credential;
    onTokenReceive(token);
  };

  useEffect(() => {
    // google.accounts.id.initialize 설정
    (window as any).google?.accounts.id.initialize({
      client_id:
        "795412424002-h96s44dm1b7junqvntu384qk2otab42n.apps.googleusercontent.com",
      callback: handleCallbackResponse,
      auto_select: false, // 자동 One Tap을 비활성화하여 수동으로 호출
    });
  }, []);

  // 버튼 클릭 시 로그인 과정을 트리거
  const handleLoginClick = () => {
    (window as any).google?.accounts.id.prompt();
    // prompt()가 호출되면, 구글 로그인 팝업(또는 One Tap)이 뜨고,
    // 로그인 성공 시 handleCallbackResponse가 실행됩니다.
  };

  return (
    <button
      onClick={handleLoginClick}
      className="w-[22.5rem] h-9 border border-[#dadce0] bg-white rounded-[20px] flex items-center justify-between px-3 mb-5"
    >
      <img src={GoogleIcon} alt="" />
      <div className="text-[#3c4043] text-sm font-medium font-['Roboto']">
        Google 계정으로 로그인
      </div>
      <div className="w-4 h-[1px]"></div>
    </button>
  );
};

export default GoogleLoginButton;
