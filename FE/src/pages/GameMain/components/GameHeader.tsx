import power from "../../../assets/icon/power.png";
import logo from "../../../assets/svg-icon/game_logo.svg";
function GameHeader() {
  return (
    <header className="p-4 md:p-6  border-black flex justify-between items-center px-6">
      <img src={logo} alt="로고" className="h-6" />
      <button className="px-2 py-1 rounded-lg duration-300 hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[#F74A5C]">
        <img src={power} alt="" />
      </button>
    </header>
  );
}

export default GameHeader;
