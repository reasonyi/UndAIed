import { atom } from "recoil";
import { IUser } from "../types/User";
import persistAtom from "./persistAtom";

export const userState = atom<IUser>({
  key: "userState",
  default: {
    isLogin: false,
    email: "email",
    token: "",
    nickname: "nickname",
    totalWin: 0,
    totalLose: 0,
  },
  effects_UNSTABLE: [persistAtom],
});
