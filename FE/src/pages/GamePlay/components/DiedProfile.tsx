import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import {
  faRobot,
  faNoteSticky,
  faCheckToSlot,
} from "@fortawesome/free-solid-svg-icons";

interface IDiedProfileProps {
  nickname: string;
  icon: string;
}

function DiedProfile({ nickname, icon }: IDiedProfileProps) {
  const robot: IconDefinition = faRobot;
  const noteSticky: IconDefinition = faNoteSticky;
  const checkToSlot: IconDefinition = faCheckToSlot;
  return (
    <div className="opacity-70 shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full">
      {/* <div className="hover:shadow-[0px_0px_16px_rgba(255,0,0,0.45)] w-full h-full hover:animate-ping text-white"></div> */}
      <div className="flex justify-center items-center px-4 py-1">
        <img
          className="filter grayscale sepia brightness-75 contrast-125 w-3/4 h-3/4"
          src={icon}
        />
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
        <button disabled className="disabled:opacity-0">
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

export default DiedProfile;
