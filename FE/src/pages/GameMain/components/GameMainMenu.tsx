import settingIcon from "../../../assets/icon/setting.png";
import bellIcon from "../../../assets/icon/bell.svg";
import friendsIcon from "../../../assets/icon/friends.svg";

function GameMainMenu() {
  const footerButtonStyle =
    "bg-black px-3 py-1 border-t-2  hover:bg-[#4e1b26]   border-[#682d39]  hover:border-[#f93c4f] duration-300 active:border-[#531d1d] active:bg-[#3f1010]";

  return (
    <div className="flex justify-end">
      <button className={`${footerButtonStyle}  rounded-tl-sm border-l-2`}>
        <img src={bellIcon} alt="icon" className="w-7" />
      </button>
      <button className={`${footerButtonStyle}`}>
        <img src={settingIcon} alt="icon" className="w-7" />
      </button>
      <button className={`${footerButtonStyle}`}>
        <img src={friendsIcon} alt="icon" className="h-7" />
      </button>
    </div>
  );
}

export default GameMainMenu;
