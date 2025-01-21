import { useParams } from "react-router-dom";

function GameLobby(){
    const { number } = useParams();

    return (
        <>
            <h1>GameLobby page</h1>
            <span>로비 번호: {number}</span>
        </>
    )
}

export default GameLobby;