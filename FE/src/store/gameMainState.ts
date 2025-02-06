import { atom } from "recoil";
import { GameRoom } from "../types/gameRoomInfo";

export const gameMainState = atom<GameRoom[]>({
  key: "gameRoomsState",
  default: [],
});
