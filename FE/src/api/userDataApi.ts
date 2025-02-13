import { ProfileUpdate } from "../types/User";
import { apiClient } from "./apiClient";

export const updateProfile = async (data: ProfileUpdate) => {
  const response = await apiClient.patch("api/v1/user/profile", data);
  return response.data;
};
