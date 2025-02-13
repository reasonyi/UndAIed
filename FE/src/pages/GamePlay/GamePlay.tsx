import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
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
import ChatBubble from "./components/ChatBuble";
import SystemBubble from "./components/SystemBubble";
import ChatForm from "./components/ChatForm";
import { useSocket } from "../../hooks/useSocket";
import RightGameSideBar from "./components/RightGameSideBar";
import LeftGameSideBar from "./components/LeftGameSideBar";

interface IUser {
  id: number;
  playerNum: number;
  name: string;
  isDied: boolean;
  imgNum: number;
}

interface IMessage {
  id: number;
  player: number;
  text: string;
  isMine: boolean; // true면 내가 보낸 메시지, false면 상대방 메시지
}

function GamePlay() {
  const { number } = useParams();
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const navigate = useNavigate();

  //플레이어 본인
  const [playerEnterId, setPlayerEnterId] = useState<number>();
  const [playerInfo, setPlayerInfo] = useState<IUser | undefined>({
    id: 999,
    playerNum: 1,
    name: "DummyUser",
    isDied: true,
    imgNum: 1,
  });

  //방 참여자 전원 정보보
  const [users, setUsers] = useState<IUser[]>([
    { id: 1, playerNum: 1, name: "익명1", isDied: true, imgNum: 1 },
    { id: 2, playerNum: 2, name: "익명2", isDied: true, imgNum: 2 },
    { id: 3, playerNum: 3, name: "익명3", isDied: true, imgNum: 3 },
    { id: 4, playerNum: 4, name: "익명4", isDied: false, imgNum: 4 },
    { id: 5, playerNum: 5, name: "익명5", isDied: true, imgNum: 5 },
    { id: 6, playerNum: 6, name: "익명6", isDied: true, imgNum: 6 },
    { id: 7, playerNum: 7, name: "익명7", isDied: true, imgNum: 7 },
    { id: 8, playerNum: 8, name: "익명8", isDied: false, imgNum: 8 },
  ]);

  //아이콘
  const bell: IconDefinition = faBell;
  const gear: IconDefinition = faGear;
  const userGroup: IconDefinition = faUserGroup;
  const doorOpen: IconDefinition = faDoorOpen;
  const circleExclamation: IconDefinition = faCircleExclamation;

  //유저 아이콘
  const iconArr = [
    PlayerIcon1,
    PlayerIcon2,
    PlayerIcon3,
    PlayerIcon4,
    PlayerIcon5,
    PlayerIcon6,
    PlayerIcon7,
    PlayerIcon8,
  ];

  const [messages, setMessages] = useState<IMessage[]>([
    { id: 0, player: 10, text: "게임이 시작되었습니다.", isMine: false },
    // { id: 1, player: 0, text: "안녕하세요", isMine: false },
    // { id: 2, player: 1, text: "ㅎㅇ", isMine: false },
    // { id: 3, player: 2, text: "안녕하세요오", isMine: true },
    // { id: 4, player: 3, text: "반갑습니다", isMine: false },
    // {
    //   id: 5,
    //   player: 4,
    //   text: "안녕, 반가워! 우리 이번 게임 잘해보자^^안녕, 반가워! 우리 이번 게임 잘해보자^^안녕, 반가워! 우리 이번 게임 잘해보자^^",
    //   isMine: false,
    // },
    // { id: 6, player: 5, text: "하이", isMine: false },
    // { id: 7, player: 6, text: "벌써 누군지 알겠는데", isMine: false },
    // { id: 8, player: 7, text: "그러니까", isMine: false },
    // { id: 9, player: 2, text: "어쨋든 난 아님", isMine: true },
  ]);

  //socket 훅 사용
  const socket = useSocket();

  const addMessage = (newMessage: IMessage) => {
    //배열의 마지막 요소에서 서버 시간 비교를 통한 정렬 기능이 필요한가?
    setMessages([...messages, newMessage]);
  };

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ block: "end" });
  };

  //socket 시작작
  //IMessage 형식으로 받아야 하나?
  useEffect(() => {
    if (!socket) {
      console.log("socket이 없습니다");
      return;
    }
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

  //게임 나가기 (방)
  const handleLeaveRoom = useCallback(() => {
    // 방 퇴장 요청
    console.log("playerInfo: ", playerInfo);
    console.log("socket: ", socket);
    if (!socket || !playerInfo) return;
  }, [socket, playerInfo, navigate]);

  useEffect(() => {
    // 새로운 메시지가 추가될 때 스크롤을 아래로 이동
    scrollToBottom();
  }, [messages]);

  console.log(scrollRef);
  return (
    <div className="bg-[#07070a]">
      <div className="background-gradient max-w-[90rem] mx-auto px-4 sm:px-4 md:px-6">
        <LeftGameSideBar
          nickname={playerInfo ? playerInfo.name : "연결이 끊어졌습니다."}
          icon={PlayerIcon1}
          socket={socket}
          onLeaveRoom={handleLeaveRoom}
        />
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
              <RightGameSideBar players={users} iconArr={iconArr} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GamePlay;
