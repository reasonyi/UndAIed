import { atom } from "recoil";

export const setShowIntroState = atom<boolean>({
  key: "settingIntro",
  default: false,
});
