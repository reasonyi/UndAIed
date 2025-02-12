import React, { useState } from "react";
import settingIcon from "../../../assets/icon/setting.png";
import bellIcon from "../../../assets/icon/bell.svg";
import friendsIcon from "../../../assets/icon/friends.svg";
import SoundSettingsModal from "../../Util/Option"; // 경로는 실제 위치에 맞게 조정해주세요

function GameMainMenu() {
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  const footerButtonStyle =
    "bg-black px-3 py-1 border-t-2  hover:bg-[#4e1b26]   border-[#682d39]  hover:border-[#f93c4f] duration-300 active:border-[#531d1d] active:bg-[#3f1010]";

  const handleVolumeChange = (volume: number) => {
    // 볼륨 변경 로직 구현
    console.log("Volume changed:", volume);
  };

  return (
    <>
      <div className="flex justify-end">
        <button className={`${footerButtonStyle}  rounded-tl-sm border-l-2`}>
          <img src={bellIcon} alt="notifications" className="w-7" />
        </button>
        <button
          className={`${footerButtonStyle}`}
          onClick={() => setIsSettingsOpen(true)}
        >
          <img src={settingIcon} alt="settings" className="w-7" />
        </button>
        <button className={`${footerButtonStyle}`}>
          <img src={friendsIcon} alt="friends" className="h-7" />
        </button>
      </div>

      <SoundSettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        initialVolume={50}
        onVolumeChange={handleVolumeChange}
      />
    </>
  );
}

export default GameMainMenu;
