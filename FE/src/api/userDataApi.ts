import { useQuery } from "@tanstack/react-query";
import axios from "axios";

export const userApi = axios.create({
  baseURL: "http://localhost5173.com",
});

userApi.interceptors.request.use(
  //header에 user token 넣어서 api 호출
  (config) => {
    const token = localStorage.getItem("userPersist");
    if (token) {
      config.headers.Authorization = `Brarer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
