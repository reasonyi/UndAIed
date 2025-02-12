// api/axiosInstance.ts 
import axios from "axios";

const baseURL = "https://i12b212.p.ssafy.io";

export const axiosInstance = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터 설정
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("recoil-persist")
      ? JSON.parse(localStorage.getItem("recoil-persist")!).userState.token
      : null;
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);