import { useState } from "react";
import { useParams } from "react-router-dom";

interface IMessage {
  id: number;
  text: string;
  isMine: boolean; // true면 내가 보낸 메시지, false면 상대방 메시지
}

function GameChats() {
  const { number } = useParams();

  const [messages, setMessages] = useState<IMessage[]>([
    { id: 1, text: "안녕하세요요", isMine: false },
    { id: 2, text: "난 ai 아님.", isMine: true },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
    { id: 3, text: "나도 아님님", isMine: false },
  ]);

  return (
    <div className="bg-[#07070a]">
      <div className="background-gradient max-w-8xl mx-auto px-4 sm:px-4 md:px-6">
        <div className="hidden lg:block fixed z-20 inset-0 left-[max(0px,calc(50%-45rem))] right-auto w-[18rem] pb-10 pt-6 pl-6 pr-4">
          <div className="w-full text-base flex justify-center items-center text-[white] bg-[rgb(7,7,10)] px-1.5 py-1 border-2 border-solid border-[#B4B4B4]">
            No. 001 방 제목
          </div>
        </div>
        <div className="lg:pl-[18rem]">
          <div className="max-w-3xl mx-auto xl:max-w-none xl:ml-0 xl:mr-[28.5rem]">
            <div className="chat-container flex flex-col h-screen overflow-auto">
              {/* 메시지 리스트 영역 */}
              <div className="flex-1 p-4 md:p-6 xl:p-4">
                {messages.map((msg: IMessage) => (
                  <div
                    key={msg.id}
                    // 내가 보낸 메시지면 오른쪽 정렬, 상대방 메시지면 왼쪽 정렬
                    className={`flex mb-4 ${
                      msg.isMine ? "justify-end" : "justify-start"
                    }`}
                  >
                    <div
                      className={`max-w-xs p-2 leading-5 bg-[rgb(9,9,11)] text-[white] text-base border-2 border-solid border-[#fd3c2f]/60 shadow-[0px_0px_14px_rgba(253,60,47,0.25)] ${
                        msg.isMine
                          ? "rounded-b-lg rounded-tl-lg"
                          : "rounded-b-lg rounded-tr-lg"
                      }`}
                    >
                      {msg.text}
                    </div>
                  </div>
                ))}
              </div>
              <div className="fixed z-20 right-[max(0px,calc(50%-45rem))] w-[30rem] pt-2 hidden xl:block h-screen bg-black bg-opacity-40"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameChats;
