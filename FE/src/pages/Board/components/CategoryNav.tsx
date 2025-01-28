import { Link } from "react-router-dom";
import { BannerProps } from "../../../types/board";

function CategoryNav({ category }: BannerProps) {
  return (
    <ul className="w-full max-w-[1260px] mx-auto flex flex-col sm:flex-row mt-10 h-14">
      <li
        className={`${
          category === "notice"
            ? " border border-x-black border-t-black"
            : "text-[#868686]"
        } flex items-center w-full sm:w-1/2`}
      >
        <Link to="/board/notice" className="w-full text-center">
          공지사항
        </Link>
      </li>
      <li
        className={`${
          category === "bugreport" ? "border border-x-black border-t-black" : ""
        } flex items-center w-full sm:w-1/2  ${
          category !== "bugreport" ? "text-[#868686]" : ""
        }`}
      >
        <Link to="/board/bugreport" className="w-full text-center">
          버그리포트
        </Link>
      </li>
    </ul>
  );
}

export default CategoryNav;
