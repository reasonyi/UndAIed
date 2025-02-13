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

interface GameRoomsResponse {
  success: boolean;
  errorMessage: string;
  data: {
    rooms: GameRoom[];
    totalPage: number;
  };
}

interface GameRoomUpdateResponse {
  type: "create" | "delete" | "update";
  data: {
    roomId: number;
    roomTitle: string;
    isPrivate: boolean;
    currentPlayerNum: number;
    playing: boolean;
  };
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

  // const getCurrentPageData = useCallback(() => {
  //   const startIndex = (gameRoomList.page - 1) * 10;
  //   const endIndex = startIndex + 10;
  //   return rooms.slice(startIndex, endIndex);
  // }, [rooms, gameRoomList.page]);

  const fetchMoreRooms = useCallback(async () => {
    if (gameRoomList.loading || !gameRoomList.hasMore || !socket) return;

    setGameRoomList((prev) => ({ ...prev, loading: true }));
    try {
      socket.emit(
        "lobby:room:list",
        { page: gameRoomList.page },
        (response: GameRoomsResponse) => {
          console.log("fetchMoreRooms (무한 스크롤 전용)", response);
        }
      );
    } catch (error) {
      console.error("방 목록을 불러오는데 실패했습니다:", error);
      setGameRoomList((prev) => ({ ...prev, loading: false }));
    }
  }, [socket, gameRoomList.page, gameRoomList.loading, gameRoomList.hasMore]);

  // 소켓 이벤트 리스너를 위한 useEffect
  useEffect(() => {
    if (!socket) return;

    const handleRoomList = (response: GameRoomsResponse) => {
      console.log(
        "들어오자마자 실행하는 핸들 룸 리스트 ",
        rooms,
        response.data.rooms,
        gameRoomList
      );
      setRooms(response.data.rooms);
      setGameRoomList((prev) => ({
        ...prev,
        totalPages: response.data.totalPage,
        page: prev.page + 1,
        hasMore: prev.page < response.data.totalPage,
        loading: false,
      }));
    };

    // 방 업데이트 이벤트 처리
    const handleRoomEvent = (response: GameRoomUpdateResponse) => {
      console.log("업데이트 이벤트 핸들러 수신", response);
      switch (response.type) {
        case "create":
          console.log("create 들어왔습니다.");
          handleRoomCreated(response.data);
          break;
        case "delete":
          console.log("delete 들어왔습니다.");
          handleRoomDeleted(response.data.roomId);
          break;
        case "update":
          console.log("update 들어왔습니다.");
          handleRoomUpdated(response.data);
          break;
      }
    };

    // 방 생성 이벤트 함수
    const handleRoomCreated = (newRoom: GameRoom) => {
      if (!newRoom.isPrivate) {
        console.log(newRoom.isPrivate);
        setRooms((prev: GameRoom[]) => [newRoom, ...prev]);
      }
    };
    // 방 업데이트 이벤트 함수
    const handleRoomUpdated = (updatedRoom: GameRoom) => {
      console.log("updatedRoom 출력", updatedRoom);
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

    socket.emit("lobby:room:list", handleRoomList);
    // 이벤트 리스너 등록
    socket.on("lobby:room:list", handleRoomList);
    socket.on("lobby:room:update", handleRoomEvent);

    // 클린업 함수
    return () => {
      socket.off("lobby:room:list", handleRoomList);
      socket.off("lobby:room:update", handleRoomEvent);
    };
  }, [socket]);

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
