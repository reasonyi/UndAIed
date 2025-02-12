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
      <SocketProvider url="https://i12b212.p.ssafy.io">
        <Outlet />
      </SocketProvider>
    </>
  );
}

export default Game;
