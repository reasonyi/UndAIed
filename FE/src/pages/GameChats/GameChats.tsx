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
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import {
  faPaperPlane,
  faBell,
  faGear,
  faUserGroup,
  faDoorOpen,
  faCircleExclamation,
} from "@fortawesome/free-solid-svg-icons";
import ChatBubble from "./components/ChatBuble";
import SystemBubble from "./components/SystemBubble";

interface IMessage {
  id: number;
  player: number;
  text: string;
  isMine: boolean; // true면 내가 보낸 메시지, false면 상대방 메시지
}

function GameChats() {
  const { number } = useParams();
  const scrollRef = useRef<HTMLDivElement | null>(null);

  //아이콘
  const paperPlane: IconDefinition = faPaperPlane;
  const bell: IconDefinition = faBell;
  const gear: IconDefinition = faGear;
  const userGroup: IconDefinition = faUserGroup;
  const doorOpen: IconDefinition = faDoorOpen;
  const circleExclamation: IconDefinition = faCircleExclamation;

  //클리아언트 소켓 선언
  const socket = new WebSocket(`ws://${window.location.host}`);
  console.log(socket);

  //
  const playerName = [
    "익명1",
    "익명2",
    "익명3",
    "익명4",
    "익명5",
    "익명6",
    "익명7",
    "익명8",
  ];

  const [messages, setMessages] = useState<IMessage[]>([
    { id: 0, player: 10, text: "게임이 시작되었습니다.", isMine: false },
    { id: 1, player: 0, text: "안녕하세요", isMine: false },
    { id: 2, player: 1, text: "ㅎㅇ", isMine: false },
    { id: 3, player: 2, text: "안녕하세요오", isMine: true },
    { id: 4, player: 3, text: "반갑습니다", isMine: false },
    {
      id: 5,
      player: 4,
      text: "안녕, 반가워! 우리 이번 게임 잘해보자^^안녕, 반가워! 우리 이번 게임 잘해보자^^안녕, 반가워! 우리 이번 게임 잘해보자^^",
      isMine: false,
    },
    { id: 6, player: 5, text: "하이", isMine: false },
    { id: 7, player: 6, text: "벌써 누군지 알겠는데", isMine: false },
    { id: 8, player: 7, text: "그러니까", isMine: false },
    { id: 9, player: 2, text: "어쨋든 난 아님", isMine: true },
  ]);

  const addMessage = (newMessage: IMessage) => {
    //배열의 마지막 요소에서 서버 시간 비교를 통한 정렬 기능이 필요한가?
    setMessages([...messages, newMessage]);
  };

  const scrollToBottom = () => {
    scrollRef.current?.scrollIntoView({ block: "end" });
  };

  useEffect(() => {
    // 새로운 메시지가 추가될 때 스크롤을 아래로 이동
    scrollToBottom();
  }, [messages]);

  console.log(scrollRef);
  return (
    <div className="bg-[#07070a]">
      <div className="background-gradient max-w-[90rem] mx-auto px-4 sm:px-4 md:px-6">
        <div className="hidden lg:flex flex-col justify-between fixed z-20 inset-0 left-[max(0px,calc(50%-45rem))] right-auto w-[21rem] pb-10 pt-6 pl-6 pr-4 bg-black bg-opacity-40 shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-r-2 border-solid border-r-[rgba(255,255,255,0.35)]">
          <div className="w-full text-base flex justify-center items-center text-[white] bg-[rgb(7,7,10)] px-1.5 py-1 border-2 border-solid border-[rgba(255,255,255,0.35)] rounded-md">
            No. 001 방 제목
          </div>
          <div className="profile"></div>
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
        <div className="lg:pl-[19.5rem]">
          <div className="max-w-3xl mx-auto xl:max-w-none xl:ml-0 xl:mr-[32rem]">
            <div className="chat-container flex flex-col h-screen overflow-auto">
              {/* 메시지 리스트 영역 */}
              <div className="flex-1 px-4 pt-4">
                {messages.map((msg: IMessage) => {
                  if (msg.player === 10) {
                    return <SystemBubble key={msg.id} message={msg} />;
                  } else {
                    return (
                      <ChatBubble
                        key={msg.id}
                        message={msg}
                        playerName={playerName}
                      />
                    );
                  }
                })}
                <div
                  ref={scrollRef}
                  className="chat-input-temp h-[4.5rem] w-full"
                ></div>
                <div className="chat-input fixed h-10 bottom-4 w-[calc(90rem-21rem-33.5rem-2rem)]">
                  <form
                    className="w-full h-full bg-[#07070a4d] focus-within:bg-neutral-900 rounded-lg shadow-lg border-2 border-solid border-[#555555] px-4 flex items-center text-sm"
                    action=""
                  >
                    <input
                      className="bg-transparent text-[#848484] focus:text-[#dddddd] w-full"
                      placeholder="채팅 입력하기"
                      type="text"
                    />
                    <div className="bg-[#848484] w-[1px] h-6"></div>
                    <button className="w-6 h-6 ml-2">
                      <FontAwesomeIcon
                        icon={paperPlane}
                        className="text-[#848484]"
                      />
                    </button>
                  </form>
                </div>
              </div>
              <div className="fixed z-20 right-[max(0px,calc(50%-45rem))] w-[33.5rem] py-6 px-3 hidden h-screen bg-black bg-opacity-40 xl:grid grid-cols-3 grid-rows-4 gap-4 shadow-[0px_0px_16px_rgba(255,255,255,0.25)]  border-solid border-l-[rgba(255,255,255,0.35)]">
                <div className="row-start-1 px-2 py-1">
                  <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,0,0,0.5)] hover:shadow-[0px_0px_16px_rgba(255,0,0,0.45)]">
                    {/* <div className="hover:shadow-[0px_0px_16px_rgba(255,0,0,0.45)] w-full h-full hover:animate-ping text-white"></div> */}
                    <div className="flex justify-center items-center px-4 pt-1 pb-3">
                      {/* 죽었을 때 변화 이미지 */}
                      {/* <img className="filter grayscale sepia brightness-75 contrast-125" src={PlayerIcon1} /> */}
                      <img className="filter brightness-75" src={PlayerIcon1} />
                    </div>
                    <div></div>
                  </div>
                </div>
                <div className="row-start-1 px-2 py-1">
                  <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,255,255,0.5)] hover:shadow-[0px_0px_16px_rgba(255,255,255,0.45)]">
                    <div className="flex justify-center items-center px-4 pt-1 pb-3">
                      <img className="filter brightness-75" src={PlayerIcon2} />
                    </div>
                    <div></div>
                  </div>
                </div>
                <div className="row-start-1 px-2 py-1">
                  <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,255,255,0.5)] hover:shadow-[0px_0px_16px_rgba(255,255,255,0.45)]">
                    <div className="flex justify-center items-center px-4 pt-1 pb-3">
                      <img className="filter brightness-75" src={PlayerIcon3} />
                    </div>
                    <div></div>
                  </div>
                </div>
                <div className="row-start-2 px-2 py-1">
                  <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,255,255,0.5)] hover:shadow-[0px_0px_16px_rgba(255,255,255,0.45)]">
                    <div className="flex justify-center items-center px-4 pt-1 pb-3">
                      <img className="filter brightness-75" src={PlayerIcon4} />
                    </div>
                    <div></div>
                  </div>
                </div>
                <div className="row-start-2 px-2 py-1">
                  <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,255,255,0.5)] hover:shadow-[0px_0px_16px_rgba(255,255,255,0.45)]">
                    <div className="flex justify-center items-center px-4 pt-1 pb-3">
                      <img className="filter brightness-75" src={PlayerIcon5} />
                    </div>
                    <div></div>
                  </div>
                </div>
                <div className="row-start-2 px-2 py-1">
                  <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,255,255,0.5)] hover:shadow-[0px_0px_16px_rgba(255,255,255,0.45)]">
                    <div className="flex justify-center items-center px-4 pt-1 pb-3">
                      <img className="filter brightness-75" src={PlayerIcon6} />
                    </div>
                    <div></div>
                  </div>
                </div>
                <div className="row-start-3 px-2 py-1">
                  <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,255,255,0.5)] hover:shadow-[0px_0px_16px_rgba(255,255,255,0.45)]">
                    <div className="flex justify-center items-center px-4 pt-1 pb-3">
                      <img className="filter brightness-75" src={PlayerIcon7} />
                    </div>
                    <div></div>
                  </div>
                </div>
                <div className="row-start-3 px-2 py-1">
                  <div className="shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-2 border-solid border-[rgba(255,255,255,0.35)] w-full h-full hover:border-[rgba(255,255,255,0.5)] hover:shadow-[0px_0px_16px_rgba(255,255,255,0.45)]">
                    <div className="flex justify-center items-center px-4 pt-1 pb-3">
                      <img className="filter brightness-75" src={PlayerIcon8} />
                    </div>
                    <div></div>
                  </div>
                </div>
                <div className="row-start-3">10</div>
                <div className="w-full text-base justify-center items-center bg-[rgb(7,7,10)] border-2 border-solid border-[#B4B4B4] col-span-3 text-white px-2 py-1">
                  <div>시스템 로그</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameChats;
