import GameHeader from "../GameMain/components/GameHeader";
import gameMyPageBackground from "../../assets/game-my-page/game-my-page-background.png";
import playerIcon from "../../assets/player-icon/player-icon-1.svg";
import charactor from "../../assets/game-my-page/charactor-demo.png";
import leftIcon from "../../assets/icon/left.svg";
import rightIcon from "../../assets/icon/right.svg";

function GameMyPage() {
  const blockStyle =
    "bg-[#0000006c] border border-[#f74a5c]/40 backdrop-blur-[12.20px] text-[#fffbfb] rounded-[5px] transition-all duration-200";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";

  const userInfo = {
    nickname: "저AI아닌데요",
    profileImage: 2,
    avatar: 1,
    sex: true,
    age: 27,
    totalWin: 15,
    totalLose: 13,
    game: [
      {
        gameId: 1,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 4,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 1,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 4,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 1,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 4,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 1,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 4,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 1,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 4,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 1,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
      {
        gameId: 4,
        roomTitle: "방 제목",
        startedAt: "2025-01-22T15:25:29.2762331",
        playTime: "00:20:36",
      },
    ],
  };

  const gameData = userInfo.game;

  return (
    <div className="bg-black min-h-screen w-full">
      <div
        className="bg-black w-full max-w-[1440px] mx-auto min-h-screen bg-cover bg-center bg-no-repeat relative select-none"
        style={{ backgroundImage: `url(${gameMyPageBackground})` }}
      >
        <div className="absolute bg-[#00000063] inset-0 bg-gradient-to-r from-black via-transparent to-black z-0" />
        <div className="relative mx-auto h-full flex flex-col">
          <GameHeader />
          <div className="h-full grid grid-cols-1 justify-items-center md:grid-cols-3 gap-4 md:gap-8">
            {/* Left Section - 유저 정보 */}
            <div className="space-y-4 md:space-y-10 flex md:flex-col items-center gap-4">
              {/* User ID Section */}
              <div className="w-44 p-4 md:p-6 flex flex-col items-center justify-center">
                <div
                  className={`${blockStyle} flex items-center justify-center p-1 md:h-44 md:w-44 mb-3 md:mb-4`}
                >
                  <img src={`${playerIcon}`} className="md:w-44" alt="" />
                </div>
                <div className="text-center text-lg md:text-xl text-white">
                  {userInfo.nickname}
                </div>
              </div>

              {/* Stats Section */}
              <div className={`${blockStyle} px-6 md:px-10 pb-10 pt-8`}>
                <div className="text-center">전적</div>
                <div className="relative w-36 h-36 md:mb-10 md:mt-10 md:w-48 md:h-48 mx-auto">
                  <svg viewBox="0 0 100 100" className="w-full h-full">
                    <circle
                      cx="50"
                      cy="50"
                      r="45"
                      fill="none"
                      stroke="#f74a5c"
                      strokeWidth="10"
                      strokeDasharray={`${180 * 0.6} 180`}
                      transform="rotate(-90 50 50)"
                    />
                    <circle
                      cx="50"
                      cy="50"
                      r="45"
                      fill="none"
                      stroke="#22c55e"
                      strokeWidth="10"
                      strokeDasharray={`${180 * 0.4} 180`}
                      transform="rotate(108 50 50)"
                    />
                  </svg>
                </div>
                <div className="text-center mb-3 md:mb-10 text-sm md:text-base">
                  승률 | {userInfo.totalWin}승 {userInfo.totalLose}패 |
                  {" " +
                    Math.round(
                      (userInfo.totalWin /
                        (userInfo.totalLose + userInfo.totalWin)) *
                        10000
                    ) /
                      100}
                  %
                </div>
                <div
                  className={`${blockStyle} ${blockActive} ${blockHover} hover:bg-[#f8376441] text-center py-2 px-3 md:py-2 mt-3 md:mt-4 text-sm md:text-base`}
                >
                  정보 수정
                </div>
              </div>
            </div>

            {/* Middle Section - Character */}
            <div className="relative z-0 md:w-[600px] justify-around items-center min-h-[300px] md:min-h-0 hidden md:flex flex-col">
              <div className="relative w-full h-full flex items-center justify-center">
                <img
                  src={charactor}
                  alt=""
                  className="h-full w-full md:pb-24 object-contain -z-10"
                />
              </div>
              <div
                className={`${blockStyle} ${blockHover} ${blockActive} text-center py-4 mb-5 text-sm md:text-base md:w-44 absolute bottom-0 left-1/2 transform -translate-x-1/2`}
              >
                캐릭터 선택
              </div>
            </div>

            {/* Right Section - Play List */}
            <div className="p-4 md:p-6 relative z-20">
              <div className="text-lg md:text-2xl mb-3 md:mb-4 text-white">
                플레이 기록
              </div>
              <div className="space-y-2">
                {gameData.map((game, index) => (
                  <div
                    key={index}
                    className={`${blockStyle} ${blockHover} ${blockActive} p-2 md:p-3 md:px-6 md:w-96 w-96 px-6 flex justify-between items-center text-sm md:text-base`}
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
              <div className=" text-center mt-3 md:mt-5 text-xl md:text-base text-white">
                <button
                  className={`${blockActive} ${blockHover} ${blockStyle} mr-3 px-3 py-1.5  align-middle`}
                >
                  <img src={leftIcon} alt="left" className="filter invert" />
                </button>
                1 / 10
                <button
                  className={`${blockActive} ${blockHover} ${blockStyle} ml-3 px-3 py-1.5 align-middle`}
                >
                  <img src={rightIcon} alt="right" className="filter invert" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameMyPage;
