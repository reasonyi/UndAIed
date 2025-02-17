import Setting from "../../../util/Setting";

function GameMainMenu() {
  const footerButtonStyle =
    "bg-black px-3 py-1 border-t-2  hover:bg-[#4e1b26]   border-[#682d39]  hover:border-[#f93c4f] duration-300 active:border-[#531d1d] active:bg-[#3f1010]";

  return (
    <>
      <div className="flex justify-end">
        {/* <button className={`${footerButtonStyle}  rounded-tl-sm border-l-2`}>
          <img src={bellIcon} alt="notifications" className="w-7" />
        </button> */}
        <div className={`${footerButtonStyle} border-l-2`}>
          <Setting title="게임 설정"></Setting>
        </div>
        {/* <button className={`${footerButtonStyle}`}>
          <img src={friendsIcon} alt="friends" className="h-7" />
        </button> */}
      </div>
    </>
  );
}

export default GameMainMenu;
