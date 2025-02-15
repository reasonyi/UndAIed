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
import ChatBubble from "./components/ChatBuble";
import SystemBubble from "./components/SystemBubble";
import ChatForm from "./components/ChatForm";
import { useSocket } from "../../hooks/useSocket";
import RightGameSideBar from "./components/RightGameSideBar";
import LeftGameSideBar from "./components/LeftGameSideBar";
import { IMessage } from "../../types/gameroom";
import { IAnonimus } from "../../types/gameplay";
import { toast } from "sonner";

interface IChatSend {
  number: number;
  content: string;
}

interface IGameInfoSend {
  stage: string;
  timer: number;
  players: IAnonimus[];
}

interface IGameInfoEmitDone {
  success: boolean;
  errorMessage: string;
  playerId: number;
}

interface IGameChatEmitDone {
  success: boolean;
  errorMessage?: string;
  data: null;
}

function GamePlay() {
  const { number } = useParams();
  const scrollRef = useRef<HTMLDivElement | null>(null);
  const navigate = useNavigate();

  //플레이어 본인
  const [playerEnterId, setPlayerEnterId] = useState<number>();
  const [playerInfo, setPlayerInfo] = useState<IAnonimus | undefined>();

  //게임 전체 정보보 (유저 정보 포함)
  const [gameInfo, setGameInfo] = useState<IGameInfoSend>();

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
    // {
    //   player: 0,
    //   nickname: "사회자",
    //   text: "게임이 시작되었습니다.",
    //   isMine: false,
    // },
    // { player: 1, text: "안녕하세요", isMine: false, nickname: "익명1" },
    // { player: 2, text: "ㅎㅇ", isMine: false, nickname: "익명2" },
    // { player: 3, text: "안녕하세요오", isMine: true, nickname: "익명3" },
    // { player: 4, text: "반갑습니다", isMine: false, nickname: "익명4" },
    // {
    //   player: 5,
    //   text: "안녕, 반가워! 우리 이번 게임 잘해보자^^안녕, 반가워! 우리 이번 게임 잘해보자^^안녕, 반가워! 우리 이번 게임 잘해보자^^",
    //   isMine: false,
    //   nickname: "익명5",
    // },
    // { player: 6, text: "하이", isMine: false, nickname: "익명6" },
    // {
    //   player: 7,
    //   text: "벌써 누군지 알겠는데",
    //   isMine: false,
    //   nickname: "익명7",
    // },
    // { player: 8, text: "그러니까", isMine: false, nickname: "익명8" },
    // { player: 3, text: "어쨋든 난 아님", isMine: true, nickname: "익명3" },
  ]);

  //socket 훅 사용
  const socket = useSocket();

  useEffect(() => {
    if (!socket) {
      console.log("소켓 없음!");
      return;
    }
    console.log("소켓 생김!");

    //여기서 받는 데이터는 data.아래에 바로 데이터 존재
    socket.on("game:info:send", (data: IGameInfoSend) => {
      console.log("game:info:send 발생! data 수신:", data);

      debugger;

      if (data.players) {
        setGameInfo(data);
      }

      debugger;
    });

    return () => {
      socket.off("game:info:send");
    };
  }, [socket, playerEnterId, gameInfo]);

  useEffect(() => {
    if (!socket) {
      console.log("소켓 없음!");
      return;
    }
    console.log("소켓 생김!");

    socket.on("room:chat:send", (data: IChatSend) => {
      console.log("chat:send 발생! data 수신:", data);
      if (data.content && gameInfo) {
        const player = gameInfo.players.find(
          (player) => player.number === data.number
        );

        if (player) {
          const newMessage: IMessage = {
            player: player.number,
            nickname: `익명${player.number}`,
            text: data.content,
            isMine: Boolean(player.number === playerEnterId),
          };
          setMessages([...messages, newMessage]);
        }
      }
    });

    return () => {
      socket.off("room:chat:send");
    };
  }, [socket, messages, playerEnterId, gameInfo]);

  //emit 이벤트 모음
  const handleGameInfo = useCallback(() => {
    // 방 입장 요청
    if (!socket) {
      console.log("enter: socket이 존재하지 않습니다");
      return;
    }
    console.log("emit 발생!");

    socket.emit(
      "game:info:emit",
      {},
      (data: { success: boolean; errorMessage: string; number: number }) => {
        //여기서 받는 건 없음

        if (data.success) {
          setPlayerEnterId(data.number);
          return;
        } else {
          console.log(data.errorMessage);
          toast.error(data.errorMessage);
          return;
        }
      }
    );
  }, [socket, playerEnterId]);

  //실행하기
  useEffect(() => {
    if (socket) {
      handleGameInfo();
    }
  }, [socket, handleGameInfo]);

  //나 찾기기
  useEffect(() => {
    setPlayerInfo(
      gameInfo?.players.find((player) => player.number === playerEnterId)
    );
  }, [gameInfo, playerEnterId]);

  const handleGameChat = useCallback(
    (input: string) => {
      if (!socket || !playerEnterId) {
        console.log("enter: socket 또는 playerEnterId이 존재하지 않습니다");
        return;
      }
      socket.emit(
        "game:chat:emit",
        {
          message: input,
        },
        ({ success, errorMessage, data }: IGameChatEmitDone) => {
          if (success) {
          } else {
            console.error("채팅 전송 오류:", errorMessage);
            toast.error(errorMessage);
          }
        }
      );
    },
    [socket, playerEnterId]
  );

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ block: "end" });
  };
  //socket 시작작

  useEffect(() => {
    // 새로운 메시지가 추가될 때 스크롤을 아래로 이동
    scrollToBottom();
  }, [messages]);

  return (
    <div className="bg-[#07070a]">
      <div className="background-gradient max-w-[90rem] mx-auto px-4 sm:px-4 md:px-6">
        <LeftGameSideBar
          nickname={
            playerInfo
              ? "익명" + String(playerInfo.number)
              : "연결이 끊어졌습니다."
          }
          icon={iconArr[playerInfo ? playerInfo.number : 1]}
          socket={socket}
          // onLeaveRoom={handleLeaveRoom}
        />
        <div className="lg:pl-[19.5rem]">
          <div className="max-w-3xl mx-auto xl:max-w-none xl:ml-0 xl:mr-[32rem]">
            <div className="chat-container flex flex-col h-screen overflow-auto">
              {/* 메시지 리스트 영역 */}
              <div className="flex-1 px-5 pt-4">
                {messages.map((msg: IMessage, index) => {
                  if (msg.player === 10) {
                    return <SystemBubble key={index} message={msg} />;
                  } else {
                    return (
                      <ChatBubble
                        key={index}
                        message={msg}
                        playerName={`익명${msg.player}`}
                      />
                    );
                  }
                })}
                <div
                  ref={scrollRef}
                  className="chat-input-temp h-[4.5rem] w-full"
                ></div>
                <div className="chat-input fixed h-10 bottom-4 w-[calc(90rem-21rem-33.5rem-2rem)]">
                  <ChatForm socket={socket} onSendChat={handleGameChat} />
                </div>
              </div>
              <RightGameSideBar players={gameInfo?.players} iconArr={iconArr} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GamePlay;
