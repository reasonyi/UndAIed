import { useParams } from "react-router";
import boardImage from "../../assets/board.jpg";
import { useRecoilState } from "recoil";
import { currentPageState } from "../../store/boardState";
import leftArrow from "../../assets/icon/left.svg";
import rightArrow from "../../assets/icon/right.svg";
function Board() {
  const testData = [
    {
      boardId: 1,
      title: "첫 번째 공지사항입니다.",
      viewCnt: 123,
      content: "첫 번째 공지사항 내용입니다.",
      category: "notice",
      createAt: "2024-01-20T09:00:00",
      updateAt: "2024-01-20T09:00:00",
    },
    {
      boardId: 2,
      title: "버그 리포트: 로그인 오류",
      content: "로그인 시 발생하는 오류에 대한 리포트입니다.",
      category: "bugreport",
      createAt: "2024-01-21T10:30:00",
      viewCnt: 123,
      updateAt: "2024-01-21T10:30:00",
    },
    {
      boardId: 3,
      title: "서비스 업데이트 안내",
      content: "새로운 기능이 추가되었습니다.",
      category: "notice",
      createAt: "2024-01-22T11:15:00",
      viewCnt: 123,
      updateAt: "2024-01-22T11:15:00",
    },
    {
      boardId: 4,
      title: "버그 리포트: 이미지 업로드 문제",
      content: "이미지 업로드 시 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-23T13:45:00",
      viewCnt: 123,
      updateAt: "2024-01-23T13:45:00",
    },
    {
      boardId: 5,
      title: "긴급 공지사항",
      content: "서버 점검 안내",
      category: "notice",
      createAt: "2024-01-24T14:20:00",
      viewCnt: 123,
      updateAt: "2024-01-24T14:20:00",
    },
    {
      boardId: 6,
      title: "버그 리포트: 결제 오류",
      content: "결제 진행 중 발생하는 오류입니다.",
      category: "bugreport",
      createAt: "2024-01-24T15:10:00",
      viewCnt: 123,
      updateAt: "2024-01-24T15:10:00",
    },
    {
      boardId: 7,
      title: "서비스 이용 안내",
      content: "서비스 이용 방법 안내입니다.",
      category: "notice",
      createAt: "2024-01-25T16:30:00",
      viewCnt: 123,
      updateAt: "2024-01-25T16:30:00",
    },
    {
      boardId: 8,
      title: "버그 리포트: 채팅 기능 오류",
      content: "채팅 기능 사용 중 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-26T17:45:00",
      viewCnt: 123,
      updateAt: "2024-01-26T17:45:00",
    },
    {
      boardId: 9,
      title: "2024년 서비스 계획 안내",
      content: "올해의 서비스 계획을 안내드립니다.",
      category: "notice",
      createAt: "2024-01-27T09:20:00",
      viewCnt: 123,
      updateAt: "2024-01-27T09:20:00",
    },
    {
      boardId: 10,
      title: "버그 리포트: 프로필 수정 오류",
      content: "프로필 수정 시 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-28T10:40:00",
      viewCnt: 123,
      updateAt: "2024-01-28T10:40:00",
    },
    {
      boardId: 11,
      title: "신규 이벤트 안내",
      content: "새로운 이벤트를 안내드립니다.",
      category: "notice",
      createAt: "2024-01-29T11:30:00",
      viewCnt: 123,
      updateAt: "2024-01-29T11:30:00",
    },
    {
      boardId: 12,
      title: "버그 리포트: 알림 기능 오류",
      content: "알림 기능 관련 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-30T13:15:00",
      viewCnt: 123,
      updateAt: "2024-01-30T13:15:00",
    },

    {
      boardId: 1,
      title: "첫 번째 공지사항입니다.",
      viewCnt: 123,
      content: "첫 번째 공지사항 내용입니다.",
      category: "notice",
      createAt: "2024-01-20T09:00:00",
      updateAt: "2024-01-20T09:00:00",
    },
    {
      boardId: 2,
      title: "버그 리포트: 로그인 오류",
      content: "로그인 시 발생하는 오류에 대한 리포트입니다.",
      category: "bugreport",
      createAt: "2024-01-21T10:30:00",
      viewCnt: 123,
      updateAt: "2024-01-21T10:30:00",
    },
    {
      boardId: 3,
      title: "서비스 업데이트 안내",
      content: "새로운 기능이 추가되었습니다.",
      category: "notice",
      createAt: "2024-01-22T11:15:00",
      viewCnt: 123,
      updateAt: "2024-01-22T11:15:00",
    },
    {
      boardId: 4,
      title: "버그 리포트: 이미지 업로드 문제",
      content: "이미지 업로드 시 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-23T13:45:00",
      viewCnt: 123,
      updateAt: "2024-01-23T13:45:00",
    },
    {
      boardId: 5,
      title: "긴급 공지사항",
      content: "서버 점검 안내",
      category: "notice",
      createAt: "2024-01-24T14:20:00",
      viewCnt: 123,
      updateAt: "2024-01-24T14:20:00",
    },
    {
      boardId: 6,
      title: "버그 리포트: 결제 오류",
      content: "결제 진행 중 발생하는 오류입니다.",
      category: "bugreport",
      createAt: "2024-01-24T15:10:00",
      viewCnt: 123,
      updateAt: "2024-01-24T15:10:00",
    },
    {
      boardId: 7,
      title: "서비스 이용 안내",
      content: "서비스 이용 방법 안내입니다.",
      category: "notice",
      createAt: "2024-01-25T16:30:00",
      viewCnt: 123,
      updateAt: "2024-01-25T16:30:00",
    },
    {
      boardId: 8,
      title: "버그 리포트: 채팅 기능 오류",
      content: "채팅 기능 사용 중 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-26T17:45:00",
      viewCnt: 123,
      updateAt: "2024-01-26T17:45:00",
    },
    {
      boardId: 9,
      title: "2024년 서비스 계획 안내",
      content: "올해의 서비스 계획을 안내드립니다.",
      category: "notice",
      createAt: "2024-01-27T09:20:00",
      viewCnt: 123,
      updateAt: "2024-01-27T09:20:00",
    },
    {
      boardId: 10,
      title: "버그 리포트: 프로필 수정 오류",
      content: "프로필 수정 시 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-28T10:40:00",
      viewCnt: 123,
      updateAt: "2024-01-28T10:40:00",
    },
    {
      boardId: 11,
      title: "신규 이벤트 안내",
      content: "새로운 이벤트를 안내드립니다.",
      category: "notice",
      createAt: "2024-01-29T11:30:00",
      viewCnt: 123,
      updateAt: "2024-01-29T11:30:00",
    },
    {
      boardId: 12,
      title: "버그 리포트: 알림 기능 오류",
      content: "알림 기능 관련 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-30T13:15:00",
      viewCnt: 123,
      updateAt: "2024-01-30T13:15:00",
    },

    {
      boardId: 1,
      title: "첫 번째 공지사항입니다.",
      viewCnt: 123,
      content: "첫 번째 공지사항 내용입니다.",
      category: "notice",
      createAt: "2024-01-20T09:00:00",
      updateAt: "2024-01-20T09:00:00",
    },
    {
      boardId: 2,
      title: "버그 리포트: 로그인 오류",
      content: "로그인 시 발생하는 오류에 대한 리포트입니다.",
      category: "bugreport",
      createAt: "2024-01-21T10:30:00",
      viewCnt: 123,
      updateAt: "2024-01-21T10:30:00",
    },
    {
      boardId: 3,
      title: "서비스 업데이트 안내",
      content: "새로운 기능이 추가되었습니다.",
      category: "notice",
      createAt: "2024-01-22T11:15:00",
      viewCnt: 123,
      updateAt: "2024-01-22T11:15:00",
    },
    {
      boardId: 4,
      title: "버그 리포트: 이미지 업로드 문제",
      content: "이미지 업로드 시 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-23T13:45:00",
      viewCnt: 123,
      updateAt: "2024-01-23T13:45:00",
    },
    {
      boardId: 5,
      title: "긴급 공지사항",
      content: "서버 점검 안내",
      category: "notice",
      createAt: "2024-01-24T14:20:00",
      viewCnt: 123,
      updateAt: "2024-01-24T14:20:00",
    },
    {
      boardId: 6,
      title: "버그 리포트: 결제 오류",
      content: "결제 진행 중 발생하는 오류입니다.",
      category: "bugreport",
      createAt: "2024-01-24T15:10:00",
      viewCnt: 123,
      updateAt: "2024-01-24T15:10:00",
    },
    {
      boardId: 7,
      title: "서비스 이용 안내",
      content: "서비스 이용 방법 안내입니다.",
      category: "notice",
      createAt: "2024-01-25T16:30:00",
      viewCnt: 123,
      updateAt: "2024-01-25T16:30:00",
    },
    {
      boardId: 8,
      title: "버그 리포트: 채팅 기능 오류",
      content: "채팅 기능 사용 중 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-26T17:45:00",
      viewCnt: 123,
      updateAt: "2024-01-26T17:45:00",
    },
    {
      boardId: 9,
      title: "2024년 서비스 계획 안내",
      content: "올해의 서비스 계획을 안내드립니다.",
      category: "notice",
      createAt: "2024-01-27T09:20:00",
      viewCnt: 123,
      updateAt: "2024-01-27T09:20:00",
    },
    {
      boardId: 10,
      title: "버그 리포트: 프로필 수정 오류",
      content: "프로필 수정 시 발생하는 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-28T10:40:00",
      viewCnt: 123,
      updateAt: "2024-01-28T10:40:00",
    },
    {
      boardId: 11,
      title: "신규 이벤트 안내",
      content: "새로운 이벤트를 안내드립니다.",
      category: "notice",
      createAt: "2024-01-29T11:30:00",
      viewCnt: 123,
      updateAt: "2024-01-29T11:30:00",
    },
    {
      boardId: 12,
      title: "버그 리포트: 알림 기능 오류",
      content: "알림 기능 관련 문제입니다.",
      category: "bugreport",
      createAt: "2024-01-30T13:15:00",
      viewCnt: 123,
      updateAt: "2024-01-30T13:15:00",
    },
  ];

  const { category = "notice" } = useParams();
  const filterData = testData.filter((data) => data.category === category);

  //pagenation 변수들
  const postPerPage = 10; //
  const [currentPage, setCurrentPage] = useRecoilState(currentPageState);
  const totalPages = Math.ceil(filterData.length / postPerPage);
  const currentPosts = filterData.slice(
    (currentPage - 1) * postPerPage,
    currentPage * postPerPage
  );

  const pageGroupSize = 10;
  const currentGroup = Math.ceil(currentPage / pageGroupSize);
  const startPage = (currentGroup - 1) * pageGroupSize + 1;
  const endPage = Math.min(currentGroup * pageGroupSize, totalPages);

  const movePage = (page: number) => {
    setCurrentPage((prev) => (prev = page));
  };

  const movePrevPage = () => {
    setCurrentPage((prev) => Math.max(prev - 1, 1));
    console.log(currentPage);
  };

  const moveNextPage = () => {
    setCurrentPage((prev) => prev + 1);
    console.log(currentPage);
  };

  // const movePrevGroup = () => {
  //   if (startPage > 1) {
  //     movePage(startPage - 1);
  //   }
  // };

  // const moveNextGroup = () => {
  //   if (endPage < totalPages) {
  //     movePage(endPage + 1);
  //   }
  // };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(date);
  };

  return (
    <>
      {/* ----------------------게시판 배너 ------------------------ */}
      <div
        className="border w-full h-[370px] bg-[#c73838] bg-cover bg-no-repeat relative"
        style={{
          backgroundImage: `url(${boardImage})`,
          backgroundSize: "cover",
          backgroundPosition: "50% 60%",
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-r from-black via-transparent to-black">
          <div className="relative ml-[17%] z-10 h-full flex flex-col justify-center items-left text-white">
            <div className="text-5xl">
              {category === "bugreport" ? "버그리포트" : "공지사항"}
            </div>
          </div>
        </div>
      </div>
      {/* ----------------------- 카테고리 네비게이션 --------------------------- */}
      <ul className="w-full max-w-[1260px] mx-auto flex flex-col sm:flex-row mt-10 h-14">
        <li
          className={`${
            category === "notice"
              ? " border border-x-black border-t-black"
              : "text-[#868686]"
          } flex items-center w-full sm:w-1/2`}
        >
          <a className="w-full text-center" href="/board/notice">
            공지사항
          </a>
        </li>
        <li
          className={`${
            category === "bugreport"
              ? "border border-x-black border-t-black"
              : ""
          } flex items-center w-full sm:w-1/2  ${
            category !== "bugreport" ? "text-[#868686]" : ""
          }`}
        >
          <a className="w-full text-center" href="/board/bugreport">
            버그리포트
          </a>
        </li>
      </ul>

      {/* ------------------------- 게시글 리스트 --------------------------------- */}

      <main className="w-full max-w-[1260px] mx-auto">
        <div className="bg-white overflow-x-auto ">
          <table className="w-full min-w-[768px]">
            <thead className="border-t-2 border-t-black bg-[#ededed27]">
              <tr className="border-b">
                <th className="py-4 px-6 text-center w-[10%]">공지</th>
                <th className="py-4 px-6 text-left w-[55%] min-w-[200px]">
                  제목
                </th>
                <th className="py-4 px-6 text-center w-[7%]">조회수</th>
                <th className="py-4 px-6 text-center w-[13%]">게시일</th>
              </tr>
            </thead>
            <tbody>
              {currentPosts.map((post, index) => (
                <tr
                  key={index}
                  className="border-b transition-colors hover:bg-gray-50"
                >
                  <td className="py-4 px-6 sm:px-6 text-center whitespace-nowrap">
                    {(currentPage - 1) * postPerPage + index + 1}
                  </td>
                  <td className="py-4 px-6 sm:px-6 truncate">{post.title}</td>
                  <td className="py-4 sm:px-6 text-center whitespace-nowrap ">
                    {post.viewCnt + " view"}
                  </td>
                  <td className="py-4 px-4 sm:px-6 text-center whitespace-nowrap">
                    {formatDate(post.createAt)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>

      {/*------------------------ 페이징 처리 ---------------------- */}
      <div className="flex justify-center mt-12 mb-44 ">
        <div className="flex gap-2">
          <button
            onClick={movePrevPage}
            className="px-3 py-3 border-[#00000026] border hover:border-black"
            disabled={currentPage === 1}
          >
            <img src={leftArrow} alt="left"></img>
          </button>

          {Array.from({ length: endPage - startPage + 1 }, (_, i) => (
            <button
              key={startPage + i}
              onClick={() => movePage(startPage + i)}
              className={`px-4 py-3  text-black hover:bg-[#323232] hover:text-white ${
                currentPage === startPage + i
                  ? "bg-black text-white pointer-events: none"
                  : ""
              }`}
            >
              {startPage + i}
            </button>
          ))}
          <button
            onClick={moveNextPage}
            className="px-3 py-3 border-[#00000026] border hover:border-black "
            disabled={currentPage === totalPages}
          >
            <img src={rightArrow} alt="left"></img>
          </button>
        </div>
      </div>
    </>
  );
}

export default Board;
