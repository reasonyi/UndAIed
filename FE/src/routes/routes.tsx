import { createBrowserRouter } from "react-router-dom";
import App from "../App";
import NotFound from "../pages/NotFound";
import Home from "../pages/Home";
import Policy from "../pages/Policy";
import GameRoom from "../pages/GameRoom/GameRoom";
import GamePlay from "../pages/GamePlay/GamePlay";
import GameMain from "../pages/GameMain/GameMain";
import Board from "../pages/Board/Board";
import User from "../pages/User";
import Log from "../pages/Log";
import BoardWrite from "../pages/BoardWrite";
<<<<<<< HEAD

const router = createBrowserRouter([
    {
        // 모든 라우터들의 컨테이너 개념. home router도 이 하위에 작성한다다
        path: "/",
        element: <App />,
        children:[
            {
                path: "",
                element: <Home />
            },
            {
                path: "/policy",
                element: <Policy />
            },
            {
                path: "/gamerooms",
                element: <GameRooms />
            },
            {
                path: "/gamelobby/:number",
                element: <GameLobby />
            },
            {
                path: "/gamechats/:number",
                element: <GameChats />
            },
            {
                path: "/board/:category/:number",
                element: <BoardDetails />
            },
            {
                path: "/board/:category",
                element: <Board />
            },
            {
                path: "/board",
                element: <Board />
            },
            {
                path: "/write",
                element: <BoardWrite />
            },
            {
                path: "/user/:userId",
                element: <User />
            },
            {
                path: "/log/:gameId",
                element: <Log />
            },
        ],
        errorElement: <NotFound />
    }
])
=======
import Friends from "../pages/Friends/Friends";
import Game from "../pages/Game/Game";
import GameMyPage from "../pages/GameMyPage/GameMyPage";
import Signup from "../pages/Signup";
import BoardDetail from "../pages/Board/BoardDetail";
import BoardEdit from "../pages/Board/components/BoardEdit";

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
        path: "/signup",
        element: <Signup />,
      },
      {
        path: "/game",
        element: <Game />,
        children: [
          {
            path: "",
            element: <GameMain />,
          },
          {
            path: "room/:number",
            element: <GameRoom />,
          },
          {
            path: "play/:number",
            element: <GamePlay />,
          },
        ],
      },
      {
        path: "/board/detail/:number",
        element: <BoardDetail />,
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
        path: "/board/edit/:number",
        element: <BoardEdit />,
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
    ],
    errorElement: <NotFound />,
  },
]);
>>>>>>> release

export default router;