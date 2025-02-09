import { Link } from "react-router-dom";
import playerIcon from "../../../assets/player-icon/player-icon-1.svg";
import DonutChart from "../components/DonutChart";
import axios from "axios";
import { useQuery } from "@tanstack/react-query";

function GameSidebar() {
  const blockStyle =
    "bg-[#5349507a] border border-[#f74a5c]/60 backdrop-blur-[12.20px] text-[#fffbfb]  rounded-[5px]  transition-all duration-200 ";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";

  function useUserProfile() {
    return useQuery({
      queryKey: ["userProfile"],
      queryFn: async () => {
        const response = await axios.get("api/v1/user/profile");
        console.log("유저 데이터 수신 완료", response);
        return response.data;
      },
    });
  }

  const { data, isLoading, error } = useUserProfile();

  if (isLoading) {
    return <div>로딩중 입니다.</div>;
  }
  if (error) {
    console.log("유저 프로필 호출 실패", error);
    return <div>유저 프로필 호출 실패</div>;
  }
  return (
    <aside
      className={` mx-8 w-80 ${blockStyle}  flex-col items-center  bg-[#00000041] hidden md:flex`}
    >
      <div className="flex align-middle">
        <div className="w-32 h-32 mt-8 mb-3 flex items-center justify-center border border-[#f74a5c]/60">
          <img src={`${playerIcon}`} alt="" />
        </div>
        <div className="w-32 mt-5 flex flex-col gap-2 items-center justify-center text-[#fcfafa]">
          <div>{data.nickname} 임시 닉네임</div>
          <div>총 판수 : {data.totalWin + data.totalLose} </div>
        </div>
      </div>

      <div className="mt-62 flex flex-col items-center">
        <div className="mt-10 mb-7 text-2xl">
          <DonutChart />
        </div>
        <div className="mb-7">
          승률 | {data.totalWin} 승 | {data.totalLose} 패 |{" "}
          {" " +
            Math.round(
              (data.totalWin / (data.totalLose + data.totalWin)) * 10000
            ) /
              100}
          %
        </div>
      </div>

      <div className="mt-auto w-full flex flex-col items-center mb-8">
        <Link to="/gamemypage">
          <button
            className={`${blockStyle} ${blockHover} ${blockActive} w-72 h-10 mt-4  bg-[#281919]`}
          >
            내 정보
          </button>
        </Link>
      </div>
    </aside>
  );
}

export default GameSidebar;
