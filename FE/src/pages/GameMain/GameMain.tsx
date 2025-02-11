import bgImg from "../../assets/game-rooms-background.png";

import { useEffect, useRef } from "react";
import { atom } from "recoil";
import { Link } from "react-router-dom";

import GameRoomList from "./components/GameRoomList";
import GameHeader from "./components/GameHeader";
import GameSidebar from "./components/GameSidebar";
import GameMainChat from "./components/GameMainChat";
import GameMainMenu from "./components/GameMainMenu";
import CreateRoomButton from "./components/CreateRoomButton";

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
              <CreateRoomButton />

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
