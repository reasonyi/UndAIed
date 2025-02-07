import PlayerIcon1 from "../../../assets/player-icon/player-icon-1.svg";
import PlayerIcon2 from "../../../assets/player-icon/player-icon-2.svg";
import PlayerIcon3 from "../../../assets/player-icon/player-icon-3.svg";
import PlayerIcon4 from "../../../assets/player-icon/player-icon-4.svg";
import PlayerIcon5 from "../../../assets/player-icon/player-icon-5.svg";
import PlayerIcon6 from "../../../assets/player-icon/player-icon-1.svg";
import PlayerIcon7 from "../../../assets/player-icon/player-icon-2.svg";
import PlayerIcon8 from "../../../assets/player-icon/player-icon-3.svg";

interface IMessage {
  id: number;
  player: number;
  text: string;
  isMine: boolean; // true면 내가 보낸 메시지, false면 상대방 메시지
}

interface ChatBubbleProps {
  message: IMessage;
  playerName: string | undefined;
}

function ChatBubble({ message, playerName }: ChatBubbleProps) {
  const isMine = message.isMine;

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

  return (
    <div>
      {/* 닉네임/아이콘 영역 */}
      <div
        className={`flex mb-1 items-center ${
          isMine ? "flex-row-reverse" : "justify-start"
        }`}
      >
        <img
          className="w-6 h-6 mr-1"
          src={
            playerColor[message.player]
              ? playerColor[message.player][3]
              : undefined
          }
          alt="player-icon"
        />
        <span className="text-white">{playerName}</span>
      </div>

      {/* 말풍선(채팅) 영역 */}
      <div className={`flex mb-4 ${isMine ? "justify-end" : "justify-start"}`}>
        <div
          className={`max-w-[70%] py-2 px-3 text-sm bg-[rgb(9,9,11)] text-[#dddddd] border-2 border-solid shadow-[0px_0px_14px_rgba(255,255,255,0.25)] break-all ${
            isMine ? "rounded-b-lg rounded-tl-lg" : "rounded-b-lg rounded-tr-lg"
          }`}
          style={{
            borderColor: `rgba(${playerColor[message.player][0]}, 
              ${playerColor[message.player][1]}, 
              ${playerColor[message.player][2]}, 0.4)`,
          }}
        >
          {message.text}
        </div>
      </div>
    </div>
  );
}

export default ChatBubble;
