import { useQuery } from "@tanstack/react-query";
import { userApi } from "../api/userDataApi";

export function useUserProfile() {
  return useQuery({
    queryKey: ["userProfile"],
    queryFn: async () => {
      const response = await userApi.get("api/v1/user/profile");
      console.log("유저 데이터 수신 완료", response);
      return response.data;
    },
  });
}
