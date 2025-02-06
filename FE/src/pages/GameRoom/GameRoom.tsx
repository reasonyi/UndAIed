import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import PlayerIcon1 from "../../assets/player-icon/player-icon-1.svg";
import PlayerIcon2 from "../../assets/player-icon/player-icon-2.svg";
import PlayerIcon3 from "../../assets/player-icon/player-icon-3.svg";
import PlayerIcon4 from "../../assets/player-icon/player-icon-4.svg";
import PlayerIcon5 from "../../assets/player-icon/player-icon-5.svg";
import PlayerIcon6 from "../../assets/player-icon/player-icon-1.svg";
import PlayerIcon7 from "../../assets/player-icon/player-icon-2.svg";
import PlayerIcon8 from "../../assets/player-icon/player-icon-3.svg";
import Sample from "../../assets/svg-icon/sample.png";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import {
  faBell,
  faGear,
  faUserGroup,
  faDoorOpen,
  faCircleExclamation,
} from "@fortawesome/free-solid-svg-icons";
import ChatBubble from "../GamePlay/components/ChatBuble";
import SystemBubble from "../GamePlay/components/SystemBubble";
import ReadyProfile from "./components/ReadyProfile";
import EmptyProfile from "./components/EmptyProfile";
import { io, Socket } from "socket.io-client";
import ChatForm from "./components/ChatForm";
import { useSocket } from "../../hooks/useSocket";

interface IMessage {
  id: number;
  player: number;
  text: string;
  isMine: boolean; // true면 내가 보낸 메시지, false면 상대방 메시지
}
interface IUser {
  id: number;
  playerNum: number;
  name: string;
  token: string;
  imgNum: number;
}

function GameRoom() {
  const { number } = useParams();
  const scrollRef = useRef<HTMLDivElement | null>(null);

  //socket 훅 사용
  const socket = useSocket();

  //아이콘
  const bell: IconDefinition = faBell;
  const gear: IconDefinition = faGear;
  const userGroup: IconDefinition = faUserGroup;
  const doorOpen: IconDefinition = faDoorOpen;
  const circleExclamation: IconDefinition = faCircleExclamation;

  // 현재 접속한 사용자(본인) playerNum 예시 (실제로는 로그인 정보나 URL 파라미터로 받는 등 구현 필요)

  console.log(socket);

  //플레이어 본인
  const [playerInfo, setPlayerInfo] = useState<IUser | undefined>({
    id: 999,
    playerNum: 1,
    name: "DummyUser",
    token: "dummy-token-123",
    imgNum: 1,
  });
  //방 참여자 전원
  const [users, setUsers] = useState<IUser[]>([]);
  const [messages, setMessages] = useState<IMessage[]>([]);

  const addMessage = (newMessage: IMessage) => {
    //배열의 마지막 요소에서 서버 시간 비교를 통한 정렬 기능이 필요한가?
    setMessages([...messages, newMessage]);
  };

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ block: "end" });
  };

  //user 참가자 전원원 변화화
  useEffect(() => {
    socket.on(
      "enter_room",
      //player는 1~6까지의 값이다.
      (id: number, playerNum: number, name: string, token: string) => {
        setUsers((prev) => [
          ...prev,
          { id, playerNum, name, token, imgNum: 1 },
        ]);
      }
    );

    return () => {
      socket.off("enter_room");
    };
  }, []);

  //socket 시작
  //IMessage 형식으로 받아야 하나?
  useEffect(() => {
    socket.on(
      "receive_system_message",
      (id: number, player: number, message: string) => {
        setMessages((prev) => [
          ...prev,
          { id, player, text: message, isMine: false },
        ]);
      }
    );

    return () => {
      socket.off("message");
    };
  }, []);

  //채팅
  useEffect(() => {
    socket.on("chat", (data: { id: number; player: number; text: string }) => {
      setMessages((prev) => [
        ...prev,
        {
          id: data.id,
          player: data.player,
          text: data.text,
          // 보낸 playerNum과 현재 사용자의 playerNum이 같으면 isMine = true
          isMine: data.player === playerInfo?.playerNum,
        },
      ]);
    });

    return () => {
      socket.off("chat");
    };
  }, [playerInfo?.playerNum]);

  useEffect(() => {
    // 새로운 메시지가 추가될 때 스크롤을 아래로 이동
    scrollToBottom();
  }, [messages]);

  console.log(scrollRef);
  return (
    <div className="bg-[#07070a]">
      <div className="background-gradient max-w-[90rem] mx-auto px-4 sm:px-4 md:px-6">
        <div className="hidden lg:flex flex-col justify-between items-center fixed z-20 inset-0 left-[max(0px,calc(50%-45rem))] right-auto w-[21rem] pb-10 pt-6 pl-6 pr-4 bg-black bg-opacity-70 shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-r-2 border-solid border-r-[rgba(255,255,255,0.35)]">
          <div className="w-full text-base flex justify-center items-center text-[white] bg-[rgb(7,7,10)] px-1.5 py-1 border-2 border-solid border-[rgba(255,255,255,0.35)] rounded-md">
            No. 001 방 제목
          </div>
          <div className="flex flex-col items-center justify-center profile w-52 h-52 border-2 border-solid border-[rgba(255,255,255,0.35)] bg-[#07070a4d]">
            <img
              className="filter brightness-75 w-28 h-28 mb-3"
              src={PlayerIcon1}
            />
            <span className="text-base font-bold justify-center text-[#cccccc] mb-1">
              유저닉네임
            </span>
          </div>
          <button className="w-52 h-14 bg-gradient-to-r from-black via-black to-black rounded-[5px] backdrop-blur-[12.20px] justify-center items-center inline-flex mb-6">
            <div className="w-52 h-14 relative">
              <div className="w-52 h-14 left-0 top-0 absolute opacity-90 bg-black/50 rounded-[5px] shadow-[inset_0px_0px_17px_4px_rgba(255,222,32,0.25)] border-2 border-[#ffc07e]/70" />
              <div className="w-52 h-14 left-0 top-0 absolute flex justify-center items-center text-white text-xl font-normal font-['Inder']">
                게임 시작
              </div>
            </div>
          </button>

          <div className="w-full">
            <div className="config-container w-[3rem] h-[16rem] bg-[#ff3939]/10 rounded-xl flex flex-col justify-between py-4">
              <button>
                <FontAwesomeIcon
                  icon={bell}
                  className="text-white p-1 w-[1.25rem] h-[1.25rem]"
                />
              </button>
              <button>
                <FontAwesomeIcon
                  icon={gear}
                  className="text-white p-1 w-[1.25rem] h-[1.25rem]"
                />
              </button>
              <button>
                <FontAwesomeIcon
                  icon={userGroup}
                  className="text-white p-1 w-[1.25rem] h-[1.25rem]"
                />
              </button>
              <button>
                <FontAwesomeIcon
                  icon={circleExclamation}
                  className="text-white p-1 w-[1.25rem] h-[1.25rem]"
                />
              </button>
              <button>
                <FontAwesomeIcon
                  icon={doorOpen}
                  className="text-white p-1 w-[1.25rem] h-[1.25rem]"
                />
              </button>
            </div>
          </div>
        </div>
        <div className="lg:pl-[19.5rem]">
          <div className="max-w-3xl mx-auto xl:max-w-none xl:ml-0 xl:mr-[32rem]">
            <div className="chat-container flex flex-col h-screen overflow-auto">
              {/* 메시지 리스트 영역 */}
              <div className="flex-1 px-5 pt-4">
                {messages.map((msg: IMessage) => {
                  if (msg.player === 10) {
                    return <SystemBubble key={msg.id} message={msg} />;
                  } else {
                    return (
                      <ChatBubble
                        key={msg.id}
                        message={msg}
                        playerName={
                          users.find((user) => user.playerNum === msg.player)
                            ?.name
                        }
                      />
                    );
                  }
                })}
                <div
                  ref={scrollRef}
                  className="chat-input-temp h-[4.5rem] w-full"
                ></div>
                <div className="chat-input fixed h-10 bottom-4 w-[calc(90rem-21rem-33.5rem-2rem)]">
                  <ChatForm playerNum={playerInfo?.playerNum} socket={socket} />
                </div>
              </div>
              <div className="fixed z-20 right-[max(0px,calc(50%-45rem))] w-[33.5rem] py-6 px-3 hidden h-screen bg-black bg-opacity-40 xl:grid grid-cols-3 grid-rows-4 gap-4 shadow-[0px_0px_16px_rgba(255,255,255,0.25)]  border-solid border-l-[rgba(255,255,255,0.35)]">
                <div className="row-start-1 px-2 py-1">
                  {users.find((user) => user.playerNum === 1)?.name}
                  <EmptyProfile />
                </div>
                <div className="row-start-1 px-2 py-1">
                  <EmptyProfile />
                </div>
                <div className="row-start-1 px-2 py-1">
                  <EmptyProfile />
                </div>
                <div className="row-start-2 px-2 py-1">
                  <EmptyProfile />
                </div>
                <div className="row-start-2 px-2 py-1">
                  <EmptyProfile />
                </div>
                <div className="row-start-2 px-2 py-1">
                  <EmptyProfile />
                </div>
                <div className="w-full text-base flex flex-col justify-between items-center row-start-3 row-end-5 col-span-3 text-white px-2 py-1">
                  <div className="w-full text-base flex justify-end items-center text-white px-2 py-1">
                    0/6
                  </div>
                  <div className="w-full h-[80%] text-base justify-center items-center bg-[rgb(7,7,10)] border-2 border-solid border-[#B4B4B4] text-white px-2 py-1">
                    <div>시스템 로그</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameRoom;
