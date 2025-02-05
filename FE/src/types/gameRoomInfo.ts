export interface GameRoom {
  id: string;
  title: string;
  players: number;
  maxPlayers: number;
  status: "waiting" | "playing";
  createdAt: Date;
}

export interface GameRoomCardProps {
  room: GameRoom;
}
