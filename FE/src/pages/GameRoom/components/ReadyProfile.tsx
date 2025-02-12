import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import {
  faRobot,
  faNoteSticky,
  faCheckToSlot,
} from "@fortawesome/free-solid-svg-icons";

interface IReadyProfileProps {
  nickname: string;
  icon: string;
  playerNum: number;
}

function ReadyProfile({ nickname, icon, playerNum }: IReadyProfileProps) {
  const robot: IconDefinition = faRobot;
  const noteSticky: IconDefinition = faNoteSticky;
  const checkToSlot: IconDefinition = faCheckToSlot;
  return (
    <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,0,0,0.5)] hover:shadow-[0px_0px_16px_rgba(255,0,0,0.45)]">
      {/* <div className="hover:shadow-[0px_0px_16px_rgba(255,0,0,0.45)] w-full h-full hover:animate-ping text-white"></div> */}
      <div className="flex justify-center items-center px-4 py-1">
        {/* 죽었을 때 변화 이미지 */}
        {/* <img className="filter grayscale sepia brightness-75 contrast-125" src={PlayerIcon1} /> */}
        <img className="filter brightness-75 w-3/4 h-3/4" src={icon} />
      </div>
      <div className="flex w-full text-base font-bold justify-center text-[#cccccc] mb-1">
        {nickname}
      </div>
      <div className="flex w-full justify-center"></div>
    </div>
  );
}

export default ReadyProfile;
