import React, { createContext, useEffect, useRef } from "react";
import { io, Socket } from "socket.io-client";
import { userState } from "../store/userState";
import { useRecoilValue } from "recoil";

const SOCKET_URL = "http://localhost:9090";

// 1. Context 생성
export const SocketContext = createContext<Socket | null>(null);

// 2. Provider 컴포넌트
export const SocketProvider = ({ children }: { children: React.ReactNode }) => {
  const userInfo = useRecoilValue(userState);
  const socketRef = useRef<Socket | null>(null);

  console.log(userInfo.token);

  if (!socketRef.current) {
    // JWT 토큰이 있다고 가정 (localStorage, Cookie 등에서 가져오기)

    socketRef.current = io(SOCKET_URL, {
      // auth 옵션으로 토큰을 보냄
      auth: {
        token: userInfo.token,
      },
    });
  }

  useEffect(() => {
    const socket = socketRef.current;
    socket?.on("connect", () => {
      console.log("소켓 연결됨:", socket.id);
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
