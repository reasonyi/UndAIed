import { Link, useNavigate } from "react-router-dom";
import React, { useState } from "react";
import Logo from "../assets/svg-icon/game_logo.svg";
import { useRecoilValue } from "recoil";
import { userState } from "../store/userState";
import { HashLink } from "react-router-hash-link";

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

  const goToLogin = () => {
    navigate("#login");
  };
  const userData = useRecoilValue(userState);
  const isLogin = userData.isLogin;

  return (
    <header
      onScroll={handleScroll}
      className="flex flex-col pt-4  header-style w-screen h-14 fixed primary-bg-black z-40"
    >
      <ul className="flex justify-between">
        <li className="ml-12 w-56 mr-4">
          <Link to={"/"}>
            <img src={Logo} />
          </Link>
        </li>
        {!isLogin ? (
          <li onClick={goToLogin} className=" ml-4 mr-8  font-semibold">
            <HashLink to="/#login" smooth>
              로그인
            </HashLink>
          </li>
        ) : (
          <li onClick={goToLogin} className=" ml-4 mr-8  font-semibold">
            {userData.nickname}님 환영합니다!
          </li>
        )}
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
