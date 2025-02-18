import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import {
  faRobot,
  faNoteSticky,
  faCheckToSlot,
} from "@fortawesome/free-solid-svg-icons";
import { useRecoilState } from "recoil";
import { isUserDiedState } from "../../../store/gamePlayState";

interface IGameProfileProps {
  nickname: string;
  playerNum: number;
  icon: string;
  isDied: boolean;
  stage?: string;
  onVoteSubmit: (target: number) => void;
}

function GameProfile({
  nickname,
  icon,
  playerNum,
  stage,
  isDied,
  onVoteSubmit,
}: IGameProfileProps) {
  const robot: IconDefinition = faRobot;
  const noteSticky: IconDefinition = faNoteSticky;
  const checkToSlot: IconDefinition = faCheckToSlot;

  const [isUserDead, setIsUserDead] = useRecoilState<boolean>(isUserDiedState);

  return (
    <div
      className={`${
        stage === "vote" ? "profile-container" : "normal-profile-container"
      } shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full`}
    >
      {/* 6개의 선(line) */}
      <div className="line"></div>
      <div className="line"></div>
      <div className="line"></div>
      <div className="line"></div>
      <div className="line"></div>
      <div className="line"></div>
      <div className="flex justify-center items-center px-4 py-1">
        <img className="filter brightness-75 w-3/4 h-3/4" src={icon} />
      </div>
      <div className="flex w-full text-base font-bold justify-center text-[#cccccc] mb-1">
        {nickname}
      </div>
      <div className="flex w-full justify-center">
        <button>
          <FontAwesomeIcon
            icon={robot}
            className="text-[#cccccc] hover:text-white p-1 w-[1.25rem] h-[1.25rem] mx-1"
          />
        </button>
        {stage === "vote" && isUserDead === false ? (
          <button
            onClick={() => {
              onVoteSubmit(playerNum);
            }}
          >
            <FontAwesomeIcon
              icon={checkToSlot}
              className="text-[#cccccc] hover:text-white p-1 w-[1.5rem] h-[1.5rem] mx-1"
            />
          </button>
        ) : (
          <></>
        )}
        <button>
          <FontAwesomeIcon
            icon={noteSticky}
            className="text-[#cccccc] hover:text-white p-1 w-[1.25rem] h-[1.25rem] mx-1"
          />
        </button>
      </div>
    </div>
  );
}

export default GameProfile;
