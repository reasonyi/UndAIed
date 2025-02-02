interface IMessage {
  id: number;
  player: number;
  text: string;
  isMine: boolean; // true면 내가 보낸 메시지, false면 상대방 메시지
}

interface SystemBubbleProps {
  message: IMessage;
}

function SystemBubble({ message }: SystemBubbleProps) {
  return (
    <div className="flex mb-4 justify-center">
      <div
        className={`w-[90%] my-2 py-2 px-3 text-sm font-semibold bg-[rgb(9,9,11)] text-red-500 border-2 border-solid border-[#555555]`}
      >
        {message.text}
      </div>
    </div>
  );
}

export default SystemBubble;
