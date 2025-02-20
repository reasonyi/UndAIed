import {
  useQuery,
  useMutation,
  useQueryClient,
  QueryClient,
  useInfiniteQuery,
} from "@tanstack/react-query";
import { boardApi, AdminBoardApi } from "../api/boardApi";
import {
  BoardDetailResponse,
  BoardRequest,
  Post,
  UpdatePostParams,
} from "../types/board";
import { boardRefreshState } from "../store/boardState";
import { useRecoilValue } from "recoil";

//all list
export const useGetPosts = (categoryNum: number, currentPage: number) => {
  return useQuery({
    queryKey: ["posts", categoryNum, currentPage],
    queryFn: async () => {
      try {
        const response = await boardApi.getPosts(categoryNum, currentPage);
        if (!response.data || !response.data.data) {
          console.log("데이터 호출 성공");
          return []; // 데이터가 없는 경우 빈 배열 반환
        }
        return response.data.data;
      } catch (error) {
        console.error("Failed to fetch posts:", error);
        throw error;
      }
    },
    // 추가 옵션
    retry: 1, // 실패시 재시도 횟수
    staleTime: 5 * 60 * 1000, // 5분
    // undefined 반환 방지를 위한 초기값 설정
    initialData: [],
  });
};
//상세페이지
export const useGetPost = (id: number) => {
  return useQuery<BoardDetailResponse>({
    queryKey: ["post", id],
    queryFn: async () => {
      const response = await boardApi.getPost(id);
      return response.data;
    },
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

export const useAdminUpdatePost = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, data }: UpdatePostParams) =>
      AdminBoardApi.updatePost(id, data),

    onSuccess: (_, { id }) => {
      // 수정 성공 시 해당 게시글의 캐시를 무효화
      console.log("업데이트 성공");
      queryClient.invalidateQueries({ queryKey: ["posts", id] });
      // 게시글 목록 캐시도 무효화 (필요한 경우)
      queryClient.invalidateQueries({ queryKey: ["posts"] });
    },
  });
};

export const useDeletePost = () => {
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
