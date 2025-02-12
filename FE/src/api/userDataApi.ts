// import { useQuery } from "@tanstack/react-query";
// import axios from "axios";

// export const userApi = axios.create({
//   baseURL: "https://i12b212.p.ssafy.io",
//   // baseURL: "http://localhost5173.com",
// });

import axios from "axios";

export const userApi = axios.create({
  // baseURL: "https://i12b212.p.ssafy.io",
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
});

userApi.interceptors.request.use(
  //header에 user token 넣어서 api 호출
  (config) => {
    const userPersist = localStorage.getItem("userPersist");
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
