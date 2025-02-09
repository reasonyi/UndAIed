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
