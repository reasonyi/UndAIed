import { Category } from "../../../types/board";

function CategoryNav({ category }: Category) {
  return (
    <ul className="w-full max-w-[1260px] mx-auto flex flex-col sm:flex-row mt-10 h-14">
      <li
        className={`${
          category === "notice"
            ? " border border-x-black border-t-black"
            : "text-[#868686]"
        } flex items-center w-full sm:w-1/2`}
      >
        <a className="w-full text-center" href="/board/notice">
          공지사항
        </a>
      </li>
      <li
        className={`${
          category === "bugreport" ? "border border-x-black border-t-black" : ""
        } flex items-center w-full sm:w-1/2  ${
          category !== "bugreport" ? "text-[#868686]" : ""
        }`}
      >
        <a className="w-full text-center" href="/board/bugreport">
          버그리포트
        </a>
      </li>
    </ul>
  );
}

export default CategoryNav;
