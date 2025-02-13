import { useMemo, useState } from "react";
import ReadyProfile from "./ReadyProfile";
import EmptyProfile from "./EmptyProfile";
import { faPeopleGroup } from "@fortawesome/free-solid-svg-icons";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

interface Player {
  playerNum: number;
  name: string;
  profileImage: number;
  isHost: boolean;
}

interface RightSideBarProps {
  /** 현재 게임 방의 유저 목록 */
  players: Player[];
  /** 플레이어 아이콘 배열 */
  iconArr: string[];
}
const peopleGroup: IconDefinition = faPeopleGroup;

/**
 * props로 전달받은 `players` 배열에서
 * playerNum(1~6)에 해당하는 유저를 찾아서
 * ReadyProfile / EmptyProfile을 렌더링
 */
function RightSideBar({ players, iconArr }: RightSideBarProps) {
  const player1 = useMemo(
    () => players.find((user) => user.playerNum === 1),
    [players]
  );
  const player2 = useMemo(
    () => players.find((user) => user.playerNum === 2),
    [players]
  );
  const player3 = useMemo(
    () => players.find((user) => user.playerNum === 3),
    [players]
  );
  const player4 = useMemo(
    () => players.find((user) => user.playerNum === 4),
    [players]
  );
  const player5 = useMemo(
    () => players.find((user) => user.playerNum === 5),
    [players]
  );
  const player6 = useMemo(
    () => players.find((user) => user.playerNum === 6),
    [players]
  );

  const [isRightOpen, setIsRightOpen] = useState(true);
  return (
    <>
      <div
        className={`fixed translate-y-full z-20 right-[max(0px,calc(50%-45rem))] w-[33.5rem] py-6 px-3 h-screen bg-black bg-opacity-40
                  border-solid border-l-2 border-l-[rgba(255,255,255,0.35)]
                  shadow-[0px_0px_16px_rgba(255,255,255,0.25)]
                  transition-transform duration-300 ease-in-out
                  xl:translate-y-0 grid grid-cols-3 grid-rows-4 gap-4
                ${isRightOpen ? "translate-y-full" : "close-right-sidebar"}`}
      >
        {/* 플레이어 프로필 영역 */}
        <div className="row-start-1 px-2 py-1">
          {players[0] ? (
            <ReadyProfile
              nickname={players[0].name}
              playerNum={players[0].playerNum}
              icon={iconArr[players[0].profileImage]}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-1 px-2 py-1">
          {players[1] ? (
            <ReadyProfile
              nickname={players[1].name}
              playerNum={players[1].playerNum}
              icon={iconArr[players[1].profileImage]}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-1 px-2 py-1">
          {players[2] ? (
            <ReadyProfile
              nickname={players[2].name}
              playerNum={players[2].playerNum}
              icon={iconArr[players[2].profileImage]}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-2 px-2 py-1">
          {players[3] ? (
            <ReadyProfile
              nickname={players[3].name}
              playerNum={players[3].playerNum}
              icon={iconArr[players[3].profileImage]}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-2 px-2 py-1">
          {players[4] ? (
            <ReadyProfile
              nickname={players[4].name}
              playerNum={players[4].playerNum}
              icon={iconArr[players[4].profileImage]}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-2 px-2 py-1">
          {players[5] ? (
            <ReadyProfile
              nickname={players[5].name}
              playerNum={players[5].playerNum}
              icon={iconArr[players[5].profileImage]}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>

        <div className="w-full text-base flex flex-col justify-between items-center row-start-3 row-end-5 col-span-3 text-white px-2 py-1">
          <div className="w-full text-base flex justify-end items-center text-white px-2 py-1">
            {players.length}/6
          </div>
          <div className="w-full h-[80%] text-base flex flex-col justify-center items-center bg-[rgb(7,7,10)] border-2 border-solid border-[#B4B4B4] text-white px-2 py-1">
            <div>시스템 로그</div>
          </div>
        </div>
      </div>

      <button
        className="fixed z-30 top-2 right-4 text-white p-3 rounded-md bg-gray-800 xl:translate-x-[150%]"
        onClick={() => setIsRightOpen((prev) => !prev)}
      >
        <FontAwesomeIcon
          icon={peopleGroup}
          className="text-white w-[1.25rem] h-[1.25rem]"
        />
      </button>
    </>
  );
}

export default RightSideBar;
