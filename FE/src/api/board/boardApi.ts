import axios from "axios";

export interface BoardRequest {
  title: string;
  content: string;
  category: "0" | "1";
}

const api = axios.create({
  baseURL: "/api/v1/board",
  headers: {
    "Content-Type": "application/json",
  },
});

const adminApi = axios.create({
  baseURL: "/api/v1/admin/board",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  //요청 보내기전에 가로채서 header에 토큰 주입
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

adminApi.interceptors.request.use((config) => {
  //요청 보내기전에 가로채서 header에 토큰 주입
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const boardApi = {
  getPosts: (page: number = 1) => api.get("", { params: { page } }),
  getPost: (id: number) => api.get(`${id}`),
  createPost: (data: BoardRequest) => api.post("", data),
  updatePost: (id: number, data: Partial<BoardRequest>) => api.put(`${id}`),
  deletePost: (id: number) => api.delete(`/${id}`),
};

export const AdminBoardApi = {
  //공지사항
  createPost: (data: BoardRequest) => adminApi.post("", data),
  updatePost: (id: number, data: Partial<BoardRequest>) =>
    api.put(`${id}`, data),
  deletePost: (id: number) => api.delete(`/${id}`),
};
