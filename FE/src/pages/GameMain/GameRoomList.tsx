import { atom, useRecoilState } from "recoil";
import { useEffect, useRef, useState } from "react";
import { GameRoom } from "../../types/gameRoomInfo";
import { gameMainState } from "../../store/gameMainState";

import GameRoomCard from "./GameRoomCard";

// 더미데이터 생성 함수
const generateDummyRooms = (page: number): GameRoom[] => {
  const startId = (page - 1) * 10 + 1;
  return Array.from({ length: 10 }, (_, index) => ({
    id: String(startId + index),
    title: `게임방 ${startId + index}`,
    players: Math.floor(Math.random() * 4) + 1,
    maxPlayers: 8,
    status: "waiting",
    createdAt: new Date(Date.now() - index * 1000 * 60 * 1),
  }));
};

// Recoil atom 정의

export const useGameRooms = () => {
  const [rooms, setRooms] = useRecoilState(gameMainState);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  // 더미데이터에서는 3페이지(30개)만 보여주도록 설정
  const TOTAL_PAGES = 3;

  const fetchMoreRooms = async () => {
    if (loading || !hasMore) return;

    setLoading(true);
    try {
      // 실제 API 호출을 시뮬레이션하기 위한 딜레이
      await new Promise((resolve) => setTimeout(resolve, 1000));

      if (page <= TOTAL_PAGES) {
        const newRooms = generateDummyRooms(page);
        setRooms((prev) => [...prev, ...newRooms]);
        setPage((prev) => prev + 1);
      }

      if (page >= TOTAL_PAGES) {
        setHasMore(false);
      }
    } catch (error) {
      console.error("방 목록을 불러오는데 실패했습니다:", error);
    } finally {
      setLoading(false);
    }
  };

  return {
    rooms,
    loading,
    hasMore,
    fetchMoreRooms,
  };
};

// GameRooms 컴포넌트 수정
function GameMain() {
  const { rooms, loading, hasMore, fetchMoreRooms } = useGameRooms();
  const observerTarget = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore) {
          fetchMoreRooms();
        }
      },
      { threshold: 0.7 }
    );

    if (observerTarget.current) {
      observer.observe(observerTarget.current);
    }

    return () => observer.disconnect();
  }, [hasMore, fetchMoreRooms]);

  return (
    <>
      <style>
        {`
          .custom-scrollbar::-webkit-scrollbar {
            width: 6px;
          }

          .custom-scrollbar::-webkit-scrollbar-track {
            background: rgba(0, 0, 0, 0.2);
            border-radius: 3px;
          }

          .custom-scrollbar::-webkit-scrollbar-thumb {
            background: #f74a5c;
            border-radius: 3px;
          }

          .custom-scrollbar::-webkit-scrollbar-thumb:hover {
            background: #ff6b7d;
          }
        `}
      </style>
      <div className="flex-1 flex flex-col h-96 p-7 bg-[#0000008f] rounded-[5px] border border-[#f74a5c]/60">
        {/* 헤더 영역 */}
        <div className="h-8 grid md:grid-cols-[6rem,1fr,8rem] grid-cols-[4rem,1fr,5rem] px-2 text-white mb-4">
          <span>No.</span>
          <span>Title</span>
          <span className="text-right">인원수</span>
        </div>

        {/* 스크롤 가능한 컨텐츠 영역 */}
        <div className="h-80 overflow-hidden">
          <ul className="custom-scrollbar h-full overflow-y-auto space-y-2.5 pr-2">
            {rooms.map((room) => (
              <GameRoomCard key={room.id} room={room} />
            ))}
            <div
              ref={observerTarget}
              className="h-10 flex items-center justify-center"
            >
              {loading && <p className="text-white">로딩중...</p>}
              {!hasMore && <p className="text-white">더 이상 방이 없습니다.</p>}
            </div>
          </ul>
        </div>
      </div>
    </>
  );
}

export default GameMain;
