import { useContext } from "react";
import { SocketContext } from "../components/SocketContext";

export const useSocket = () => {
  const socket = useContext(SocketContext);
  if (!socket) {
    throw new Error("useSocket must be used within a SocketProvider.");
  }
  return socket;
};
