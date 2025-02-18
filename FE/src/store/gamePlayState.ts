import { atom } from "recoil";

export const isGameEndState = atom<boolean>({
  key: "isGameEndState",
  default: false,
});

export const isUserDiedState = atom<boolean>({
  key: "isUserDiedState",
  default: false,
});
