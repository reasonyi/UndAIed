import { Link } from "react-router-dom";
import playerIcon from "../../../assets/player-icon/player-icon-1.svg";

function GameSidebar() {
  const blockStyle =
    "bg-[#5349507a] border border-[#f74a5c]/60 backdrop-blur-[12.20px] text-[#fffbfb]  rounded-[5px]  transition-all duration-200 ";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";
  const footerButtonStyle =
    "bg-black px-3 py-1 border-t-2  hover:bg-[#4e1b26]   border-[#682d39]  hover:border-[#f93c4f] duration-300 active:border-[#531d1d] active:bg-[#3f1010]";

  return (
    <aside
      className={` mx-8 w-80 ${blockStyle}  flex flex-col items-center  bg-[#0000008f] hidden md:flex`}
    >
      <div className="w-32 h-32 mt-24 mb-3 flex items-center justify-center border border-[#f74a5c]/60">
        <img src={`${playerIcon}`} alt="" />
      </div>
      <div className="w-32 flex items-center justify-center text-[#fcfafa]">
        player 닉네임
      </div>

      <div className="mt-62">
        <div>내 전적</div>
      </div>

      <div className="mt-auto w-full flex flex-col items-center mb-8">
        <button
          className={`${blockStyle} ${blockHover} ${blockActive} w-72 h-10    bg-[#281919]`}
        >
          blank
        </button>
        <Link to="/gamemypage">
          <button
            className={`${blockStyle} ${blockHover} ${blockActive} w-72 h-10 mt-4  bg-[#281919]`}
          >
            내 정보
          </button>
        </Link>
      </div>
    </aside>
  );
}

export default GameSidebar;
