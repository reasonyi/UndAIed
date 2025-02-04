import { Link, useNavigate } from "react-router-dom";
import React, { useState } from "react";
import Logo from "../assets/svg-icon/game_logo.svg";

interface IHeaderProps {
  scrollRatio: number;
}

function Header({ scrollRatio }: IHeaderProps) {
  const [scrollTop, setScrollTop] = useState(0);

  const handleScroll = (event: React.UIEvent<HTMLDivElement>) => {
    const currentScrollTop = event.currentTarget.scrollTop;
    setScrollTop(currentScrollTop);
    console.log(scrollTop);
  };

  const navigate = useNavigate();
  const onBoardClick = () => {
    navigate("/board");
  };

  return (
    <header
      onScroll={handleScroll}
      className="flex flex-col header-style w-screen h-14 fixed primary-bg-black z-40"
    >
      <div></div>
      <ul className="flex items-center">
        <li className="ml-12 mr-4">
          <Link to={"/"}>
            <img src={Logo} />
          </Link>
        </li>
        <li className="ml-4 mr-1 font-semibold">
          <Link to={"/policy"}>Policy로</Link>
        </li>
        <li className="ml-4 mr-1 font-semibold">
          <Link to={"/gamerooms"}>Game rooms으로</Link>
        </li>
        <li className="ml-4 mr-1 font-semibold">
          <Link to={"/gameroom/1"}>1번 gameroom로</Link>
        </li>
        <li className="ml-4 mr-1 font-semibold">
          <Link to={"/gameplay/2"}>2번 gameplay로</Link>
        </li>
        <li className="ml-4 mr-1 font-semibold">
          <Link to={"/user/3"}>3번 user로</Link>
        </li>
        <li className="ml-4 mr-1 font-semibold">
          <Link to={"/log/4"}>4번 log로</Link>
        </li>
        <li className="ml-4 mr-1 font-semibold">
          <button onClick={onBoardClick}>board로</button>
        </li>
        <li className="ml-4 mr-1 font-semibold">
          <Link to={"/write"}>게시글작성으로</Link>
        </li>
      </ul>
      <div
        className="header-bottom-style"
        style={{
          background: `linear-gradient(to right, #eee, #eee ${scrollRatio}%, #333 ${scrollRatio}%)`,
        }}
      ></div>
    </header>
  );
}

export default Header;
