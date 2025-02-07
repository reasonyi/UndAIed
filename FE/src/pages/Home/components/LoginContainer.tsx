import { Link } from "react-router";
import { IUser } from "../../../types/User";
import PlayerIcon1 from "../../../assets/player-icon/player-icon-1.svg";

interface ILoginContainer {
  userInfo: IUser;
}

function LoginContainer({ userInfo }: ILoginContainer) {
  return (
    <div className="flex flex-col w-full items-center">
      <button className="w-[22.5rem] h-[5.5rem] flex justify-center items-center mb-8 bg-black text-white font-mono border-2 border-[#872341] rounded-sm text-3xl font-semibold">
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

            <button className="text-xs ml-3 border-[1px] p-1">로그아웃</button>
          </div>
          <h2 className="text-sm">{userInfo.email}</h2>
          <div className="text-xs">
            승률 | {userInfo.totalWin}승 | {userInfo.totalLose}패{" | "}
            {userInfo.totalWin + userInfo.totalLose === 0
              ? "0"
              : (
                  userInfo.totalWin / userInfo.totalWin +
                  userInfo.totalLose
                ).toString()}
            %
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginContainer;
