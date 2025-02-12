import { useSocket } from "./useSocket";
import { useRecoilState } from "recoil";
import { useEffect, useState, useCallback } from "react";
import { gameMainState } from "../store/gameMainState";
import { GameRoom } from "../types/gameRoomInfo";

interface GameRoomState {
  page: number;
  loading: boolean;
  hasMore: boolean;
  totalPages: number;
}

export const useGameRooms = () => {
  const socket = useSocket();
  const [rooms, setRooms] = useRecoilState(gameMainState);
  const [gameRoomList, setGameRoomList] = useState<GameRoomState>({
    page: 1,
    loading: false,
    hasMore: true,
    totalPages: 3,
  });

  const getCurrentPageData = useCallback(() => {
    const startIndex = (gameRoomList.page - 1) * 10;
    const endIndex = startIndex + 10;
    return rooms.slice(startIndex, endIndex);
  }, [rooms, gameRoomList.page]);

  const fetchMoreRooms = useCallback(async () => {
    if (gameRoomList.loading || !gameRoomList.hasMore || !socket) return;

    setGameRoomList((prev) => ({ ...prev, loading: true }));
    try {
      socket.emit("lobby:room:list", { page: gameRoomList.page });
      console.log("방 목록 요청 보냄");
    } catch (error) {
      console.error("방 목록을 불러오는데 실패했습니다:", error);
      setGameRoomList((prev) => ({ ...prev, loading: false }));
    }
  }, [socket, gameRoomList.page, gameRoomList.loading, gameRoomList.hasMore]);

  // 소켓 이벤트 리스너를 위한 useEffect
  useEffect(() => {
    if (!socket) return;

    const handleRoomList = (response: {
      rooms: GameRoom[];
      totalPage: number;
    }) => {
      console.log(
        "들어오자마자 실행하는 핸들 룸 리스트 ",
        rooms,
        response.rooms,
        gameRoomList
      );
      setRooms(response.rooms);
      setGameRoomList((prev) => ({
        ...prev,
        totalPages: response.totalPage,
        page: prev.page + 1,
        hasMore: prev.page < response.totalPage,
        loading: false,
      }));
    };

    // 방 생성 이벤트 처리
    const handleRoomCreated = (newRoom: GameRoom) => {
      if (!newRoom.isPrivate) {
        console.log(newRoom.isPrivate);
        setRooms((prev: GameRoom[]) => [newRoom, ...prev]);
      }
    };

    // 방 업데이트 이벤트 처리
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

    // 클린업 함수
    return () => {
      socket.off("lobby:room:list", handleRoomList);
      socket.off("lobby:room:create", handleRoomCreated);
      socket.off("lobby:room:update", handleRoomUpdated);
      socket.off("lobby:room:delete", handleRoomDeleted);
    };
  }, [socket]); // socket만 의존성으로 가짐

  const loadNextPage = useCallback(() => {
    if (!gameRoomList.loading && gameRoomList.hasMore && socket) {
      setGameRoomList((prev) => ({
        ...prev,
        page: prev.page + 1,
      }));
      fetchMoreRooms();
    }
  }, [gameRoomList.loading, gameRoomList.hasMore, fetchMoreRooms, socket]);

  return {
    rooms,
    loading: gameRoomList.loading,
    hasMore: gameRoomList.hasMore,
    fetchMoreRooms: loadNextPage,
  };
};
