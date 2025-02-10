import { useSocket } from "./useSocket";
import { useRecoilState } from "recoil";
import { useEffect, useState, useCallback } from "react";
import { gameMainState } from "../store/gameMainState";
import { GameRoom } from "../types/gameRoomInfo";

export const useGameRooms = () => {
  const socket = useSocket();
  const [rooms, setRooms] = useRecoilState(gameMainState);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [totalPages, setTotalPages] = useState(3);

  // 웹소켓으로 방 목록 요청
  const fetchMoreRooms = useCallback(async () => {
    if (loading || !hasMore) return;

    setLoading(true);
    try {
      // 웹소켓으로 특정 페이지의 방 목록 요청
      socket.emit("lobby:room:list", { page });
    } catch (error) {
      console.error("방 목록을 불러오는데 실패했습니다:", error);
    }
  }, [socket, page, loading, hasMore]);

  // 웹소켓 이벤트 리스너 설정
  useEffect(() => {
    // 방 목록 응답 처리
    const handleRoomList = (response: {
      rooms: GameRoom[];
      totalPage: number;
    }) => {
      setRooms((prev: GameRoom[]) => [...prev, ...response.rooms]);
      setTotalPages(response.totalPage);
      setPage((prev) => prev + 1);

      if (page >= response.totalPage) {
        setHasMore(false);
      }
      setLoading(false);
    };

    // 방 생성 이벤트 처리
    const handleRoomCreated = (newRoom: GameRoom) => {
      setRooms((prev: GameRoom[]) => [newRoom, ...prev]);
    };

    // 방 업데이트 이벤트 처리 (인원 변경, 게임 상태 변경 등)
    const handleRoomUpdated = (updatedRoom: GameRoom) => {
      setRooms((prev: GameRoom[]) =>
        prev.map((room) =>
          room.roomId === updatedRoom.roomId ? updatedRoom : room
        )
      );
    };

    // 방 삭제 이벤트 처리
    const handleRoomDeleted = (roomId: number) => {
      setRooms((prev: GameRoom[]) =>
        prev.filter((room) => room.roomId !== roomId)
      );
    };

    // 이벤트 리스너 등록
    socket.on("lobby:room:list", handleRoomList);
    socket.on("lobby:room:create", handleRoomCreated);
    socket.on("lobby:room:update", handleRoomUpdated);
    socket.on("lobby:room:delete", handleRoomDeleted);

    // 컴포넌트 언마운트 시 이벤트 리스너 제거
    return () => {
      socket.off("lobby:room:list", handleRoomList);
      socket.off("lobby:room:create", handleRoomCreated);
      socket.off("lobby:room:update", handleRoomUpdated);
      socket.off("lobby:room:delete", handleRoomDeleted);
    };
  }, [socket, page, setRooms]);

  // 초기 데이터 로드
  useEffect(() => {
    fetchMoreRooms();
  }, []);

  return {
    rooms,
    loading,
    hasMore,
    fetchMoreRooms,
  };
};
