import { useParams } from "react-router-dom";

function GameChats(){
    const { number } = useParams();
    return (
        <>
            <h1>GameChats page</h1>
            <span>게임방 번호: {number}</span>
        </>
    )
}

export default GameChats;