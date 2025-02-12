import { faPaperPlane } from "@fortawesome/free-solid-svg-icons";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { useForm, FieldErrors } from "react-hook-form";
import { toast } from "sonner";
import { Socket } from "socket.io-client";

interface IFormProps {
  playerNum: number | undefined;
  socket: Socket | null; // 부모에서 전달받을 소켓 인스턴스
}
interface IForm {
  chat: string;
}

function ChatForm({ playerNum, socket }: IFormProps) {
  const paperPlane: IconDefinition = faPaperPlane;
  const {
    register,
    handleSubmit,
    resetField,
    formState: { errors },
  } = useForm<IForm>();

  const onValidSubmit = async (data: IForm, e?: React.BaseSyntheticEvent) => {
    //local storage에서 토큰 가져오기, player num 가져오기기
    //axios api 작성성
    e?.preventDefault();

    if (!socket) {
      console.log("socket이 없습니다");
      return;
    }

    try {
      // 1) 서버로 메시지 전송
      // 서버에서도 "chat" 이벤트를 수신하여 모든 클라이언트에게 broadcast하도록 구현
      const newMessage = {
        id: Date.now(), // 간단히 임시 ID
        player: playerNum,
        text: data.chat,
      };
      socket.emit("chat1", newMessage);

      // 메시지 전송 로직 성공 시 input 비우기
      resetField("chat");
    } catch (error) {
      // 에러 처리
      console.error(error);
    }
  };

  const onInvalidSubmit = (errors: FieldErrors<IForm>) => {
    if (errors.chat?.type === "required") {
      // "빈 채팅을 입력할 수 없습니다" 에러 발생 시, 원하는 custom function 실행
      toast.error(errors.chat.message);
    }
  };

  return (
    <form
      className="w-full h-full bg-[#07070a4d] focus-within:bg-neutral-900 rounded-lg shadow-lg border-2 border-solid border-[#555555] px-4 flex items-center text-sm"
      onSubmit={handleSubmit(onValidSubmit, onInvalidSubmit)}
      action=""
    >
      <input
        {...register("chat", {
          required: "빈 채팅을 입력할 수 없습니다.",
          maxLength: {
            value: 200,
            message: "최대 200자리까지 입력할 수 있습니다다",
          },
        })}
        className="bg-transparent text-[#848484] focus:text-[#dddddd] w-full"
        placeholder="채팅 입력하기"
        type="text"
        autoComplete="off"
      />
      <div className="bg-[#848484] w-[1px] h-6"></div>
      <button className="w-6 h-6 ml-2">
        <FontAwesomeIcon icon={paperPlane} className="text-[#848484]" />
      </button>
    </form>
  );
}

export default ChatForm;
