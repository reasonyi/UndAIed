import { useQuery } from "@tanstack/react-query";
import axios from "axios";

export const userApi = axios.create({
  baseURL: "http://localhost:8080",
});

userApi.interceptors.request.use(
  //header에 user token 넣어서 api 호출
  (config) => {
    const userPersist = sessionStorage.getItem("userPersist");
    if (userPersist) {
      const { userState } = JSON.parse(userPersist);
      const token = userState.token; // JSON에서 token만 추출
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
