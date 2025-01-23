import { useState, useEffect } from "react";

import { useOutletContext } from "react-router-dom";
import LoggedInHome from "../components/LoggedInHome";
import LoggedOutHome from "../components/LoggedOutHome";
import Header from "../components/Header";
import HeaderTemp from "../components/HeaderTemp";

interface OuletContextType {
  isLoggedIn: boolean;
}

function Home() {
  const { isLoggedIn } = useOutletContext<OuletContextType>();

  const [offset, setOffset] = useState(0);
  const [scrollRatio, setScrollRatio] = useState(0);

  // 스크롤 감지 핸들러
  useEffect(() => {
    const onScroll = () => {
      const docHeight = document.documentElement.scrollHeight; // 전체 문서 높이
      const winHeight = window.innerHeight; // 뷰포트 높이
      const totalScrollableHeight = docHeight - winHeight; // 스크롤 가능한 전체 높이
      setOffset(window.scrollY);
      setScrollRatio(
        window.scrollY === 0
          ? 0
          : (window.scrollY / totalScrollableHeight) * 100
      );
    };
    // clean up code
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  console.log(offset);
  console.log(scrollRatio);

  return (
    <div className="">
      <Header scrollRatio={scrollRatio} />
      <HeaderTemp />
      <div className="on-scroll w-screen min-h-[calc(100vh-3.5rem)] primary-bg-black">
        <div className="w-[1px] h-[400vh]"></div>
      </div>
      {/* <div className="login-div">
          {isLoggedIn ? <LoggedInHome /> : <LoggedOutHome />}
        </div>
        <footer className="w-full"></footer> */}
    </div>
  );
}

export default Home;
