// import { useQuery } from "@tanstack/react-query";
// import axios from "axios";

// export const userApi = axios.create({
//   baseURL: "https://i12b212.p.ssafy.io",
//   // baseURL: "http://localhost5173.com",
// });


import axios from "axios";

export const userApi = axios.create({
  baseURL: "https://i12b212.p.ssafy.io",
  withCredentials: true,
});

// Request interceptor
userApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor
userApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 토큰 갱신 또는 로그아웃 로직
    }
    return Promise.reject(error);
  }
);