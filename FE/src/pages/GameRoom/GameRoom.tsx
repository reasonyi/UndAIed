import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import PlayerIcon1 from "../../assets/player-icon/player-icon-1.svg";
import PlayerIcon2 from "../../assets/player-icon/player-icon-2.svg";
import PlayerIcon3 from "../../assets/player-icon/player-icon-3.svg";
import PlayerIcon4 from "../../assets/player-icon/player-icon-4.svg";
import PlayerIcon5 from "../../assets/player-icon/player-icon-5.svg";
import PlayerIcon6 from "../../assets/player-icon/player-icon-1.svg";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import { toast } from "sonner";
import ChatBubble from "../GamePlay/components/ChatBuble";
import SystemBubble from "../GamePlay/components/SystemBubble";
import ChatForm from "./components/ChatForm";
import { useSocket } from "../../hooks/useSocket";
import LeftSideBar from "./components/LeftSideBar";
import RightSideBar from "./components/RightSideBar";
import { IPlayer } from "../../types/gameroom";

interface IRoomInfo {
  roomId: number;
  roomTitle: string;
  isPrivate: boolean;
  playing: boolean;
  currentPlayers: IPlayer[];
}

interface IMessage {
  id: number;
  player: number;
  text: string;
  isMine: boolean; // true면 내가 보낸 메시지, false면 상대방 메시지
}
interface IEmitDone {
  success: boolean;
  errorMessage: string;
  enterId: number;
  data: IRoomInfo;
}

function GameRoom() {
  const { number: roomIdParam } = useParams();
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

  const [roomError, setRoomError] = useState(false);
  const [roomErrorMessage, setRoomErrorMessage] = useState("");

  const [roomTitle, setRoomTitle] = useState("temp");
  const [roomIdState, setRoomIdState] = useState(0);

  //본인 엔터 아이다와 정보 각각
  const [playerEnterId, setPlayerEnterId] = useState<number>();
  const [playerInfo, setPlayerInfo] = useState<IPlayer | undefined>();

  //방 참여자 전원 더미
  const [roomInfo, setRoomInfo] = useState<IRoomInfo>();

  const [messages, setMessages] = useState<IMessage[]>([]);

  const addMessage = (newMessage: IMessage) => {
    //배열의 마지막 요소에서 서버 시간 비교를 통한 정렬 기능이 필요한가?
    setMessages([...messages, newMessage]);
  };

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ block: "end" });
  };

  useEffect(() => {
    if (!socket) {
      console.log("소켓 없음!");
      return;
    }
    console.log("소켓 생김!");

    //여기서 받는 데이터는 data.아래에 바로 데이터 존재
    socket.on("room:enter:send", (data: IRoomInfo) => {
      console.log("send 발생! data 수신:", data);
      debugger;
      if (data.currentPlayers) {
        debugger;
        const newUsers: IPlayer[] = data.currentPlayers.sort(
          (a: IPlayer, b: IPlayer) => a.enterId - b.enterId
        );
        const data_ = data;
        data_.currentPlayers = newUsers;
        setRoomInfo(data_);
      }
    });

    // 누군가(또는 내가) 방에서 나갔을 때 (서버에서 send ('room:leave:send', data))
    // socket.on("room:leave:send", (data: any) => {
    //   console.log("방 퇴장 이벤트 수신:", data);
    //   // 예:
    //   // 1) 방에서 나간 사람 정보: { enterId: 2, roomId: 1 }
    //   // 2) 남아 있는 사람 목록 정보: { currentPlayers: [...] }
    //   if (data.currentPlayers) {
    //     const newUsers: IUser[] = data.currentPlayers.sort(
    //       (a: IUser, b: IUser) => a.playerNum - b.playerNum
    //     );
    //     setUsers(newUsers);
    //   }
    // });

    // socket.on("game:init:send", (data: any) => {
    //   console.log("게임으로 이동 이벤트 수신:", data);
    //   navigate(`game/play/${data.gameId}`);
    // });
    return () => {
      socket.off("room:enter:send");
      // socket.off("room:leave:send");
      // socket.off("game:init:send");
    };
  }, [socket]);

  //나 찾기기
  useEffect(() => {
    setPlayerInfo(
      roomInfo?.currentPlayers.find(
        (player) => player.enterId === playerEnterId
      )
    );
  }, [roomInfo, playerEnterId]);

  const handleEnterRoom = useCallback(() => {
    // 방 입장 요청
    if (!socket || !roomIdParam) {
      console.log("enter: socket 또는 roomIdParam이이 존재하지 않습니다");
      return;
    }
    console.log("emit 발생!");

    socket.emit(
      "room:enter:emit",
      {
        roomId: Number(roomIdParam),
        password: password,
      },
      ({ success, errorMessage, enterId, data }: IEmitDone) => {
        //여기서 받는 건 없음
        if (success) {
          setPlayerEnterId(enterId);
          debugger;
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
  }, [socket, roomIdParam]);

  // const handleLeaveRoom = useCallback(() => {
  //   // 방 퇴장 요청
  //   if (!socket || !playerInfo) {
  //     console.log("playerInfo: ", playerInfo);
  //     console.log("socket: ", socket);
  //     return;
  //   }
  //   socket.emit(
  //     "room:leave:emit",
  //     {
  //       roomId: Number(roomIdParam),
  //     },
  //     (response: { success: boolean; errorMessage?: string }) => {
  //       if (response.success) {
  //         navigate("/game");
  //       } else {
  //         console.error("방 퇴장 중 오류:", response.errorMessage);
  //       }
  //     }
  //   );
  // }, [socket, roomIdParam, playerInfo, navigate]);

  // const handleGameStart = useCallback(() => {
  //   if (!socket || !playerInfo) {
  //     console.log("playerInfo: ", playerInfo);
  //     console.log("socket: ", socket);
  //     return;
  //   }
  //   //방장 정보?
  //   socket.emit(
  //     "game:init:emit",
  //     { roomId: Number(roomIdParam) },
  //     (response: { success: boolean; errorMessage?: string }) => {
  //       if (response.success) {
  //       } else {
  //         //게임 시작 실패 안내내
  //         toast.error(response.errorMessage);
  //       }
  //     }
  //   );
  // }, [socket, playerInfo]);

  useEffect(() => {
    if (socket && roomIdParam) {
      handleEnterRoom();
    }
  }, [socket, roomIdParam, handleEnterRoom]);

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
          roomId={roomIdState}
          roomTitle={roomTitle}
          nickname={playerInfo ? playerInfo.nickname : "연결이 끊어졌습니다."}
          icon={PlayerIcon1}
          socket={socket}
          // onLeaveRoom={handleLeaveRoom}
          // onGameStart={handleGameStart}
          player={playerInfo}
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
                          roomInfo?.currentPlayers.find(
                            (user) => user.enterId === msg.player
                          )?.nickname
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
                  <ChatForm playerNum={playerInfo?.enterId} socket={socket} />
                </div>
              </div>
              <RightSideBar
                players={roomInfo?.currentPlayers}
                iconArr={iconArr}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameRoom;
