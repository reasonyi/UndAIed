import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { boardApi, BoardRequest, AdminBoardApi } from "../api/board/boardApi";
import { Post } from "../types/board";

//all list
export const useGetPosts = () => {
  return useQuery({
    queryKey: ["posts"],
    queryFn: async () => {
      const response = await boardApi.getPosts();
      return response.data.data;
    },
    staleTime: Infinity, // 데이터가 업데이트되더라도 리렌더링을 방지함함
  });
};

//상세페이지
export const useGetPost = (id: number) => {
  return useQuery<Post>({
    queryKey: ["post", id],
    queryFn: async () => {
      const response = await boardApi.getPost(id);
      return response.data.data;
    },
    enabled: !!id, //id가 있을때만 쿼리 실행
    staleTime: 1000 * 60 * 5, //5분동안 데이터를 fresh하게 유지지
  });
};

export const useCreatePost = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: BoardRequest) => {
      const response = await boardApi.createPost(data);
      return response.data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["posts"] });
    },
  });
};

export function useAdminCreatePost() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: BoardRequest) => {
      const response = await AdminBoardApi.createPost(data);
      return response.data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["posts"] });
    },
  });
}
