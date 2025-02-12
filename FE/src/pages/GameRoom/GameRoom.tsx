import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import PlayerIcon1 from "../../assets/player-icon/player-icon-1.svg";
import PlayerIcon2 from "../../assets/player-icon/player-icon-2.svg";
import PlayerIcon3 from "../../assets/player-icon/player-icon-3.svg";
import PlayerIcon4 from "../../assets/player-icon/player-icon-4.svg";
import PlayerIcon5 from "../../assets/player-icon/player-icon-5.svg";
import PlayerIcon6 from "../../assets/player-icon/player-icon-1.svg";
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
import ChatForm from "./components/ChatForm";
import { useSocket } from "../../hooks/useSocket";
import LeftSideBar from "./components/LeftSideBar";
import RightSideBar from "./components/RightSideBar";

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
interface IEmitDone {
  success: boolean;
  errorMessage: string;
  enterId: number;
}

function GameRoom() {
  const { number: roomId } = useParams();
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const password = searchParams.get("pwd");

  const iconArr = [
    PlayerIcon1,
    PlayerIcon2,
    PlayerIcon3,
    PlayerIcon4,
    PlayerIcon5,
    PlayerIcon6,
  ];

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

  const [roomError, setRoomError] = useState(false);
  const [roomErrorMessage, setRoomErrorMessage] = useState("");

  //플레이어 본인
  //본인 엔터 아이다와 정보 각각
  const [playerEnterId, setPlayerEnterId] = useState<number>();
  const [playerInfo, setPlayerInfo] = useState<IUser | undefined>({
    id: 999,
    playerNum: 1,
    name: "DummyUser",
    token: "dummy-token-123",
    imgNum: 1,
  });

  //방 참여자 전원
  const [users, setUsers] = useState<IUser[]>([
    {
      id: 999,
      playerNum: 1,
      name: "DummyUser",
      token: "dummy-token-123",
      imgNum: 1,
    },
  ]);

  const player1 = useMemo(
    () => users?.find((user) => user.playerNum === 1),
    [users]
  );
  const player2 = useMemo(
    () => users?.find((user) => user.playerNum === 2),
    [users]
  );
  const player3 = useMemo(
    () => users?.find((user) => user.playerNum === 3),
    [users]
  );
  const player4 = useMemo(
    () => users?.find((user) => user.playerNum === 4),
    [users]
  );
  const player5 = useMemo(
    () => users?.find((user) => user.playerNum === 5),
    [users]
  );
  const player6 = useMemo(
    () => users?.find((user) => user.playerNum === 6),
    [users]
  );

  const [messages, setMessages] = useState<IMessage[]>([]);

  const addMessage = (newMessage: IMessage) => {
    //배열의 마지막 요소에서 서버 시간 비교를 통한 정렬 기능이 필요한가?
    setMessages([...messages, newMessage]);
  };

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ block: "end" });
  };

  useEffect(() => {
    if (!socket) return;

    // 방이 생성되었을 때 수신(서버에서 emit('room:create', data)로 보냈다고 가정)

    // 누군가(또는 내가) 방에 입장했을 때 (서버에서 emit('room:enter', data))
    socket.on("room:enter:send", (data: any) => {
      console.log("방 입장 이벤트 수신:", data);
      // 예: { roomId, roomTitle, isPrivate, playing, currentPlayers: [...] }
      if (data.currentPlayers) {
        const newUsers: IUser[] = data.currentPlayers
          .sort((a: any, b: any) => a.enterId - b.enterId)
          .map((player: any, index: number) => ({
            id: player.enterId,
            playerNum: index + 1, // 1부터 시작
            name: player.nickname,
            token: "",
            imgNum: 1,
          }))
          .slice(0, 6);
        setUsers(newUsers);
        console.log(playerInfo);
      }
    });

    // 누군가(또는 내가) 방에서 나갔을 때 (서버에서 send ('room:leave:send', data))
    socket.on("room:leave:send", (data: any) => {
      console.log("방 퇴장 이벤트 수신:", data);
      // 예:
      // 1) 방에서 나간 사람 정보: { enterId: 2, roomId: 1 }
      // 2) 남아 있는 사람 목록 정보: { currentPlayers: [...] }
      if (data.currentPlayers) {
        const newUsers: IUser[] = data.currentPlayers
          .sort((a: any, b: any) => a.enterId - b.enterId)
          .map((player: any, index: number) => ({
            id: player.enterId,
            playerNum: index + 1,
            name: player.nickname,
            token: "",
            imgNum: 1,
          }));
        setUsers(newUsers);
      }
    });

    // 클린업
    return () => {
      socket.off("room:enter:send");
      socket.off("room:leave:send");
    };
  }, [socket]);

  // ----------------------------------------------------
  // 2) 클라이언트에서 서버로 이벤트 emit (방 생성, 입장, 퇴장)
  // ----------------------------------------------------

  const handleEnterRoom = useCallback(() => {
    // 방 입장 요청
    if (!socket) return;
    // roomId는 string일 수 있으니 Number(roomId) 등으로 파싱
    socket.emit(
      "room:enter:emit",
      {
        roomId: Number(roomId),
        password: password,
      },
      ({ success, errorMessage, enterId }: IEmitDone) => {
        if (success) {
          //올바른 방인 경우
          setPlayerEnterId(enterId);
          return;
        } else {
          setRoomError(true);
          setRoomErrorMessage(
            errorMessage || "입장 도중 에러러가 발생했습니다."
          );
          console.log(errorMessage);
          return;
        }
      }
    );
  }, [socket, roomId, playerInfo]);

  const handleLeaveRoom = useCallback(() => {
    // 방 퇴장 요청
    console.log("playerInfo: ", playerInfo);
    console.log("socket: ", socket);
    if (!socket || !playerInfo) return;
    socket.emit(
      "room:leave:emit",
      {
        enterId: playerInfo.id,
        roomId: Number(roomId),
      },
      (response: { success: boolean; errorMessage?: string }) => {
        if (response.success) {
          navigate("/game");
        } else {
          console.error("방 퇴장 중 오류:", response.errorMessage);
        }
      }
    );
  }, [socket, roomId, playerInfo, navigate]);

  //컴포넌트 마운트시 방에 입장
  //반복 입장하지 않기 위해 useRef 사용
  const didEnterRoomRef = useRef(false);

  useEffect(() => {
    if (!playerEnterId) return;
    setPlayerInfo(users.find((u) => u.id === playerEnterId));
  }, [playerEnterId, users]);

  useEffect(() => {
    if (!didEnterRoomRef.current && socket && playerInfo && roomId) {
      handleEnterRoom();
      didEnterRoomRef.current = true; // 다시 못 들어가도록
    }
  }, [socket, playerInfo, roomId, handleEnterRoom]);

  // (1) chat-input-temp 너비를 저장할 state
  const [chatInputWidth, setChatInputWidth] = useState<number>(0);

  useEffect(() => {
    // (3) ResizeObserver를 통한 너비 측정
    const ro = new ResizeObserver((entries) => {
      for (let entry of entries) {
        // contentRect.width가 실제 요소의 넓이
        setChatInputWidth(entry.contentRect.width);
      }
    });
    if (scrollRef.current) {
      ro.observe(scrollRef.current);
    }
    return () => {
      ro.disconnect();
    };
  }, []);

  useEffect(() => {
    // 새로운 메시지가 추가될 때 스크롤을 아래로 이동
    scrollToBottom();
  }, [messages]);
  return (
    <div className="bg-[#07070a]">
      <div className="background-gradient max-w-[90rem] mx-auto px-4 sm:px-4 md:px-6">
        <LeftSideBar
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
                <div
                  className="chat-input fixed z-10 h-10 bottom-4"
                  style={{ width: chatInputWidth }}
                >
                  <ChatForm playerNum={playerInfo?.playerNum} socket={socket} />
                </div>
              </div>
              <RightSideBar players={users} iconArr={iconArr} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameRoom;
