import { useState, useEffect } from "react";

import { Link, useOutletContext } from "react-router-dom";
import LoggedInHome from "../components/LoggedInHome";
import LoggedOutHome from "../components/LoggedOutHome";
import Header from "../components/Header";
import HeaderTemp from "../components/HeaderTemp";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { IconDefinition } from "@fortawesome/fontawesome-svg-core";
import {
  faChevronDown,
  faChevronRight,
} from "@fortawesome/free-solid-svg-icons";
import GoogleIcon from "../assets/svg-icon/google_logo.svg";

interface OuletContextType {
  isLoggedIn: boolean;
}

function Home() {
  const { isLoggedIn } = useOutletContext<OuletContextType>();

  //fontawsome 타입 선언
  const downChervon: IconDefinition = faChevronDown;
  const rightChervon: IconDefinition = faChevronRight;

  const [offset, setOffset] = useState(0);
  const [scrollRatio, setScrollRatio] = useState(0);
  const INFO_VIEWPORT = 3;
  const [infoScrollRatio, setInfoScrollRatio] = useState(0);

  // 스크롤 감지 핸들러
  useEffect(() => {
    const onScroll = () => {
      const docHeight = document.documentElement.scrollHeight; // 전체 문서 높이
      const winHeight = window.innerHeight; // 뷰포트 높이
      const infoHeigt = winHeight * INFO_VIEWPORT; // 안내 스크린 끝까지의 높이이
      const totalScrollableHeight = docHeight - winHeight; // 스크롤 가능한 전체 높이
      setOffset(window.scrollY);
      setScrollRatio(
        window.scrollY === 0
          ? 0
          : (window.scrollY / totalScrollableHeight) * 100
      );
      setInfoScrollRatio(
        window.scrollY === 0
          ? 0
          : window.scrollY / infoHeigt > 1
          ? 100
          : (window.scrollY / infoHeigt) * 100
      );
    };
    // clean up code
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  const onChervonClick = () => {
    window.scrollTo({
      top: window.innerHeight * 4,
      behavior: "smooth",
    });
  };

  return (
    <div className="">
      <Header scrollRatio={scrollRatio} />
      <HeaderTemp />
      <div className="on-scroll flex w-screen min-h-[calc(100vh-3.5rem)]">
        <div className="w-[1px] h-[400vh] primary-bg-black"></div>
        <div className="grow flex flex-col sticky top-[3.5rem] overflow-hidden h-[calc(100vh-3.5rem)] primary-bg-black">
          <div className="flex flex-col flex-grow w-full max-w-screen-md self-center pt-8 pb-4 px-4">
            <span className="text-white mb-8 text-4xl font-extrabold leading-none tracking-tight">
              채팅방 속에 숨어든 AI를 찾아라!
            </span>
            <div className="grow relative">
              <div
                className="absolute top-0 left-0 w-auto h-full flex gap-4 transition-transform"
                style={{
                  transform: `translateX(calc(-${infoScrollRatio}% + ${
                    (infoScrollRatio / 100) * 720
                  }px))`,
                }}
              >
                <div className="h-full border-2 border-[#555555] rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] flex flex-col gap-4">
                  <h1 className="text-3xl font-extrabold text-white mb-4">
                    1.
                  </h1>
                  <h3 className="text-lg text-[#a1a1aa] font-normal mb-4">
                    6명의 플레이어들과 한 방에서 게임을 시작합니다.
                  </h3>
                </div>
                <div className="h-full border-2 border-[#555555] rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] flex flex-col gap-4">
                  <h1 className="text-3xl font-extrabold text-white">2.</h1>
                </div>
                <div className="h-full border-2 border-[#555555] rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] flex flex-col gap-4">
                  <h1 className="text-3xl font-extrabold text-white">3.</h1>
                </div>
                <div className="h-full border-2 border-[#555555] rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] flex flex-col gap-4">
                  <h1 className="text-3xl font-extrabold text-white">4.</h1>
                </div>
              </div>
            </div>
            <div className="flex justify-center">
              <button
                className="group w-[4.5rem] h-[4.5rem] flex flex-col justify-center items-center text-white transition-all mt-2"
                onClick={onChervonClick}
              >
                <FontAwesomeIcon
                  className="animate-bounce w-[1.2rem] h-[1.2rem]"
                  icon={downChervon}
                />
                <h1 className="opacity-0 group-hover:opacity-100 transition-all">
                  게임 시작
                </h1>
              </button>
            </div>
          </div>
        </div>
      </div>
      <div className="flex justify-center bg-[#f7f7f7]">
        <div className="relative white-container min-h-[calc(70vh-3.5rem)] py-10 flex md:pl-[calc(32rem)] lg:pl-[calc(40rem)]">
          <div className="md:flex hidden w-[32rem] lg:w-[40rem] left-[max(0px,calc(50%-45rem))] absolute">
            <div className="w-1/2">
              <Link to={"/board"} className="text-md font-bold text-[#872341]">
                공지사항{" "}
                <FontAwesomeIcon
                  className="w-[1rem] h-[1rem]"
                  icon={rightChervon}
                />
              </Link>
              <ul className="">
                <li className=""></li>
              </ul>
            </div>
            <div className="w-1/2">
              <Link to={"/board"} className="text-md font-bold text-[#872341]">
                버그 제보{" "}
                <FontAwesomeIcon
                  className="w-[1rem] h-[1rem]"
                  icon={rightChervon}
                />
              </Link>
            </div>
          </div>
          <div className="flex w-full justify-center">
            <button className="w-[22.5rem] h-9 border border-[#dadce0] bg-white rounded-[20px] flex items-center justify-between px-3">
              <img src={GoogleIcon} alt="" />
              <div className="text-[#3c4043] text-sm font-medium font-['Roboto']">
                Google로 로그인
              </div>
              <div></div>
            </button>
          </div>
        </div>
      </div>
      <div className="footer min-h-[calc(30vh)] primary-bg-black p-10 text-white">
        <h1>UndAIed</h1>
        <span>© SSAFY Korea Corp. & Daejeon 2nd Class B212</span>
        <div>
          <span></span>
        </div>
      </div>
      {/* <div className="login-div">
          {isLoggedIn ? <LoggedInHome /> : <LoggedOutHome />}
        </div>
        <footer className="w-full"></footer> */}
    </div>
  );
}

export default Home;
