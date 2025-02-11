import React, { createContext, useEffect, useRef } from "react";
import { io, Socket } from "socket.io-client";
import { userState } from "../store/userState";
import { useRecoilValue } from "recoil";

const SOCKET_URL = "https://i12b212.p.ssafy.io";
// const SOCKET_URL = "ws://localhost:9090";

// 1. Context 생성
export const SocketContext = createContext<Socket | null>(null);

// 2. Provider 컴포넌트
export const SocketProvider = ({ children }: { children: React.ReactNode }) => {
  const userInfo = useRecoilValue(userState);
  const socketRef = useRef<Socket | null>(null);

  console.log("Current token:", userInfo.token); // 토큰 로깅
  if (!socketRef.current) {
    socketRef.current = io(SOCKET_URL, {
      extraHeaders: {
        Authorization: `Bearer ${userInfo.token}`,
      },
      query: {
        auth: userInfo.token,
      },
      transports: ["websocket", "polling"],
    });
  }

  useEffect(() => {
    const socket = socketRef.current;

    socket?.on("connect", () => {
      console.log("소켓 연결됨:", socket.id);
    });

    socket?.on("connect_error", (error) => {
      console.error("소켓 연결 에러:", error);
    });

    socket?.on("error", (error) => {
      console.error("소켓 에러:", error);
    });

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