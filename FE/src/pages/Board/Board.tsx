<<<<<<< HEAD
import { useParams } from "react-router-dom";
=======
import { useParams } from "react-router";
import { useRecoilState } from "recoil";
import { currentPageState } from "../../store/boardState";
import boardBanner from "../../assets/board/upscalingBoard.png";
import { CategoryType } from "../../types/board";
>>>>>>> release

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