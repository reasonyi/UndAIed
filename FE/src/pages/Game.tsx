import { Outlet } from "react-router";
import { SocketProvider } from "../components/SocketContext";

// function Game() {
//   return (
//     <>
//         <Outlet />
//     </>
//   );
// }

// export default Game;
function Game() {
  return (
    <>
      <SocketProvider url={import.meta.env.VITE_SOCKET_URL}>
        <Outlet />
      </SocketProvider>
    </>
  );
}

export default Game;
