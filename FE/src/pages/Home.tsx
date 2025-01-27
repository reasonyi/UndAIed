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
      <div className="on-scroll flex w-screen min-h-[calc(100vh-3.5rem)]">
        <div className="w-[1px] h-[400vh] primary-bg-black"></div>
        <div className="grow flex flex-col sticky top-[3.5rem] overflow-hidden h-[calc(100vh-3.5rem)] primary-bg-black">
          <div className="flex flex-col flex-grow w-full max-w-screen-md self-center py-16 px-4">
            <span className="text-white">우리 중에 AI가 있다.</span>
            <div className="grow relative">
              <div
                className="absolute top-0 left-0 w-auto h-full flex gap-4"
                style={{
                  transform: `translateX(calc(-4.18301% + 30.7869px)) translateZ(0px);`,
                }}
              >
                <div className="h-full border-2 rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] flex flex-col gap-4"></div>
                <div className="h-full border-2 rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] flex flex-col gap-4"></div>
                <div className="h-full border-2 rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] flex flex-col gap-4"></div>
                <div className="h-full border-2 rounded-xl p-8 grow min-w-[360px] w-[60vw] max-w-[600px] flex flex-col gap-4"></div>
              </div>
            </div>
            <div className="bg-slate-400">게임으로</div>
          </div>
        </div>
      </div>
      <div className="relative white-container min-h-[calc(70vh-3.5rem)] bg-white pr-20 py-10 flex md:pl-[calc(48rem+5rem)]">
        <div className="md:flex hidden w-[48rem] bg-orange-300 left-[max(5rem,calc(50%-45rem+5rem))] absolute">
          <div className="">
            <h1 className="text-md font-bold ">공지사항</h1>
          </div>
          <div className="">
            <h1 className="text-md font-bold">버그 제보</h1>
          </div>
        </div>
        <div className="flex w-full justify-center">
          <div className="">게임시작 컨테이너</div>
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
