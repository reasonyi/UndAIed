// import { useQuery } from "@tanstack/react-query";
// import { userApi } from "../api/userDataApi";

// export function useUserProfile() {
//   return useQuery({
//     queryKey: ["userProfile"],
//     queryFn: async () => {
//       const response = await userApi.get("api/v1/user/profile");
//       console.log("유저 데이터 수신 완료", response);
//       return response.data;
//     },
//   });
// }

// useUserData.ts
import { useQuery } from "@tanstack/react-query";
import { apiClient } from "../api/apiClient";
import { useRecoilValue } from "recoil";
import { userState } from "../store/userState";

export function useUserProfile() {
  const user = useRecoilValue(userState);

  return useQuery({
    queryKey: ["userProfile"],
    queryFn: async () => {
      const response = await apiClient.get("api/v1/user/profile", {
        headers: {
          Authorization: `Bearer ${user.token}`,
        },
      });
      console.log("유저 데이터 수신 완료", response);
      return response.data;
    },
    enabled: !!user.token, // 토큰이 있을 때만 쿼리 실행
    retry: 1,
    refetchOnWindowFocus: false,
  });
}
