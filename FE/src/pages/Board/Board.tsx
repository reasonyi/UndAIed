import { useParams } from "react-router";
import { useRecoilState } from "recoil";
import { currentPageState } from "../../store/board/boardState";

import Banner from "./components/Banner";
import CategoryNav from "./components/CategoryNav";
import PostList from "./components/PostList";
import Pagination from "./components/Pagination";

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

  const pageGroupSize = 5;
  const currentGroup = Math.ceil(currentPage / pageGroupSize);
  const startPage = (currentGroup - 1) * pageGroupSize + 1;
  const endPage = Math.min(currentGroup * pageGroupSize, totalPages);

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat("ko-KR", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    }).format(date);
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

  return (
    <>
      <Banner category={category} />
      <CategoryNav category={category} />
      <PostList
        currentPosts={currentPosts}
        currentPage={currentPage}
        postPerPage={postPerPage}
        formatDate={formatDate}
      />
      <Pagination
        currentPage={currentPage}
        endPage={endPage}
        startPage={startPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </>
  );
}

export default Board;
