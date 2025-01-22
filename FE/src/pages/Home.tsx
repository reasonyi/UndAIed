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

  const [scrollX, setScrollX] = useState(0);

  // 스크롤 감지 핸들러
  useEffect(() => {
    const handleScroll = () => {
      setScrollX(window.scrollY); // 스크롤 Y 값을 X 좌표 이동으로 사용
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <>
      <Header scrollRatio={30} />
      <HeaderTemp />
      <div className="w-screen min-h-[calc(100vh-3.5rem)] primary-bg-black">
        <div className="w-[1px] h-[400vh]"></div>
      </div>
      {/* <div className="login-div">
          {isLoggedIn ? <LoggedInHome /> : <LoggedOutHome />}
        </div>
        <footer className="w-full"></footer> */}
    </>
  );
}

export default Home;
