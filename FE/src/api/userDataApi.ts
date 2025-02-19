import { ProfileUpdate } from "../types/User";
import { apiClient } from "./apiClient";

export const updateProfile = async (data: ProfileUpdate) => {
  const response = await apiClient.patch("api/v1/user/profile", data);

  // status가 400인 경우 에러 객체 반환
  if (response.data.status === 400) {
    console.log("에러 잘 들어왓어요");
    return {
      error: true,
      message: "중복된 닉네임입니다",
    };
  }

  return response.data;
};
