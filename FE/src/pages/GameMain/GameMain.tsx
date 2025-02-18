import lobbyBgm from "../../assets/bgm/lobby.mp3";
import bgImg from "../../assets/game-rooms-background.png";
import "./components/style.css";

import GameRoomList from "./components/GameRoomList";
import GameHeader from "./components/GameHeader";
import GameSidebar from "./components/GameSidebar";
import GameMainChat from "./components/GameMainChat";
import GameMainMenu from "./components/GameMainMenu";
import CreateRoomButton from "./components/CreateRoomButton";
import AudioPlayer from "../../util/AudioPlayer";
import { useRecoilValue } from "recoil";
import { settingsState } from "../../store/settingState";

function GameMain() {
  const settingData = useRecoilValue(settingsState);
  const isFullscreen = settingData.isFullscreen;

  return (
    <>
      <AudioPlayer src={lobbyBgm} isPlaying={true} shouldLoop={true} />

      <div className="w-screen h-screen bg-black select-none flex items-center justify-center overflow-hidden">
        <div
          className="w-full h-full bg-cover bg-center bg-no-repeat fixed inset-0"
          style={{ backgroundImage: `url(${bgImg})` }}
        >
          <div className="absolute inset-0 bg-gradient-to-r from-black to-[#11223349] opacity-100" />
        </div>

        <div className="relative w-full h-full flex items-center justify-center z-10">
          <div className="max-w-[1440px] w-full">
            <div className="slide-in-top">
              <GameHeader />
            </div>

            <div className="md:flex md:m-12 md:mb-5 mb-5">
              <div className="slide-in-left">
                <GameSidebar />
              </div>

              <div className="flex flex-col flex-1 px-8">
                <div className="slide-in-top flex flex-col gap-4">
                  <CreateRoomButton />
                  <GameRoomList />
                </div>

                <div className="slide-in-right mt-5">
                  <GameMainChat />
                </div>
              </div>
            </div>

            <div className="slide-in-right">
              <GameMainMenu />
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default GameMain;
