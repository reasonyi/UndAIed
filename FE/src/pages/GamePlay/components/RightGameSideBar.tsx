import { useMemo, useState } from "react";
import { faPeopleGroup } from "@fortawesome/free-solid-svg-icons";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import GameProfile from "./GameProfile";
import DiedProfile from "./DiedProfile";
import EmptyProfile from "../../GameRoom/components/EmptyProfile";

interface Player {
  id: number;
  playerNum: number;
  name: string;
  isDied: boolean;
  imgNum: number;
}

interface IRightGameSideBarProps {
  /** 현재 게임 방의 유저 목록 */
  players: Player[];
  /** 플레이어 아이콘 배열 */
  iconArr: string[];
}
const peopleGroup: IconDefinition = faPeopleGroup;

/**
 * props로 전달받은 `players` 배열에서
 * playerNum(1~6)에 해당하는 유저를 찾아서
 * GameProfile / DiedProfile을 렌더링
 */
function RightGameSideBar({ players, iconArr }: IRightGameSideBarProps) {
  //일단 펼쳐두고 나중에 배열로
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
  const player7 = useMemo(
    () => players.find((user) => user.playerNum === 6),
    [players]
  );
  const player8 = useMemo(
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
          {player1 ? (
            <GameProfile
              nickname={player1.name}
              playerNum={player1.playerNum}
              icon={iconArr[player1.imgNum]}
              isDied={player1.isDied}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-1 px-2 py-1">
          {player2 ? (
            <GameProfile
              nickname={player2.name}
              playerNum={player2.playerNum}
              icon={iconArr[player2.imgNum]}
              isDied={player2.isDied}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-1 px-2 py-1">
          {player3 ? (
            <GameProfile
              nickname={player3.name}
              playerNum={player3.playerNum}
              icon={iconArr[player3.imgNum]}
              isDied={player3.isDied}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-2 px-2 py-1">
          {player4 ? (
            <GameProfile
              nickname={player4.name}
              playerNum={player4.playerNum}
              icon={iconArr[player4.imgNum]}
              isDied={player4.isDied}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-2 px-2 py-1">
          {player5 ? (
            <GameProfile
              nickname={player5.name}
              playerNum={player5.playerNum}
              icon={iconArr[player5.imgNum]}
              isDied={player5.isDied}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-2 px-2 py-1">
          {player6 ? (
            <GameProfile
              nickname={player6.name}
              playerNum={player6.playerNum}
              icon={iconArr[player6.imgNum]}
              isDied={player6.isDied}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-3 px-2 py-1">
          {player7 ? (
            <GameProfile
              nickname={player7.name}
              playerNum={player7.playerNum}
              icon={iconArr[player7.imgNum]}
              isDied={player7.isDied}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-3 px-2 py-1">
          {player8 ? (
            <GameProfile
              nickname={player8.name}
              playerNum={player8.playerNum}
              icon={iconArr[player8.imgNum]}
              isDied={player8.isDied}
            />
          ) : (
            <EmptyProfile />
          )}
        </div>
        <div className="row-start-3"></div>
        <div className="w-full text-base justify-center items-center bg-[rgb(7,7,10)] border-2 border-solid border-[#B4B4B4] col-span-3 text-white px-2 py-1">
          <div>시스템 로그</div>
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

export default RightGameSideBar;
