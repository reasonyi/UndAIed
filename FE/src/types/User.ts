export interface IUser {
  isLogin: boolean;
  email: string;
  token: string;
  nickname: string;
  totalWin: number;
  totalLose: number;
}
export interface Game {
  gameId: number;
  roomTitle: string;
  startedAt: string;
  playTime: string;
}

export interface UserData {
  nickname: string;
  profileImage: number;
  avatar: number;
  sex: boolean;
  age: number;
  totalWin: number;
  totalLose: number;
  game: Game[];
}

export interface UserDataResponse {
  timeStamp: string;
  isSuccess: boolean;
  status: number;
  message: string;
  data: UserData;
}

export interface GameUserInfoProps {
  userInfo: UserData;
}
