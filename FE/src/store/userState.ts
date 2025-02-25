<<<<<<< HEAD
import { atom } from 'recoil';
import { IUser } from '../types/User';

export const userState = atom<IUser>({
    key: 'userState',
    default: {
        id: null,
        username: '',
        token: 'default_token',
    },
})
=======
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
    profileImage: 0,
  },
  effects_UNSTABLE: [persistAtom],
});
>>>>>>> release
