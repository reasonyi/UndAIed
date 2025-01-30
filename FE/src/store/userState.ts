import { atom } from "recoil";
import { IUser } from "../types/user";

export const userState = atom<IUser>({
  key: "userState",
  default: {
    id: null,
    username: "",
    token: "default_token",
  },
});
