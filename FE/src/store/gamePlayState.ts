import { atom } from "recoil";

// Recoil atoms
export const isGameEndState = atom<boolean>({
  key: "isGameEndState",
  default: false,
});
