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
    <div className="relative min-h-screen w-full bg-black overflow-hidden">
      <div className="absolute bg-[#00000063] inset-0 bg-gradient-to-r from-black via-transparent to-black z-0" />
      <AudioPlayer src={slideSound} isPlaying={true} shouldLoop={false} />
      <AudioPlayer src={myPageBgm} isPlaying={true} shouldLoop={true} />
      <div
        className="absolute inset-0 bg-cover bg-center bg-no-repeat"
        style={{ backgroundImage: `url(${gameMyPageBackground})` }}
      />
      <div className="absolute inset-0 bg-black opacity-40" />{" "}
      <div className="absolute inset-0 bg-gradient-to-r from-black via-transparent to-black opacity-70" />{" "}
      <div className="absolute inset-0 bg-[#00000063]" />
      <div className="relative h-full min-h-screen flex flex-col max-w-[1440px] mx-auto px-4">
        <div className="py-4">
          <GameHeader />
        </div>

        <div className="flex-1 grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-8">
          <div className="slide-in-left flex justify-center">
            <GameUserInfo />
          </div>
          <div className="slide-in-center flex justify-center">
            <Character />
          </div>
          <div className="slide-in-right flex justify-center">
            <GamePlayList />
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameMyPage;
