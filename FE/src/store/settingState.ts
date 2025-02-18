import { atom } from "recoil";
import { SettingsStateType } from "../types/setting";

export const settingsState = atom<SettingsStateType>({
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
