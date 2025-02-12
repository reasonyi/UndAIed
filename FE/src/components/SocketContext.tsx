import React, { createContext, useEffect, useRef } from "react";
import { io, Socket } from "socket.io-client";
import { userState } from "../store/userState";
import { useRecoilValue } from "recoil";

// const SOCKET_URL = "https://i12b212.p.ssafy.io";
const SOCKET_URL = "http://localhost:9090";

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



// import React, { createContext, useEffect, useState, ReactNode } from 'react';
// import { Socket, io } from 'socket.io-client';
// import { useRecoilValue } from 'recoil';
// import { userState } from '../store/userState';

// interface SocketContextType {
//   socket: Socket | null;
//   isConnected: boolean;
//   connect: () => void;
// }

// export const SocketContext = createContext<SocketContextType | null>(null);

// interface SocketProviderProps {
//   children: ReactNode;
//   url: string;
// }

// export const SocketProvider: React.FC<SocketProviderProps> = ({ children, url }) => {
//   const [socket, setSocket] = useState<Socket | null>(null);
//   const [isConnected, setIsConnected] = useState(false);
//   const userData = useRecoilValue(userState);

//   const connect = () => {
//     if (!userData?.token) {
//       console.log('No token available, skipping socket connection');
//       return;
//     }

//     if (socket?.connected) {
//       console.log('Socket already connected');
//       return;
//     }

//     // 기존 소켓이 있다면 정리
//     if (socket) {
//       socket.close();
//       setSocket(null);
//     }

//     try {
//       const newSocket = io(`${url}/socket.io`, {  // namespace를 URL에 포함
//         transports: ['websocket'],
//         // query: {  // extraHeaders 대신 query 사용
//         //   auth: `Bearer ${userData.token}`
//         // },
//         // auth: { token: userData.token },
//         extraHeaders: {
//           Authorization: `Bearer ${userData.token}`,
//         },
//         reconnection: true,
//         reconnectionAttempts: 5,
//         reconnectionDelay: 1000,
//         timeout: 10000,
//         autoConnect: false
//       });

//       newSocket.on('connect', () => {
//         console.log('Socket connected successfully');
//         setIsConnected(true);
//       });

//       newSocket.on('connect_error', (error) => {
//         console.error('Socket connection error:', error.message);
//         setIsConnected(false);
//       });

//       newSocket.on('disconnect', (reason) => {
//         console.log('Socket disconnected. Reason:', reason);
//         setIsConnected(false);
//       });

//       newSocket.on('error', (error) => {
//         console.error('Socket error:', error);
//       });

//       newSocket.connect();
//       setSocket(newSocket);

//     } catch (error) {
//       console.error('Error creating socket connection:', error);
//       setIsConnected(false);
//     }
//   };

//   // cleanup on unmount
//   useEffect(() => {
//     return () => {
//       if (socket) {
//         socket.close();
//         setSocket(null);
//         setIsConnected(false);
//       }
//     };
//   }, [socket]);

//   return (
//     <SocketContext.Provider value={{ socket, isConnected, connect }}>
//       {children}
//     </SocketContext.Provider>
//   );
// };
