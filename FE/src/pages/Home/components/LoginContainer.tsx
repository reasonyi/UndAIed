import { useNavigate } from "react-router-dom";
import { IUser } from "../../../types/User";
import PlayerIcon1 from "../../../assets/player-icon/player-icon-1.svg";
import { userState } from "../../../store/userState";
import { useRecoilState, useSetRecoilState } from "recoil";
import IntroOverlay from "../../../util/IntroOverlay";
import { useState } from "react";
import Policy from "../../../pages/Policy";
import FirstSetting from "../../../util/FirstSetting";
import { setShowIntroState } from "../../../store/showState";

interface ILoginContainer {
  userInfo: IUser;
}

function LoginContainer({ userInfo }: ILoginContainer) {
  const navigate = useNavigate();
  const setUser = useSetRecoilState(userState);
  const [showIntro, setShowIntro] = useRecoilState(setShowIntroState);
  const [showPolicy, setShowPolicy] = useState(false);
  const [showSetting, setShowSetting] = useState(false);

  const checkUserPolicy = () => {
    const userPersistData = localStorage.getItem("userPersist");
    if (userPersistData) {
      const userData = JSON.parse(userPersistData);
      const userId = userData.userState.email; // 현재 로그인된 사용자의 ID

      const policyData = localStorage.getItem(`policy`);
      console.log(policyData);
      if (policyData) {
        const policyEmailData = JSON.parse(policyData);
        const policyEmail = policyEmailData.userEmail;
        console.log(policyData, "??? 뭐임");
        if (policyEmail === userId && policyEmailData.agreed) {
          console.log("뚫림");
          return true;
        } else {
          return false;
        }
      }

      return policyData ? JSON.parse(policyData).agreed : false;
    }
    return false;
  };

  // 게임 시작 버튼 클릭 핸들러
  const handleGameStart = () => {
    const policyAgreed = checkUserPolicy();

    if (!policyAgreed) {
      setShowPolicy(true);
    } else {
      setShowIntro(true);
    }
  };

  // Policy 동의 처리
  const handlePolicyAccept = () => {
    const userPersistData = localStorage.getItem("userPersist");
    if (userPersistData) {
      const userData = JSON.parse(userPersistData);
      const userId = userData.userState.email;

      localStorage.setItem(
        `policy`,
        JSON.stringify({
          userEmail: userId,
          agreed: true,
          timestamp: new Date().toISOString(),
        })
      );
    }

    setShowPolicy(false);
    setShowSetting(true);
  };

  // Policy 거부 처리
  const handlePolicyDecline = () => {
    const userPersistData = localStorage.getItem("userPersist");
    if (userPersistData) {
      const userData = JSON.parse(userPersistData);
      const userId = userData.userState.email;

      localStorage.setItem(
        `policy`,
        JSON.stringify({
          userEmail: userId,
          agreed: false,
          timestamp: new Date().toISOString(),
        })
      );
    }

    setShowPolicy(false);
  };

  // 로그아웃 처리
  const handleLogout = () => {
    setUser({
      isLogin: false,
      token: "",
      email: "",
      nickname: "",
      totalWin: 0,
      totalLose: 0,
    });
    navigate("/");
  };

  return (
    <div className="flex flex-col w-full items-center">
      {showIntro && <IntroOverlay />}
      {showSetting && <FirstSetting />}
      {showPolicy && (
        <Policy onAccept={handlePolicyAccept} onDecline={handlePolicyDecline} />
      )}
      <button
        onClick={handleGameStart}
        className="w-[22.5rem] h-[5.5rem] flex justify-center items-center mb-8 bg-black text-white font-mono border-2 border-[#872341] rounded-sm text-3xl font-semibold hover:bg-[#872341] transition-colors"
      >
        GAME START
      </button>
      <div className="w-[22.5rem] h-[10rem] flex justify-center items-center bg-black text-white border-2 border-[#872341] rounded-sm">
        <div className="w-24 h-24 flex justify-center items-center mr-3">
          <img className="w-20 h-20" src={PlayerIcon1} alt="" />
        </div>
        <div>
          <div className="flex items-end text-lg">
            <h1 className="font-semibold max-w-[10rem] truncate">
              {userInfo.nickname}님
            </h1>
            <button
              className="text-xs ml-3 border-[1px] p-1 hover:bg-[#872341] transition-colors"
              onClick={handleLogout}
            >
              로그아웃
            </button>
          </div>
          <h2 className="text-sm">{userInfo.email}</h2>
          <div className="text-xs">
            승률 | {userInfo.totalWin}승 | {userInfo.totalLose}패{" | "}
            {userInfo.totalWin + userInfo.totalLose === 0
              ? "0"
              : (
                  (userInfo.totalWin /
                    (userInfo.totalWin + userInfo.totalLose)) *
                  100
                ).toFixed(1)}
            %
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginContainer;
