import boardImage from "../../../assets/board.jpg";
import { Category } from "../../../types/board";

function Banner({ category }: Category): JSX.Element {
  return (
    <>
      <div
        className="border w-full h-[370px] bg-[#c73838] bg-cover bg-no-repeat relative"
        style={{
          backgroundImage: `url(${boardImage})`,
          backgroundSize: "cover",
          backgroundPosition: "50% 60%",
        }}
      >
        <div className="absolute inset-0 bg-gradient-to-r from-black via-transparent to-black">
          <div className="relative ml-[17%] z-10 h-full flex flex-col justify-center items-left text-white">
            <div className="text-5xl">
              {category === "bugreport" ? "버그리포트" : "공지사항"}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default Banner;
