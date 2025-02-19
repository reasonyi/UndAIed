import {
  useQuery,
  useMutation,
  useQueryClient,
  QueryClient,
  useInfiniteQuery,
} from "@tanstack/react-query";
import { boardApi, AdminBoardApi } from "../api/boardApi";
import { BoardRequest, Post, UpdatePostParams } from "../types/board";

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

export const useUpdatePost = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: UpdatePostParams) =>
      boardApi.updatePost(id, data),

    onSuccess: (_, { id }) => {
      // 수정 성공 시 해당 게시글의 캐시를 무효화
      queryClient.invalidateQueries({ queryKey: ["posts", id] });
      // 게시글 목록 캐시도 무효화 (필요한 경우)
      queryClient.invalidateQueries({ queryKey: ["posts"] });
    },
  });
};

const useDeletePost = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (postId: number) => boardApi.deletePost(postId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["posts"] });
      console.log("삭제 완료");
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
