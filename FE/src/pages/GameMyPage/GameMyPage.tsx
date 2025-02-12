import GameHeader from "../GameMain/components/GameHeader";
import gameMyPageBackground from "../../assets/game-my-page/game-my-page-background.png";
import GameUserInfo from "./components/GameUserInfo";
import Character from "./components/Character";
import GamePlayList from "./components/GamePlayList";
import { useUserProfile } from "../../hooks/useUserData";
import GameProfileEditor from "./components/GameProfileEditor";

function GameMyPage() {
  const { data: response, isLoading, error } = useUserProfile();
  if (isLoading) {
    console.log("is Loading");
  }

  if (error) {
    console.log("GamrMyPage 에러 났어요 유저 프로필을 못가져와요 ㅎㅎ", error);
  }
  const userInfo = response?.data;
  console.log("유저 정보 입니다", userInfo);
  // const userInfo = {
  //   nickname: "저AI아닌데요",
  //   profileImage: 2,
  //   avatar: 1,
  //   sex: true,
  //   age: 27,
  //   totalWin: 15,
  //   totalLose: 13,
  //   game: [
  //     {
  //       gameId: 1,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 4,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 1,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 4,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 1,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 4,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 1,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 4,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 1,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 4,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 1,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //     {
  //       gameId: 4,
  //       roomTitle: "방 제목",
  //       startedAt: "2025-01-22T15:25:29.2762331",
  //       playTime: "00:20:36",
  //     },
  //   ],
  // };
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
            <GameUserInfo />
            <Character />
            <GamePlayList />
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameMyPage;
