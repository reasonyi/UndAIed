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
      <SocketProvider url="http://localhost:9090">
        <Outlet />
      </SocketProvider>
    </>
  );
}

export default Game;
