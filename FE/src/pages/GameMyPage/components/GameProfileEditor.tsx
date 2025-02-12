// GameProfileEditor.tsx
import { useState } from "react";
import { ProfileEditModal } from "./ProfileEditModal";
import { GameUserInfoProps } from "../../../types/User";

// 8개의 아바타 이미지 import
import playerIcon1 from "../../../assets/player-icon/player-icon-1.svg";
import playerIcon2 from "../../../assets/player-icon/player-icon-2.svg";
import playerIcon3 from "../../../assets/player-icon/player-icon-3.svg";
import playerIcon4 from "../../../assets/player-icon/player-icon-4.svg";
import playerIcon5 from "../../../assets/player-icon/player-icon-5.svg";
import playerIcon6 from "../../../assets/player-icon/player-orange.svg";
import playerIcon7 from "../../../assets/player-icon/player-pink.svg";
import playerIcon8 from "../../../assets/player-icon/player-white.svg";
import { apiClient } from "../../../api/apiClient";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

interface GameProfileEditorProps {
  isOpen: boolean;
  onClose: () => void;
  userInfo: GameUserInfoProps["userInfo"];
}

export function GameProfileEditor({
  isOpen,
  onClose,
  userInfo,
}: GameProfileEditorProps) {
  const blockStyle =
    "bg-[#0000006c] border border-[#f74a5c]/40 backdrop-blur-[12.20px] text-[#fffbfb] rounded-[5px] transition-all duration-200";
  const blockHover =
    "hover:bg-[#f8376467] hover:border-[#f93c4f] hover:shadow-[0_0_15px_0] hover:shadow-[#F74A5C]";
  const blockActive =
    "active:bg-[#f837644e] active:border-[#f837644e] active:shadow-sm";
  const [nickname, setNickname] = useState(userInfo.nickname);
  const [selectedProfileImage, setSelectedProfileImage] = useState(1); // 기본값 1

  const avatars = [
    { id: 1, src: playerIcon1 },
    { id: 2, src: playerIcon2 },
    { id: 3, src: playerIcon3 },
    { id: 4, src: playerIcon4 },
    { id: 5, src: playerIcon5 },
    { id: 6, src: playerIcon6 },
    { id: 7, src: playerIcon7 },
    { id: 8, src: playerIcon8 },
  ];

  // const handleSave = () => {
  //   // TODO: 저장 로직 구현
  //   const response = apiClient
  //     .patch("api/v1/user/profile", {
  //       sex: null,
  //       profile_image: selectedProfileImage,
  //       avatar: null,
  //       age: null,
  //       nickname: nickname,
  //     })
  //     .then((response) => {
  //       console.log(response);
  //     })
  //     .catch((error) => {
  //       console.log(error);
  //     });
  //   onClose();
  // };

  interface ProfileUpdateDTO {
    sex: null;
    profileImage: number;
    avatar: null;
    age: null;
    nickname: string;
  }

  const updateProfile = async (data: ProfileUpdateDTO) => {
    const response = await apiClient.patch("api/v1/user/profile", data);
    return response.data;
  };

  const queryClient = useQueryClient();
  const updateProfileMutation = useMutation({
    mutationFn: updateProfile,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["userProfile"] });
      toast.success("프로필이 성공적으로 업데이트 되었습니다.");
      onClose();
    },
    onError: (error) => {
      toast.error("프로필 업데이트에 실패했습니다.");
      console.error("프로필 업데이트 오류: ", error);
    },
  });

  const handleSave = () => {
    console.log(selectedProfileImage);
    updateProfileMutation.mutate({
      sex: null,
      profileImage: selectedProfileImage,
      avatar: null,
      age: null,
      nickname: nickname,
    });
  };

  return (
    <ProfileEditModal isOpen={isOpen} onClose={onClose}>
      <div className={`${blockStyle} py-6 px-8`}>
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">프로필 수정</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-white">
            ✕
          </button>
        </div>

        <div className="space-y-4">
          <div>
            <label className="block text-sm mb-1.5">닉네임 수정</label>
            <input
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              className="w-full px-3 py-2 bg-[#2e2e2ef0] border border-[#f74a5c]/40 rounded text-white focus:outline-none focus:border-[#f74a5c]"
              placeholder="닉네임을 입력하세요"
            />
          </div>

          <div>
            <label className="block text-sm mb-2">플레이어 아이콘 선택</label>
            <div className="grid grid-cols-4 gap-2">
              {avatars.map((avatar) => (
                <div
                  key={avatar.id}
                  onClick={() => setSelectedProfileImage(avatar.id)}
                  className={`
                    ${blockStyle}
                    ${
                      selectedProfileImage === avatar.id
                        ? "border-[#f74a5c] shadow-[0_0_15px_0] shadow-[#F74A5C]"
                        : ""
                    }
                    p-2 cursor-pointer transition-all duration-200 hover:border-[#f74a5c]
                  `}
                >
                  <img
                    src={avatar.src}
                    alt={`Avatar ${avatar.id}`}
                    className="w-full h-auto"
                  />
                </div>
              ))}
            </div>
          </div>

          <div className="flex justify-end gap-2 mt-6">
            <button
              onClick={onClose}
              className={`${blockStyle} ${blockHover} hover:shadow-none ${blockActive} px-4 py-2`}
            >
              취소
            </button>
            <button
              onClick={handleSave}
              className={`${blockStyle} ${blockHover} ${blockActive} bg-[#f4393996] hover:bg-[#f12e2ec6] active: px-4 py-2`}
            >
              저장
            </button>
          </div>
        </div>
      </div>
    </ProfileEditModal>
  );
}

export default GameProfileEditor;
