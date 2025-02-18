import GameHeader from "../GameMain/components/GameHeader";
import gameMyPageBackground from "../../assets/game-my-page/game-my-page-background.png";
import GameUserInfo from "./components/GameUserInfo";
import Character from "./components/Character";
import GamePlayList from "./components/GamePlayList";
import { useRecoilValue } from "recoil";
import { settingsState } from "../../store/settingState";

import "../GameMain/components/style.css";
import AudioPlayer from "../../util/AudioPlayer";
import slideSound from "../../assets/bgm/slide.mp3";
import myPageBgm from "../../assets/bgm/my-page.mp3";

function GameMyPage() {
  const settingData = useRecoilValue(settingsState);
  const isFullscreen = settingData.isFullscreen;

  return (
    <>
      <AudioPlayer src={slideSound} isPlaying={true} shouldLoop={false} />
      <AudioPlayer src={myPageBgm} isPlaying={true} shouldLoop={true} />

      <div className="bg-black min-h-screen w-full">
        {/* {isFullscreen ? ( */}
        <div
          className="w-full h-screen bg-cover bg-center bg-no-repeat relative select-none flex items-center justify-center"
          style={{ backgroundImage: `url(${gameMyPageBackground})` }}
        >
          <div className="absolute bg-[#00000063] inset-0 bg-gradient-to-r from-black via-transparent to-black z-0" />
          <div className="relative max-w-[1440px] w-full z-10">
            <GameHeader />
            <div className="h-full grid grid-cols-1 justify-items-center md:grid-cols-3 gap-4 md:gap-8">
              <div className="slide-in-left">
                <GameUserInfo />
              </div>
              <div className="slide-in-center">
                <Character />
              </div>
              <div className="slide-in-right">
                <GamePlayList />
              </div>
            </div>
          </div>
        </div>
        {/* ) : ( */}
        {/* <div
            className="bg-black w-full max-w-[1440px] mx-auto min-h-screen bg-cover bg-center bg-no-repeat relative select-none"
            style={{ backgroundImage: `url(${gameMyPageBackground})` }}
          >
            <div className="absolute bg-[#00000063] inset-0 bg-gradient-to-r from-black via-transparent to-black z-0" />
            <div className="relative mx-auto h-full flex flex-col">
              <GameHeader />
              <div className="h-full grid grid-cols-1 justify-items-center md:grid-cols-3 gap-4 md:gap-8">
                <div className="slide-in-left">
                  <GameUserInfo />
                </div>
                <div className="slide-in-center">
                  <Character />
                </div>
                <div className="slide-in-right">
                  <GamePlayList />
                </div>
              </div>
            </div>
          </div>
        )} */}
      </div>
    </>
  );
}

export default GameMyPage;
