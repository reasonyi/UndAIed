import bgImg from "../../assets/game-rooms-background.png";

import { useEffect, useRef } from "react";
import { atom } from "recoil";
import { Link } from "react-router-dom";

import GameRoomList from "./components/GameRoomList";
import GameHeader from "./components/GameHeader";
import GameSidebar from "./components/GameSidebar";
import GameMainChat from "./components/GameMainChat";
import GameMainMenu from "./components/GameMainMenu";
function GameMain() {
  interface RoomData {
    id: number;
    title: string;
    memberCount: number;
  }

  const fetchRooms: RoomData[] = [
    {
      id: 1,
      title: "즐거운 파티게임방",
      memberCount: 3,
    },
    {
      id: 2,
      title: "초보만 Welcome",
      memberCount: 2,
    },
    {
      id: 3,
      title: "실력자만 오세요",
      memberCount: 4,
    },
    {
      id: 4,
      title: "자유롭게 즐겨요~",
      memberCount: 1,
    },
    {
      id: 5,
      title: "친목게임방",
      memberCount: 2,
    },
    {
      id: 1,
      title: "즐거운 파티게임방",
      memberCount: 3,
    },
    {
      id: 2,
      title: "초보만 Welcome",
      memberCount: 2,
    },
    {
      id: 3,
      title: "실력자만 오세요",
      memberCount: 4,
    },
    {
      id: 4,
      title: "자유롭게 즐겨요~",
      memberCount: 1,
    },
    {
      id: 5,
      title: "친목게임방",
      memberCount: 2,
    },
  ];

  const observerOption = {
    root: null,
    threshold: 0.7,
  };

  // 무한스크롤 구현
  const target = useRef<HTMLDivElement>(null);
  // hasNextPage는 백엔드에서 구현해놔야 데이터가 일치함 만약 프론트에서 totalpage가 3이고 현재페이지가 3인데 방이 증가해서 백엔드의 totalpage가 4가 되었을때? 데이터가 불일치하는 문제가 있다
  // 일단 api 부르고 체크하는 방법이 있지만 그렇게 한다면 일단 로딩이 한번 더 발생하기 때문에 손해
  // const { data, fetchNextPage, hasNextPage, isFetchingNextPage } =
  //   useRoomList();
  // useEffect(() => {
  //   const currentTarget = target.current;
  //   const observer = new IntersectionObserver((entries) => {
  //     if (entries[0].isIntersecting && hasNextPage) {
  //       //보이고 다음페이지가 있으면
  //       // fetchNextPage();
  //     }
  //   }, observerOption);

  //   if (currentTarget) {
  //     observer.observe(currentTarget);
  //   }

  //   return () => {
  //     if (currentTarget) {
  //       observer.unobserve(currentTarget);
  //     }
  //     observer.disconnect();
  //   };
  // }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  //--------------------------------웹소켓 구현--------------------
  //type, atoms, hook

  return (
    <div className="w-screen h-screen bg-black select-none">
      <div
        className="w-full max-w-[1440px] mx-auto h-max-[840px] bg-cover bg-center bg-no-repeat relative"
        style={{ backgroundImage: `url(${bgImg})` }}
      >
        <div className="absolute inset-0 bg-gradient-to-r from-black to-[#11223349] opacity-100 " />
        <div className="relative z-10 ">
          {/* Header */}
          <GameHeader />

          <div className="md:flex md:h-screen md:max-h-[730px] md:m-12 md:mb-5 mb-5">
            <GameSidebar />
            <div className="flex flex-col flex-1 gap-4 px-8 ">
              <Link to="/createroom">
                <button className="w-32 justify-start px-6 py-2 bg-black border-2 border-[#bf8f5b] text-white rounded hover:bg-[#211b05] hover:border-[#dea569] hover:shadow-[0_0_10px_0] hover:shadow-[#f99f3e] active:border-[#906639] active:bg-black active:shadow-none duration-100 ">
                  방 만들기
                </button>
              </Link>
              <GameRoomList />
              <GameMainChat />
            </div>
          </div>
          <GameMainMenu />
        </div>
      </div>
    </div>
  );
}

export default GameMain;
