import { useMemo, useState } from "react";
import { faPeopleGroup } from "@fortawesome/free-solid-svg-icons";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import GameProfile from "./GameProfile";
import DiedProfile from "./DiedProfile";
import EmptyProfile from "../../GameRoom/components/EmptyProfile";
import { IAnonimus } from "../../../types/gameplay";

interface IRightGameSideBarProps {
  /** 현재 게임 방의 유저 목록 */
  players?: IAnonimus[];
  /** 플레이어 아이콘 배열 */
  iconArr: string[];
  stage?: string;
  onVoteSubmit: (target: number) => void;
}
const peopleGroup: IconDefinition = faPeopleGroup;

/**
 * props로 전달받은 `players` 배열에서
 * playerNum(1~6)에 해당하는 유저를 찾아서
 * GameProfile / DiedProfile을 렌더링
 */
function RightGameSideBar({
  players,
  iconArr,
  stage,
  onVoteSubmit,
}: IRightGameSideBarProps) {
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
        {players ? (
          <>
            <div className="row-start-1 px-2 py-1">
              {players[0].inGame ? (
                players[0].died ? (
                  <DiedProfile
                    nickname={`익명${players[0].number}`}
                    icon={iconArr[players[0].number]}
                  />
                ) : (
                  <GameProfile
                    stage={stage}
                    onVoteSubmit={onVoteSubmit}
                    nickname={`익명${players[0].number}`}
                    playerNum={players[0].number}
                    icon={iconArr[players[0].number]}
                    isDied={players[0].died}
                  />
                )
              ) : (
                <EmptyProfile />
              )}
            </div>
            <div className="row-start-1 px-2 py-1">
              {players[1].inGame ? (
                players[1].died ? (
                  <DiedProfile
                    nickname={`익명${players[1].number}`}
                    icon={iconArr[players[1].number]}
                  />
                ) : (
                  <GameProfile
                    stage={stage}
                    onVoteSubmit={onVoteSubmit}
                    nickname={`익명${players[1].number}`}
                    playerNum={players[1].number}
                    icon={iconArr[players[1].number]}
                    isDied={players[1].died}
                  />
                )
              ) : (
                <EmptyProfile />
              )}
            </div>
            <div className="row-start-1 px-2 py-1">
              {players[2].inGame ? (
                players[2].died ? (
                  <DiedProfile
                    nickname={`익명${players[2].number}`}
                    icon={iconArr[players[2].number]}
                  />
                ) : (
                  <GameProfile
                    stage={stage}
                    onVoteSubmit={onVoteSubmit}
                    nickname={`익명${players[2].number}`}
                    playerNum={players[2].number}
                    icon={iconArr[players[2].number]}
                    isDied={players[2].died}
                  />
                )
              ) : (
                <EmptyProfile />
              )}
            </div>
            <div className="row-start-2 px-2 py-1">
              {players[3].inGame ? (
                players[3].died ? (
                  <DiedProfile
                    nickname={`익명${players[3].number}`}
                    icon={iconArr[players[3].number]}
                  />
                ) : (
                  <GameProfile
                    stage={stage}
                    onVoteSubmit={onVoteSubmit}
                    nickname={`익명${players[3].number}`}
                    playerNum={players[3].number}
                    icon={iconArr[players[3].number]}
                    isDied={players[3].died}
                  />
                )
              ) : (
                <EmptyProfile />
              )}
            </div>
            <div className="row-start-2 px-2 py-1">
              {players[4].inGame ? (
                players[4].died ? (
                  <DiedProfile
                    nickname={`익명${players[4].number}`}
                    icon={iconArr[players[4].number]}
                  />
                ) : (
                  <GameProfile
                    stage={stage}
                    onVoteSubmit={onVoteSubmit}
                    nickname={`익명${players[4].number}`}
                    playerNum={players[4].number}
                    icon={iconArr[players[4].number]}
                    isDied={players[4].died}
                  />
                )
              ) : (
                <EmptyProfile />
              )}
            </div>
            <div className="row-start-2 px-2 py-1">
              {players[5].inGame ? (
                players[5].died ? (
                  <DiedProfile
                    nickname={`익명${players[5].number}`}
                    icon={iconArr[players[5].number]}
                  />
                ) : (
                  <GameProfile
                    stage={stage}
                    onVoteSubmit={onVoteSubmit}
                    nickname={`익명${players[5].number}`}
                    playerNum={players[5].number}
                    icon={iconArr[players[5].number]}
                    isDied={players[5].died}
                  />
                )
              ) : (
                <EmptyProfile />
              )}
            </div>
            <div className="row-start-3 px-2 py-1">
              {players[6].inGame ? (
                players[6].died ? (
                  <DiedProfile
                    nickname={`익명${players[6].number}`}
                    icon={iconArr[players[6].number]}
                  />
                ) : (
                  <GameProfile
                    stage={stage}
                    onVoteSubmit={onVoteSubmit}
                    nickname={`익명${players[6].number}`}
                    playerNum={players[6].number}
                    icon={iconArr[players[6].number]}
                    isDied={players[6].died}
                  />
                )
              ) : (
                <EmptyProfile />
              )}
            </div>
            <div className="row-start-3 px-2 py-1">
              {players[7].inGame ? (
                players[7].died ? (
                  <DiedProfile
                    nickname={`익명${players[7].number}`}
                    icon={iconArr[players[7].number]}
                  />
                ) : (
                  <GameProfile
                    stage={stage}
                    onVoteSubmit={onVoteSubmit}
                    nickname={`익명${players[7].number}`}
                    playerNum={players[7].number}
                    icon={iconArr[players[7].number]}
                    isDied={players[7].died}
                  />
                )
              ) : (
                <EmptyProfile />
              )}
            </div>
            <div className="row-start-3"></div>
          </>
        ) : (
          <div className="text-lg text-white">Loading...</div>
        )}

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
