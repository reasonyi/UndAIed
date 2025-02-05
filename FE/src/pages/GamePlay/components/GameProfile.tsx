import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import {
  faRobot,
  faNoteSticky,
  faCheckToSlot,
} from "@fortawesome/free-solid-svg-icons";

interface IGameProfileProps {
  nickname: string;
  icon: string;
}

function GameProfile({ nickname, icon }: IGameProfileProps) {
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
      <div className="flex w-full justify-center">
        <button>
          <FontAwesomeIcon
            icon={robot}
            className="text-[#cccccc] hover:text-white p-1 w-[1.25rem] h-[1.25rem] mb-2"
          />
        </button>
        <button>
          <FontAwesomeIcon
            icon={checkToSlot}
            className="text-[#cccccc] hover:text-white p-1 w-[1.25rem] h-[1.25rem] mx-3"
          />
        </button>
        <button>
          <FontAwesomeIcon
            icon={noteSticky}
            className="text-[#cccccc] hover:text-white p-1 w-[1.25rem] h-[1.25rem] mb-2"
          />
        </button>
      </div>
    </div>
  );
}

export default GameProfile;
