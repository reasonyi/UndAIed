// import { useContext } from "react";
// import { SocketContext } from "../components/SocketContext";

// export const useSocket = () => {
//   const socket = useContext(SocketContext);
//   if (!socket) {
//     throw new Error("useSocket must be used within a SocketProvider.");
//   }
//   return socket;
// };



import { useContext, useEffect } from "react";
import { SocketContext } from "../components/SocketContext";

export const useSocket = () => {
  const context = useContext(SocketContext);
  
  if (!context) {
    throw new Error("useSocket must be used within a SocketProvider");
  }

  const { socket, isConnected, connect } = context;

  useEffect(() => {
    if (!isConnected) {
      connect();
    }
  }, [isConnected, connect]);

  // 소켓 연결이 안되었을 때 에러 대신 로딩 상태 반환
  if (!socket) {
    return null; // 또는 loading 상태를 표시하는 다른 값
  }

  return socket;
};