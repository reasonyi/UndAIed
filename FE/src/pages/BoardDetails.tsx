import { useParams } from "react-router-dom";

function BoardDetails(){
    const { category, number } = useParams();

    return (
        <>
            <h1>BoardDetails page</h1>
            <span>게시글 카테고리: {category}</span>
            <span>게시글 번호: {number}</span>
        </>
    )
}

export default BoardDetails;