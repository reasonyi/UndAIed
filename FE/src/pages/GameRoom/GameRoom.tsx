import { useParams } from "react-router-dom";

function GameRoom() {
  const { number } = useParams();

  return (
    <>
      <h1>GameRoom page</h1>
      <span>로비 번호: {number}</span>
    </>
  );
}

export default GameRoom;
