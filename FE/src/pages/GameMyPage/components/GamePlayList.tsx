import { useState } from "react";
import leftIcon from "../../../assets/icon/left.svg";
import rightIcon from "../../../assets/icon/right.svg";
import { useUserProfile } from "../../../hooks/useUserData";
import { Game } from "../../../types/User";
import GamePlayDetail from "./GamePlayDetail";

const customScrollbarStyle = `
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c60f2188;
  border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #ff6b7d;
}
`;

function MyPlayList() {
  const [selectedGame, setSelectedGame] = useState<Game | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const blockStyle =
    "bg-[#0000006c] border border-[#f74a5c]/40 backdrop-blur-[12.20px] text-[#fffbfb] rounded-[5px] transition-all duration-200";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";

  const { data: response, isLoading, error } = useUserProfile();
  const gameList = response?.data.game;

  const handleGameClick = (game: Game) => {
    setSelectedGame(game);
    setIsModalOpen(true);
  };

  if (isLoading) return <div>로딩중</div>;
  if (error) return <div>에러났어요</div>;
  return (
    <>
      <style>{customScrollbarStyle}</style>
      <div className="p-4 md:p-6 relative z-20">
        <div className="text-lg md:text-2xl mb-3  md:mb-4 text-white">
          플레이 기록
        </div>
        <div className="custom-scrollbar space-y-2 overflow-y-auto max-h-[740px]  ">
          {gameList.map((game: Game, index: number) => (
            <div
              key={index}
              onClick={() => handleGameClick(game)}
              className={`${blockStyle} ${blockHover} ${blockActive} p-2 md:p-3 md:px-6 md:w-96 w-96 px-6 flex justify-between items-center text-sm md:text-base cursor-pointer`}
            >
              <span className="text-center">{game.gameId}</span>
              <span className="text-center truncate px-2">
                {game.roomTitle}
              </span>
              <span className="text-center">
                {new Date(game.startedAt).toLocaleDateString()}
              </span>
              <span className="text-center">
                {game.playTime.split(":").slice(1).join(":")}
              </span>
            </div>
          ))}
        </div>
        {/* 
      <div className="text-center mt-3 md:mt-5 text-xl md:text-base text-white">
        <button
          className={`${blockActive} ${blockHover} ${blockStyle} mr-3 px-3 py-1.5 align-middle`}
        >
          <img src={leftIcon} alt="left" className="filter invert" />
        </button>
        1 / 10
        <button
          className={`${blockActive} ${blockHover} ${blockStyle} ml-3 px-3 py-1.5 align-middle`}
        >
          <img src={rightIcon} alt="right" className="filter invert" />
        </button>
      </div> */}

        {/* 게임 기록 상세 모달 */}
        {selectedGame && (
          <GamePlayDetail
            isOpen={isModalOpen}
            onClose={() => {
              setIsModalOpen(false);
              setSelectedGame(null);
            }}
            gameId={selectedGame.gameId}
          />
        )}
      </div>
    </>
  );
}

export default MyPlayList;
