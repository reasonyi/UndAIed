import { useParams } from "react-router-dom";

function Board(){
    const { category = "notice" } = useParams();

    return (
        <>
            <h1>Board page</h1>
            <span>게시글 카테고리: {category}</span>
        </>
    )
}

export default Board;