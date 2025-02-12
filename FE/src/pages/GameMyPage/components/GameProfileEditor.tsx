import { useState } from "react";
import { createPortal } from "react-dom";

function GameProfileEditor() {
  const blockStyle =
    "bg-[#0000006c] border border-[#f74a5c]/40 backdrop-blur-[12.20px] text-[#fffbfb] rounded-[5px] transition-all duration-200";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";
  const [isOpen, setIsOpen] = useState(false);

  const handleClick = () => {
    setIsOpen(!isOpen);
  };

  return (
    <>
      <div
        onClick={handleClick}
        className={`${blockStyle} ${blockActive} ${blockHover} hover:bg-[#f8376441] text-center py-2 px-3 md:py-2 mt-3 md:mt-4 text-sm md:text-base cursor-pointer`}
      >
        정보 수정
      </div>

      {isOpen &&
        createPortal(
          <div className="fixed inset-0 z-[9999]">
            {/* Blur Overlay */}
            <div
              className="fixed inset-0 bg-black/50 backdrop-blur-sm"
              onClick={handleClick}
            />

            {/* Modal Content */}
            <div className="fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md">
              <div className={`${blockStyle} py-6 px-8`}>
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-xl font-semibold">프로필 수정</h2>
                  <button
                    onClick={handleClick}
                    className="text-gray-400 hover:text-white"
                  >
                    ✕
                  </button>
                </div>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm mb-1">닉네임</label>
                    <input
                      type="text"
                      className="w-full px-3 py-2 bg-[#2e2e2ef0] border border-[#f74a5c]/40 rounded text-white focus:outline-none focus:border-[#f74a5c]"
                      placeholder="닉네임을 입력하세요"
                    />
                  </div>

                  <div>
                    <label className="block text-sm mb-1">아바타</label>
                    {/* 아바타 선택 UI */}
                  </div>

                  <div className="flex justify-end gap-2 mt-6">
                    <button
                      onClick={handleClick}
                      className={`${blockStyle} px-4 py-2`}
                    >
                      취소
                    </button>
                    <button className={`${blockStyle} ${blockHover} px-4 py-2`}>
                      저장
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>,
          document.body
        )}
    </>
  );
}

export default GameProfileEditor;
