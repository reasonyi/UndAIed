import { useContext } from "react";
import { SocketContext } from "../components/SocketContext";

export const useSocket = () => {
  const socket = useContext(SocketContext);
  if (!socket) {
    throw new Error("useSocket must be used within a SocketProvider.");
  }
  return socket;
};


// 1차 수정
// import { useContext, useEffect } from "react";
// import { SocketContext } from "../components/SocketContext";

// export const useSocket = () => {
//   const context = useContext(SocketContext);
  
//   if (!context) {
//     throw new Error("useSocket must be used within a SocketProvider");
//   }

//   const { socket, isConnected, connect } = context;

//   useEffect(() => {
//     if (!isConnected) {
//       connect();
//     }
//   }, [isConnected, connect]);

//   // 소켓 연결이 안되었을 때 에러 대신 로딩 상태 반환
//   if (!socket) {
//     return null; // 또는 loading 상태를 표시하는 다른 값
//   }

//   return socket;
// };

// // 2차수정
// import { useContext, useEffect, useState } from "react";
// import { SocketContext } from "../components/SocketContext";

// export const useSocket = () => {
//   const context = useContext(SocketContext);
//   const [isLoading, setIsLoading] = useState(true);
  
//   if (!context) {
//     throw new Error("useSocket must be used within a SocketProvider");
//   }

//   const { socket, connect } = context;

//   useEffect(() => {
//     if (!socket) {
//       try {
//         connect();
//       } catch (error) {
//         console.error("Socket connection failed:", error);
//       }
//     } else {
//       setIsLoading(false);
//     }
//   }, [socket, connect]);

//   if (isLoading) {
//     return null;
//   }

//   return socket;
// };

// 3차 수정
// import { useContext, useEffect, useState } from "react";
// import { SocketContext } from "../components/SocketContext";
// import { io } from "socket.io-client";

// export const useSocket = () => {
//   const context = useContext(SocketContext);
//   const [isLoading, setIsLoading] = useState(true);
  
//   if (!context) {
//     throw new Error("useSocket must be used within a SocketProvider");
//   }

//   const { socket, setSocket } = context;

//   useEffect(() => {
//     if (!socket) {
//       try {
//         // 토큰 가져오기 (localStorage나 다른 상태 관리에서)
//         const token = sessionStorage.getItem('userPersist');
        
//         const newSocket = io('wss://i12b212.p.ssafy.io', {
//           path: '/socket.io',
//           transports: ['websocket'],
//           extraHeaders: {
//             Authorization: `Bearer ${token}`
//           }
//         });

//         newSocket.on('connect', () => {
//           console.log('Socket connected successfully');
//           setIsLoading(false);
//         });

//         newSocket.on('connect_error', (error) => {
//           console.error('Socket connection error:', error);
//           setIsLoading(false);
//         });

//         setSocket(newSocket);
//       } catch (error) {
//         console.error("Socket connection failed:", error);
//         setIsLoading(false);
//       }
//     } else {
//       setIsLoading(false);
//     }

//     return () => {
//       if (socket) {
//         socket.disconnect();
//       }
//     };
//   }, [socket, setSocket]);

//   if (isLoading) {
//     return null;
//   }

//   return socket;
// };

// 4차수정
// import { useContext, useEffect, useState } from "react";
// import { SocketContext } from "../components/SocketContext";
// import { io } from "socket.io-client";

// export const useSocket = () => {
//   const context = useContext(SocketContext);
//   const [isLoading, setIsLoading] = useState(true);
  
//   if (!context) {
//     throw new Error("useSocket must be used within a SocketProvider");
//   }

//   const { socket, isConnected, connect } = context;

//   useEffect(() => {
//     if (!socket && !isConnected) {
//       try {
//         const userDataString = localStorage.getItem('userPersist');
//         if (!userDataString) {
//           // throw new Error('No authentication token found');
//           setIsLoading(false); // 에러를 던지지 않고 로딩만 완료
//           return;
//         }
//         // console.log(userDataString)
//         const userData = JSON.parse(userDataString);
//         // console.log(userData)
//         const token = userData?.userState?.token;

//         if (!token) {
//           // throw new Error('Invalid token format');
//           setIsLoading(false);
//           return;
//         }

//         console.log('Attempting socket connection with token');
//         connect();

//       } catch (error) {
//         console.error("Socket connection failed:", error);
//         setIsLoading(false);
//       }
//     } else if (isConnected) {
//       setIsLoading(false);
//     }
//   }, [socket, isConnected, connect]);

//   // 소켓 연결 상태 모니터링
//   useEffect(() => {
//     if (socket) {
//       socket.on('disconnect', () => {
//         console.log('Socket disconnected');
//         setIsLoading(true);
//       });

//       socket.on('connect', () => {
//         console.log('Socket connected');
//         setIsLoading(false);
//       });

//       socket.on('connect_error', (error) => {
//         console.error('Socket connection error:', error);
//         setIsLoading(false);
//       });

//       return () => {
//         socket.off('disconnect');
//         socket.off('connect');
//         socket.off('connect_error');
//       };
//     }
//   }, [socket]);

//   if (isLoading) {
//     return null;
//   }

//   return socket;
// };