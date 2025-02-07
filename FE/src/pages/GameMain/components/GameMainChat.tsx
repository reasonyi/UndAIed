function GameMainChat() {
  const blockStyle =
    "bg-[#0000006c] border border-[#f74a5c]/40 backdrop-blur-[12.20px] text-[#fffbfb] rounded-[5px] transition-all duration-200";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";

  return (
    <div className={`flex flex-col p-4 bg-[#0000008f] h-52   ${blockStyle} `}>
      <div className="flex-1 min-h-[8rem] max-h-[12rem] overflow-y-auto">
        kub938: ㅎㅇ
      </div>
      <input
        placeholder="채팅 입력"
        type="text"
        className={`p-4 h-8  bg-[#241818de]  ${blockStyle} transition-colors duration-50 focus:border-2 focus:border-[#ff6767] focus:outline-none focus:bg-[#1122338e]`}
      />
    </div>
  );
}

export default GameMainChat;
