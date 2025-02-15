import bgImg from "../../assets/game-rooms-background.png";
import "./components/style.css";
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
        className="w-full max-w-[1440px] mx-auto h-min-[1000px] h-max-[840px] bg-cover bg-center bg-no-repeat relative"
        style={{ backgroundImage: `url(${bgImg})` }}
      >
        <div className="absolute inset-0 bg-gradient-to-r from-black to-[#11223349] opacity-100" />
        <div className="relative z-10">
          {/* 위에서 아래로 */}
          <div className="slide-in-top">
            <GameHeader />
          </div>

          <div className="md:flex md:h-screen md:max-h-[730px] md:m-12 md:mb-5 mb-5">
            {/* 왼쪽에서 오른쪽으로 */}
            <div className="slide-in-left">
              <GameSidebar />
            </div>

            <div className="flex flex-col flex-1 px-8">
              {/* 아래에서 위로 */}
              <div className="slide-in-top flex flex-col gap-4">
                <CreateRoomButton />
                <GameRoomList />
              </div>

              {/* 위에서 아래로 */}
              <div className="slide-in-right mt-5">
                <GameMainChat />
              </div>
            </div>
          </div>

          {/* 오른쪽에서 왼쪽으로 */}
          <div className="slide-in-right">
            <GameMainMenu />
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameMain;
