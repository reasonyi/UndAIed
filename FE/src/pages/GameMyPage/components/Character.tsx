import CharacterImage1 from "../../../assets/character/character-demo.png";
import CharacterImage2 from "../../../assets/character/character1.png";
import CharacterImage3 from "../../../assets/character/character2.png";
import { AvatarProps } from "../../../types/User";

function Character({ avatar }: AvatarProps) {
  const blockStyle =
    "bg-[#0000006c] border border-[#f74a5c]/40 backdrop-blur-[12.20px] text-[#fffbfb] rounded-[5px] transition-all duration-200";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";

  const getCharacterImage = (avatar: number) => {
    switch (avatar) {
      case 1:
        return CharacterImage1;
      case 2:
        return CharacterImage2;
      case 3:
        return CharacterImage3;
      default:
        return CharacterImage1; // 기본 이미지
    }
  };

  return (
    <div className="relative z-0 md:w-[600px] justify-around items-center min-h-[300px] md:min-h-0 hidden md:flex flex-col">
      <div className="relative w-full h-full flex items-center justify-center">
        <img
          src={getCharacterImage(avatar)}
          alt={`Character ${avatar}`}
          className="h-full w-full md:pb-24 object-contain -z-10"
        />
      </div>
      <div
        className={`${blockStyle} ${blockHover} ${blockActive} text-center py-4 mb-5 text-sm md:text-base md:w-44 absolute bottom-0 left-1/2 transform -translate-x-1/2`}
      >
        캐릭터 선택
      </div>
    </div>
  );
}

export default Character;
