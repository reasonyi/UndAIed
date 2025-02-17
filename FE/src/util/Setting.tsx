import React, { useEffect } from "react";
import { atom, useRecoilState, RecoilRoot } from "recoil";
// import { Maximize2, Minimize2, Volume2, VolumeX } from "lucide-react";

// Recoil 상태 정의
interface SettingsState {
  isFullscreen: boolean;
  isMuted: boolean;
  volume: number;
}

const blockStyle =
  "bg-[#0000006c] border border-[#f74a5c]/40 backdrop-blur-[12.20px] text-[#fffbfb] rounded-[5px] transition-all duration-200";
const blockHover =
  "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
const blockActive =
  "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";

export const settingsState = atom<SettingsState>({
  key: "settingsState",
  default: (() => {
    const savedSettings = localStorage.getItem("appSettings");
    if (savedSettings) {
      return JSON.parse(savedSettings);
    }
    return {
      isFullscreen: false,
      isMuted: false,
      volume: 1.0,
    };
  })(),
  effects: [
    ({ onSet }) => {
      onSet((newSettings) => {
        localStorage.setItem("appSettings", JSON.stringify(newSettings));
      });
    },
  ],
});

// Settings 컴포넌트
const Settings = () => {
  const [settings, setSettings] = useRecoilState(settingsState);

  console.log(settings, " 세팅입니다");
  // 모든 미디어 요소에 설정 적용
  const applySettingsToMedia = () => {
    document.querySelectorAll("video, audio").forEach((element) => {
      if (element instanceof HTMLMediaElement) {
        element.volume = settings.volume;
        element.muted = settings.isMuted;
      }
    });
  };

  // 초기 설정 적용
  useEffect(() => {
    applySettingsToMedia();
  }, []);

  // 설정 변경시 적용
  useEffect(() => {
    applySettingsToMedia();
  }, [settings.volume, settings.isMuted]);

  // 새로 추가되는 미디어 요소 감지
  useEffect(() => {
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
          if (node instanceof HTMLMediaElement) {
            node.volume = settings.volume;
            node.muted = settings.isMuted;
          }
        });
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
    });

    return () => observer.disconnect();
  }, [settings.volume, settings.isMuted]);

  // 전체화면 감지
  useEffect(() => {
    const handleFullscreenChange = () => {
      setSettings((prev) => ({
        ...prev,
        isFullscreen: !!document.fullscreenElement,
      }));
    };

    document.addEventListener("fullscreenchange", handleFullscreenChange);
    return () =>
      document.removeEventListener("fullscreenchange", handleFullscreenChange);
  }, []);

  const toggleFullscreen = async () => {
    try {
      if (!document.fullscreenElement) {
        await document.documentElement.requestFullscreen();
      } else {
        await document.exitFullscreen();
      }
    } catch (error) {
      console.error("Fullscreen error:", error);
    }
  };

  const toggleMute = () => {
    setSettings((prev) => {
      const newMuted = !prev.isMuted;
      return { ...prev, isMuted: newMuted };
    });
  };

  const setVolume = (volume: number) => {
    const normalizedVolume = Math.max(0, Math.min(1, volume));
    setSettings((prev) => ({
      ...prev,
      volume: normalizedVolume,
    }));
  };

  return (
    <div className="z-50 fixed inset-0 flex items-center justify-center">
      <div
        className={`${blockStyle} ${blockHover} ${blockActive} flex items-center gap-4 p-4`}
      >
        <button
          onClick={toggleFullscreen}
          className="flex items-center gap-2 px-3 py-2 text-sm rounded-md hover:bg-[#ffffff1a] transition-colors"
          aria-label={
            settings.isFullscreen ? "Exit fullscreen" : "Enter fullscreen"
          }
        >
          {settings.isFullscreen ? (
            <>
              <Minimize2 className="w-5 h-5" /> <span>Exit Fullscreen</span>
            </>
          ) : (
            <>
              <Maximize2 className="w-5 h-5" /> <span>Fullscreen</span>
            </>
          )}
        </button>

        <button
          onClick={toggleMute}
          className="flex items-center gap-2 px-3 py-2 text-sm rounded-md hover:bg-[#ffffff1a] transition-colors"
          aria-label={settings.isMuted ? "Unmute" : "Mute"}
        >
          {settings.isMuted ? (
            <>
              <VolumeX className="w-5 h-5" /> <span>Unmute</span>
            </>
          ) : (
            <>
              <Volume2 className="w-5 h-5" /> <span>Mute</span>
            </>
          )}
        </button>

        <div className="flex items-center gap-2">
          <span className="text-sm">Volume</span>
          <input
            type="range"
            min="0"
            max="1"
            step="0.01"
            value={settings.volume}
            onChange={(e) => setVolume(parseFloat(e.target.value))}
            className="w-24 accent-[#f74a5c]"
            aria-label="Volume control"
          />
        </div>
      </div>
    </div>
  );
};

// // RecoilRoot로 감싼 컴포넌트 내보내기
// export const SettingsWrapper = () => (
//   <RecoilRoot>
//     <Settings />
//   </RecoilRoot>
// );

export default Settings;
