import React, { createContext, useEffect, useRef } from "react";
import { io, Socket } from "socket.io-client";

const SOCKET_URL = "http://localhost:3000";

// 1. Context 생성
export const SocketContext = createContext<Socket | null>(null);

// 2. Provider 컴포넌트
export const SocketProvider = ({ children }: { children: React.ReactNode }) => {
  const socketRef = useRef<Socket | null>(null);

  if (!socketRef.current) {
    socketRef.current = io(SOCKET_URL, {
      // 필요한 옵션들
      transports: ["websocket"],
    });
  }

  useEffect(() => {
    // 컴포넌트가 마운트될 때 소켓 연결
    const socket = socketRef.current;

    socket?.on("connect", () => {
      console.log("소켓 연결됨:", socket.id);
    });

    // 언마운트 시 소켓 연결 해제(필요하다면)
    return () => {
      socket?.disconnect();
    };
  }, []);

  return (
    <SocketContext.Provider value={socketRef.current}>
      {children}
    </SocketContext.Provider>
  );
};
