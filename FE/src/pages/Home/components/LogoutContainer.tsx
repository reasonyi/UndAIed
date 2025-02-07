import GoogleIcon from "../../../assets/svg-icon/google_logo.svg";

function LogoutContainer() {
  return (
    <div className="flex flex-col w-full items-center">
      <button className="w-[22.5rem] h-[5.5rem] flex justify-center items-center mb-10 bg-black text-white font-mono border-2 border-[#872341] rounded-sm text-3xl font-semibold">
        GAME START
      </button>
      <button className="w-[22.5rem] h-9 border border-[#dadce0] bg-white rounded-[20px] flex items-center justify-between px-3 mb-5">
        <img src={GoogleIcon} alt="" />
        <div className="text-[#3c4043] text-sm font-medium font-['Roboto']">
          Google로 로그인
        </div>
        <div className="w-4 h-[1px]"></div>
      </button>
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
