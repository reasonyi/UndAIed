import { Outlet } from "react-router";
import { SocketProvider } from "../components/SocketContext";

function Game() {
  return (
    <>
      <SocketProvider>
        <Outlet />
      </SocketProvider>
    </>
  );
}

export default Game;
