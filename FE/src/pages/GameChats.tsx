import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import PlayerIcon1 from "../assets/player-icon/player-icon-1.svg";
import PlayerIcon2 from "../assets/player-icon/player-icon-2.svg";
import PlayerIcon3 from "../assets/player-icon/player-icon-3.svg";
import PlayerIcon4 from "../assets/player-icon/player-icon-4.svg";
import PlayerIcon5 from "../assets/player-icon/player-icon-5.svg";
import PlayerIcon6 from "../assets/player-icon/player-icon-1.svg";
import PlayerIcon7 from "../assets/player-icon/player-icon-2.svg";
import PlayerIcon8 from "../assets/player-icon/player-icon-3.svg";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import { faPaperPlane } from "@fortawesome/free-solid-svg-icons";

interface IMessage {
  id: number;
  player: number;
  text: string;
  isMine: boolean; // true면 내가 보낸 메시지, false면 상대방 메시지
}

function GameChats() {
  const { number } = useParams();
  const scrollRef = useRef<HTMLDivElement>(null);

  //아이콘
  const paperPlane: IconDefinition = faPaperPlane;

  //클리아언트 소켓 선언
  const socket = new WebSocket(`ws://${window.location.host}`);
  console.log(socket);

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

  const playerColor = [
    ["253", "60", "47", PlayerIcon1],
    ["252", "158", "37", PlayerIcon2],
    ["250", "255", "144", PlayerIcon3],
    ["59", "140", "51", PlayerIcon4],
    ["45", "58", "201", PlayerIcon5],
    ["176", "65", "253", PlayerIcon6],
    ["253", "99", "206", PlayerIcon7],
    ["255", "255", "255", PlayerIcon8],
  ];

  const [messages, setMessages] = useState<IMessage[]>([
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

  useEffect(() => {
    // 새로운 메시지가 추가될 때 스크롤을 아래로 이동
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  console.log(scrollRef);
  return (
    <div className="bg-[#07070a]">
      <div className="background-gradient max-w-[90rem] mx-auto px-4 sm:px-4 md:px-6">
        <div className="hidden lg:block fixed z-20 inset-0 left-[max(0px,calc(50%-45rem))] right-auto w-[21rem] pb-10 pt-6 pl-6 pr-4 bg-black bg-opacity-40 shadow-[0px_0px_16px_rgba(255,255,255,0.25)] border-r-2 border-solid border-r-[rgba(255,255,255,0.35)]">
          <div className="w-full text-base flex justify-center items-center text-[white] bg-[rgb(7,7,10)] px-1.5 py-1 border-2 border-solid border-[#B4B4B4]">
            No. 001 방 제목
          </div>
        </div>
        <div className="lg:pl-[19.5rem]">
          <div className="max-w-3xl mx-auto xl:max-w-none xl:ml-0 xl:mr-[32rem]">
            <div className="chat-container flex flex-col h-screen overflow-auto">
              {/* 메시지 리스트 영역 */}
              <div ref={scrollRef} className="flex-1 px-4 pt-4">
                {messages.map((msg: IMessage) => (
                  <div key={msg.id}>
                    <div
                      className={`flex mb-1 items-center ${
                        msg.isMine ? "flex-row-reverse" : "justify-start"
                      }`}
                    >
                      <img
                        className="w-6 h-6 mr-1"
                        src={playerColor[msg.player][3]}
                        alt=""
                      />
                      <span className="text-white">
                        {playerName[msg.player]}
                      </span>
                    </div>
                    <div
                      // 내가 보낸 메시지면 오른쪽 정렬, 상대방 메시지면 왼쪽 정렬
                      className={`flex mb-4 ${
                        msg.isMine ? "justify-end" : "justify-start"
                      }`}
                    >
                      <div
                        className={`max-w-[70%] py-2 px-3 text-sm bg-[rgb(9,9,11)] text-[#dddddd] border-2 border-solid shadow-[0px_0px_14px_rgba(255,255,255,0.25)] ${
                          msg.isMine
                            ? "rounded-b-lg rounded-tl-lg"
                            : "rounded-b-lg rounded-tr-lg"
                        }`}
                        style={{
                          borderColor: `rgba(${playerColor[msg.player][0]},${
                            playerColor[msg.player][1]
                          },${playerColor[msg.player][2]}, 0.4)`,
                        }}
                      >
                        {msg.text}
                      </div>
                    </div>
                  </div>
                ))}
                <div className="chat-input-temp h-[4.5rem] w-full"></div>
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
                <div className="col-span-3 text-white">시스템 로그</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameChats;
