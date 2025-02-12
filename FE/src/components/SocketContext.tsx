// import React, { createContext, useEffect, useRef } from "react";
// import { io, Socket } from "socket.io-client";
// import { userState } from "../store/userState";
// import { useRecoilValue } from "recoil";

// const SOCKET_URL = "https://i12b212.p.ssafy.io";
// // const SOCKET_URL = "ws://localhost:9090";

// // 1. Context 생성
// export const SocketContext = createContext<Socket | null>(null);

// // 2. Provider 컴포넌트
// export const SocketProvider = ({ children }: { children: React.ReactNode }) => {
//   const userInfo = useRecoilValue(userState);
//   const socketRef = useRef<Socket | null>(null);

//   console.log("Current token:", userInfo.token); // 토큰 로깅
//   if (!socketRef.current) {
//     socketRef.current = io(SOCKET_URL, {
//       extraHeaders: {
//         Authorization: `Bearer ${userInfo.token}`,
//       },
//       query: {
//         auth: userInfo.token,
//       },
//       transports: ["websocket", "polling"],
//     });
//   }

//   useEffect(() => {
//     const socket = socketRef.current;

//     socket?.on("connect", () => {
//       console.log("소켓 연결됨:", socket.id);
//     });

//     socket?.on("connect_error", (error) => {
//       console.error("소켓 연결 에러:", error);
//     });

//     socket?.on("error", (error) => {
//       console.error("소켓 에러:", error);
//     });

//     return () => {
//       socket?.disconnect();
//     };
//   }, []);

//   return (
//     <SocketContext.Provider value={socketRef.current}>
//       {children}
//     </SocketContext.Provider>
//   );
// };


// // SocketContext.tsx 수정
// import { createContext, useEffect, useState } from "react";
// import { io, Socket } from "socket.io-client";

// export const SocketContext = createContext<Socket | null>(null);

// export const SocketProvider = ({ children }: { children: React.ReactNode }) => {
//   const [socket, setSocket] = useState<Socket | null>(null);

//   useEffect(() => {
//     // 토큰 가져오기 로직
//     const token = localStorage.getItem("accessToken");

//     const socketInstance = io("https://i12b212.p.ssafy.io", {
//       path: "/socket.io/",
//       transports: ["websocket"],
//       auth: {
//         token: token
//       },
//       reconnection: true,
//       reconnectionAttempts: 5,
//       reconnectionDelay: 1000
//     });

//     socketInstance.on("connect_error", (error) => {
//       console.error("Socket connection error:", error);
//     });

//     setSocket(socketInstance);

//     return () => {
//       socketInstance.close();
//     };
//   }, []);

//   return (
//     <SocketContext.Provider value={socket}>
//       {children}
//     </SocketContext.Provider>
//   );
// };

// 2차 수정
// import { createContext, useEffect, useState, useCallback } from "react";
// import { io, Socket } from "socket.io-client";

// interface SocketContextType {
//   socket: Socket | null;
//   isConnected: boolean;
//   connect: () => void;
//   disconnect: () => void;
// }

// export const SocketContext = createContext<SocketContextType>({
//   socket: null,
//   isConnected: false,
//   connect: () => { },
//   disconnect: () => { }
// });

// export const SocketProvider = ({ children }: { children: React.ReactNode }) => {
//   const [socket, setSocket] = useState<Socket | null>(null);
//   const [isConnected, setIsConnected] = useState(false);

//   const connect = useCallback(() => {
//     const token = localStorage.getItem("accessToken");
//     if (!token) {
//       console.warn("No access token found");
//       return;
//     }

//     const socketInstance = io("https://i12b212.p.ssafy.io", {
//       path: "/socket.io", // 슬래시로 시작하는 경로
//       transports: ["websocket"],
//       auth: { token },
//       reconnection: true,
//       reconnectionAttempts: 5,
//       reconnectionDelay: 1000,
//       timeout: 10000,
//       extraHeaders: {
//         "Upgrade": "websocket",
//         "Connection": "Upgrade"
//       },
//       autoConnect: true,
//       namespace: "/socket.io" // namespace 명시
//     });

//     socketInstance.on("connect", () => {
//       console.log("Socket connected successfully");
//       setIsConnected(true);
//     });

//     socketInstance.on("disconnect", () => {
//       console.log("Socket disconnected");
//       setIsConnected(false);
//     });

//     socketInstance.on("connect_error", (error) => {
//       console.error("Socket connection error:", error);
//       setIsConnected(false);
//     });

//     setSocket(socketInstance);
//   }, []);

//   const disconnect = useCallback(() => {
//     if (socket) {
//       socket.disconnect();
//       setSocket(null);
//       setIsConnected(false);
//     }
//   }, [socket]);

//   useEffect(() => {
//     connect();
//     return () => {
//       disconnect();
//     };
//   }, [connect, disconnect]);

//   return (
//     <SocketContext.Provider value={{ socket, isConnected, connect, disconnect }}>
//       {children}
//     </SocketContext.Provider>
//   );
// };


// 3차 수정
// SocketContext.tsx
// import { createContext, useEffect, useState, useCallback, useRef } from "react";
// import { io, Socket } from "socket.io-client";
// import { useRecoilValue } from "recoil";
// import { userState } from "../store/userState";
// import { IUser } from "../types/User";

// interface SocketContextType {
//   socket: Socket | null;
//   isConnected: boolean;
//   connect: () => void;
//   disconnect: () => void;
// }

// export const SocketContext = createContext<SocketContextType>({
//   socket: null,
//   isConnected: false,
//   connect: () => {},
//   disconnect: () => {}
// });

// export const SocketProvider = ({ children }: { children: React.ReactNode }) => {
//   const [socket, setSocket] = useState<Socket | null>(null);
//   const [isConnected, setIsConnected] = useState(false);
//   const user = useRecoilValue<IUser>(userState);
//   const socketRef = useRef<Socket | null>(null);

//   const connect = useCallback(() => {
//     if (!user.token || !user.isLogin) {
//       console.warn("No token found or user not logged in");
//       return;
//     }

//     if (socketRef.current?.connected) {
//       console.log("Socket already connected");
//       return;
//     }

//     const socketInstance = io("https://i12b212.p.ssafy.io", {
//       path: "/socket.io",
//       transports: ["websocket"],
//       auth: { token: user.token },
//       reconnection: true,
//       reconnectionAttempts: 5,
//       reconnectionDelay: 1000,
//       timeout: 10000,
//       extraHeaders: {
//         "Upgrade": "websocket",
//         "Connection": "Upgrade"
//       },
//       autoConnect: true,
//       namespace: "/socket.io"
//     });

//     socketInstance.on("connect", () => {
//       console.log("Socket connected successfully");
//       setIsConnected(true);
//     });

//     socketInstance.on("disconnect", () => {
//       console.log("Socket disconnected");
//       setIsConnected(false);
//     });

//     socketInstance.on("connect_error", (error) => {
//       console.error("Socket connection error:", error);
//       setIsConnected(false);
//     });

//     socketRef.current = socketInstance;
//     setSocket(socketInstance);
//   }, [user.token, user.isLogin]);

//   const disconnect = useCallback(() => {
//     if (socketRef.current) {
//       socketRef.current.disconnect();
//       socketRef.current = null;
//       setSocket(null);
//       setIsConnected(false);
//     }
//   }, []);

//   useEffect(() => {
//     if (user.isLogin && user.token) {
//       connect();
//     }

//     return () => {
//       disconnect();
//     };
//   }, [user.isLogin, user.token]);

//   return (
//     <SocketContext.Provider value={{ 
//       socket: socketRef.current, 
//       isConnected, 
//       connect, 
//       disconnect 
//     }}>
//       {children}
//     </SocketContext.Provider>
//   );
// };

// // 4차 수정
// import React, { createContext, useEffect, useState, ReactNode } from 'react';
// import { Socket, io } from 'socket.io-client';
// import { useRecoilValue } from 'recoil';
// import { userState } from '../store/userState'; // Recoil state import

// interface SocketContextType {
//   socket: Socket | null;
//   isConnected: boolean;
// }

// export const SocketContext = createContext<SocketContextType | null>(null);

// interface SocketProviderProps {
//   children: ReactNode;
//   url: string;
// }

// export const SocketProvider: React.FC<SocketProviderProps> = ({ children, url }) => {
//   const [socket, setSocket] = useState<Socket | null>(null);
//   const [isConnected, setIsConnected] = useState(false);
//   const userData = useRecoilValue(userState); // userState에서 토큰 가져오기

//   useEffect(() => {
//     // 토큰이 없으면 연결하지 않음
//     if (!userData?.token) return;

//     const newSocket = io(url, {
//       path: '/socket.io',
//       transports: ['websocket'],
//       auth: {
//         token: userData.token  // auth 옵션으로 토큰 전달
//       },
//       reconnection: true,
//       reconnectionAttempts: 5,
//       reconnectionDelay: 1000,
//       timeout: 10000,
//       autoConnect: false
//     });

//     newSocket.on('connect', () => {
//       console.log('Socket connected');
//       setIsConnected(true);
//     });

//     newSocket.on('connect_error', (error) => {
//       console.error('Socket connection error:', error);
//       setIsConnected(false);
//     });

//     newSocket.on('disconnect', () => {
//       console.log('Socket disconnected');
//       setIsConnected(false);
//     });

//     setSocket(newSocket);

//     return () => {
//       newSocket.close();
//     };
//   }, [url, userData?.token]); // 토큰이 변경될 때마다 재연결

//   return (
//     <SocketContext.Provider value={{ socket, isConnected }}>
//       {children}
//     </SocketContext.Provider>
//   );
// };

// 5차 수정
// import React, { createContext, useEffect, useState, ReactNode } from 'react';
// import { Socket, io } from 'socket.io-client';
// import { useRecoilValue } from 'recoil';
// import { userState } from '../store/userState';

// interface SocketContextType {
//   socket: Socket | null;
//   isConnected: boolean;
//   connect: () => void;  // connect 함수 추가
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

//   // 소켓 연결 함수
//   const connect = () => {
//     if (!userData?.token || socket) {
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


//     const newSocket = io(url, {
//       path: '/socket.io',
//       nsp: '/socket.io',  // OK
//       transports: ['websocket'],
//       extraHeaders: {
//         Authorization: `Bearer ${userData.token}`
//       },
//       reconnection: true,
//       reconnectionAttempts: 5,
//       reconnectionDelay: 1000,
//       timeout: 10000,
//       autoConnect: false
//     });

//     newSocket.on('connect', () => {
//       console.log('Socket connected');
//       setIsConnected(true);
//     });

//     newSocket.on('connect_error', (error) => {
//       console.error('Socket connection error:', error);
//       setIsConnected(false);
//     });

//     newSocket.on('disconnect', () => {
//       console.log('Socket disconnected');
//       setIsConnected(false);
//     });

//     newSocket.connect();
//     setSocket(newSocket);
//   };

//   // cleanup on unmount
//   useEffect(() => {
//     return () => {
//       if (socket) {
//         socket.close();
//       }
//     };
//   }, [socket]);

//   return (
//     <SocketContext.Provider value={{ socket, isConnected, connect }}>
//       {children}
//     </SocketContext.Provider>
//   );
// };

//6차 수정
import React, { createContext, useEffect, useState, ReactNode } from 'react';
import { Socket, io } from 'socket.io-client';
import { useRecoilValue } from 'recoil';
import { userState } from '../store/userState';

interface SocketContextType {
  socket: Socket | null;
  isConnected: boolean;
  connect: () => void;
}

export const SocketContext = createContext<SocketContextType | null>(null);

interface SocketProviderProps {
  children: ReactNode;
  url: string;
}

export const SocketProvider: React.FC<SocketProviderProps> = ({ children, url }) => {
  const [socket, setSocket] = useState<Socket | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const userData = useRecoilValue(userState);

  const connect = () => {
    if (!userData?.token) {
      console.log('No token available, skipping socket connection');
      return;
    }

    if (socket?.connected) {
      console.log('Socket already connected');
      return;
    }

    // 기존 소켓이 있다면 정리
    if (socket) {
      socket.close();
      setSocket(null);
    }

    try {
      const newSocket = io(`${url}/socket.io`, {  // namespace를 URL에 포함
        path: '/socket.io',
        transports: ['websocket'],
        // query: {  // extraHeaders 대신 query 사용
        //   auth: `Bearer ${userData.token}`
        // },
        // auth: { token: userData.token },
        extraHeaders: {
          Authorization: `Bearer ${userData.token}`,
        },
        reconnection: true,
        reconnectionAttempts: 5,
        reconnectionDelay: 1000,
        timeout: 10000,
        autoConnect: false
      });

      newSocket.on('connect', () => {
        console.log('Socket connected successfully');
        setIsConnected(true);
      });

      newSocket.on('connect_error', (error) => {
        console.error('Socket connection error:', error.message);
        setIsConnected(false);
      });

      newSocket.on('disconnect', (reason) => {
        console.log('Socket disconnected. Reason:', reason);
        setIsConnected(false);
      });

      newSocket.on('error', (error) => {
        console.error('Socket error:', error);
      });

      newSocket.connect();
      setSocket(newSocket);

    } catch (error) {
      console.error('Error creating socket connection:', error);
      setIsConnected(false);
    }
  };

  // cleanup on unmount
  useEffect(() => {
    return () => {
      if (socket) {
        socket.close();
        setSocket(null);
        setIsConnected(false);
      }
    };
  }, [socket]);

  return (
    <SocketContext.Provider value={{ socket, isConnected, connect }}>
      {children}
    </SocketContext.Provider>
  );
};
