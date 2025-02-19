import { Link } from "react-router-dom";

function WriteButton() {
  return (
    <>
      <div className="text-right mt-5">
        <Link to="/write">
          <button className="w-30 h-30 border-2 px-11 py-3 bg-black text-white hover:bg-[#484848f7] border-black right-0">
            글쓰기
          </button>
        </Link>
      </div>
    </>
  );
}

export default WriteButton;
