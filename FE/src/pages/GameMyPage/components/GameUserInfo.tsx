import { useState } from "react";
import DonutChart from "../../GameMain/components/DonutChart";
import { GameProfileEditor } from "./GameProfileEditor";
import { getPlayerIcon } from "../../Util/PlayerIcon";
import { useUserProfile } from "../../../hooks/useUserData";

export function GameUserInfo() {
  const blockStyle =
    "bg-[#0000006c] border border-[#f74a5c]/40 backdrop-blur-[12.20px] text-[#fffbfb] rounded-[5px] transition-all duration-200";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";

  const [isOpen, setIsOpen] = useState(false);
  const { data: response, isLoading, error } = useUserProfile();
  const userInfo = response?.data;
  const handleClick = () => {
    setIsOpen(!isOpen);
  };

  if (isLoading) {
    return (
      <>
        <div>로딩중</div>
      </>
    );
  }

  if (error) {
    return (
      <>
        <div>에러났어요</div>
      </>
    );
  }

  return (
    <div className="space-y-4 md:space-y-10 flex md:flex-col items-center gap-4">
      {/* User ID Section */}
      <div className="w-44 p-4 md:p-6 flex flex-col items-center justify-center">
        <div
          className={`${blockStyle} flex items-center justify-center p-1 md:h-44 md:w-44 mb-3 md:mb-4`}
        >
          <img
            src={getPlayerIcon(userInfo.profileImage)}
            className="md:w-44"
            alt="Player Icon"
          />
        </div>
        <div className="text-center text-lg md:text-xl text-white">
          {userInfo.nickname}
        </div>
      </div>

      {/* Stats Section */}
      <div className={`${blockStyle} px-6 md:px-10 pb-10 pt-8`}>
        <div className="text-center">전적</div>
        <div className="relative w-36 h-36 md:mb-10 md:mt-10 md:w-48 md:h-48 mx-auto">
          <DonutChart />
        </div>
        <div className="text-center mb-3 md:mb-10 text-sm md:text-base">
          승률 | {userInfo.totalWin}승 {userInfo.totalLose}패 |
          {" " +
            Math.round(
              (userInfo.totalWin / (userInfo.totalLose + userInfo.totalWin)) *
                10000
            ) /
              100}
          %
        </div>
        <div
          onClick={handleClick}
          className={`${blockStyle} ${blockActive} ${blockHover} hover:bg-[#f8376441] text-center py-2 px-3 md:py-2 mt-3 md:mt-4 text-sm md:text-base cursor-pointer`}
        >
          정보 수정
        </div>
      </div>

      <GameProfileEditor
        isOpen={isOpen}
        onClose={handleClick}
        userInfo={userInfo}
      />
    </div>
  );
}

export default GameUserInfo;
