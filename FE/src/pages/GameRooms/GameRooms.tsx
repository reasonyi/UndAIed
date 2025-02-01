import logo from "../../assets/logo.png";
import bgImg from "../../assets/game-rooms-background.png";
import power from "../../assets/icon/power.png";
import playerIcon from "../../assets/player-icon/player-icon-1.svg";
import settingIcon from "../../assets/icon/setting.png";
import bellIcon from "../../assets/icon/bell.svg";
import friendsIcon from "../../assets/icon/friends.svg";

function GameRooms() {
  const blockStyle =
    "bg-[#5349507a] border border-[#f74a5c]/60 backdrop-blur-[12.20px] text-[#fffbfb]  rounded-[5px]  transition-all duration-200 ";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";
  const footerButtonStyle =
    "bg-black px-3 py-1 border-t-2  hover:bg-[#4e1b26]   border-[#682d39]  hover:border-[#f93c4f] duration-300 active:border-[#531d1d] active:bg-[#3f1010]";
  interface RoomData {
    id: number;
    title: string;
    memberCount: number;
  }

  const dummyRoomData: RoomData[] = [
    {
      id: 1,
      title: "즐거운 파티게임방",
      memberCount: 3,
    },
    {
      id: 2,
      title: "초보만 Welcome",
      memberCount: 2,
    },
    {
      id: 3,
      title: "실력자만 오세요",
      memberCount: 4,
    },
    {
      id: 4,
      title: "자유롭게 즐겨요~",
      memberCount: 1,
    },
    {
      id: 5,
      title: "친목게임방",
      memberCount: 2,
    },
  ];
  return (
    <div className="w-screen h-screen bg-black select-none">
      <div
        className="w-full max-w-[1440px] mx-auto h-max-[840px] bg-cover bg-center bg-no-repeat relative"
        style={{ backgroundImage: `url(${bgImg})` }}
      >
        <div className="absolute inset-0 bg-gradient-to-r from-black to-[#11223349] opacity-100 " />
        <div className="relative z-10 ">
          {/* Header */}
          <header className="p-4 md:p-6  border-black flex justify-between items-center px-6">
            <img src={logo} alt="로고" className="h-6" />
            <button className="px-2 py-1 rounded-lg duration-300 hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[#F74A5C]">
              <img src={power} alt="" />
            </button>
          </header>

          <div className="md:flex md:h-screen md:max-h-[730px] md:m-12 md:mb-5 mb-5">
            {/* Aside */}
            <aside
              className={` mx-8 w-80 ${blockStyle}  flex flex-col items-center  bg-[#0000008f] hidden md:flex`}
            >
              <div className="w-32 h-32 mt-24 mb-3 flex items-center justify-center border border-[#f74a5c]/60">
                <img src={`${playerIcon}`} alt="" />
              </div>
              <div className="w-32 flex items-center justify-center text-[#fcfafa]">
                player 닉네임
              </div>

              <div className="mt-62">
                <div>내 전적</div>
              </div>

              <div className="mt-auto w-full flex flex-col items-center mb-8">
                <button
                  className={`${blockStyle} ${blockHover} ${blockActive} w-72 h-10    bg-[#281919]`}
                >
                  blank
                </button>
                <button
                  className={`${blockStyle} ${blockHover} ${blockActive} w-72 h-10 mt-4  bg-[#281919]`}
                >
                  내 정보
                </button>
              </div>

              {/* 왼쪽 컨텐츠 영역 */}
            </aside>

            {/* Main Content */}
            <div className="flex flex-col flex-1 gap-4 px-8 ">
              {/* 상단 영역 */}
              <button className="w-32 justify-start px-6 py-2 bg-black border-2 border-[#bf8f5b] text-white rounded hover:bg-[#211b05] hover:border-[#dea569] hover:shadow-[0_0_10px_0] hover:shadow-[#f99f3e] active:border-[#906639] active:bg-black active:shadow-none duration-100 ">
                방 만들기
              </button>

              {/* 중앙 방 목록 영역 무한스크롤로 구현 예정*/}
              <div className={`flex-1 p-7 ${blockStyle} bg-[#0000008f]`}>
                <div className="h-8  grid md:grid-cols-[6rem,1fr,8rem] grid-cols-[4rem,1fr,5rem] px-2">
                  <span>No.</span>
                  <span>Title</span>
                  <span className="text-right">인원수</span>
                </div>
                <ul className="space-y-2.5">
                  {dummyRoomData.map((item) => (
                    <li
                      key={item.id}
                      className={`h-8 bg-[#241818de] grid md:grid-cols-[6rem,1fr,8rem] grid-cols-[4rem,1fr,5rem] items-center px-2 ${blockStyle} ${blockHover} ${blockActive} cursor-pointer`}
                    >
                      <span className="ml-2">{item.id}</span>
                      <span className="truncate">{item.title}</span>
                      <span className="text-right mr-2">
                        {item.memberCount} / 8
                      </span>
                    </li>
                  ))}
                </ul>
              </div>

              {/* 하단 채팅 영역 */}
              <div
                className={`flex flex-col p-4 bg-[#0000008f] h-52   ${blockStyle} `}
              >
                <div className="flex-1 min-h-[8rem] max-h-[12rem] overflow-y-auto">
                  kub938: ㅎㅇ
                </div>
                <input
                  placeholder="채팅 입력"
                  type="text"
                  className={`p-4 h-8  bg-[#241818de]  ${blockStyle} transition-colors duration-50 focus:border-2 focus:border-[#ff6767] focus:outline-none focus:bg-[#1122338e]`}
                />
              </div>
            </div>
          </div>

          {/* Footer */}

          <div className="flex justify-end">
            <button
              className={`${footerButtonStyle}  rounded-tl-sm border-l-2`}
            >
              <img src={bellIcon} alt="icon" className="w-7" />
            </button>
            <button className={`${footerButtonStyle}`}>
              <img src={settingIcon} alt="icon" className="w-7" />
            </button>
            <button className={`${footerButtonStyle}`}>
              <img src={friendsIcon} alt="icon" className="h-7" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default GameRooms;
