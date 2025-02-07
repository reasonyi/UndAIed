import { createBrowserRouter } from "react-router-dom";
import App from "../App";
import NotFound from "../pages/NotFound";
import Home from "../pages/Home/Home";
import Policy from "../pages/Policy";
import GameRoom from "../pages/GameRoom/GameRoom";
import GamePlay from "../pages/GamePlay/GamePlay";
import GameMain from "../pages/GameMain/GameMain";
import BoardDetails from "../pages/BoardDetails";
import Board from "../pages/Board/Board";
import User from "../pages/User";
import Log from "../pages/Log";
import BoardWrite from "../pages/BoardWrite";
import Friends from "../pages/Friends/Friends";
import GameMyPage from "../pages/GameMyPage/GameMyPage";
import Game from "../pages/Game";

const router = createBrowserRouter([
  {
    // 모든 라우터들의 컨테이너 개념. home router도 이 하위에 작성한다다
    path: "/",
    element: <App />,
    children: [
      {
        path: "",
        element: <Home />,
      },
      {
        path: "/policy",
        element: <Policy />,
      },
      {
        path: "/gamemain",
        element: <GameMain />,
      },
      {
        path: "/gameplay/:number",
        element: <GamePlay />,
      },
      {
        path: "/gameroom/:number",
        element: <GameRoom />,
      },
      {
        path: "/board/:category/:number",
        element: <BoardDetails />,
      },
      {
        path: "/board/:category",
        element: <Board />,
      },
      {
        path: "/board",
        element: <Board />,
      },
      {
        path: "/write",
        element: <BoardWrite />,
      },
      {
        path: "/user/:userId",
        element: <User />,
      },
      {
        path: "/log/:gameId",
        element: <Log />,
      },
      {
        path: "/friends",
        element: <Friends />,
      },
      {
        path: "/gamemypage",
        element: <GameMyPage />,
      },
      {
        path: "/Game",
        element: <Game />,
      },
    ],
    errorElement: <NotFound />,
  },
]);

export default router;
