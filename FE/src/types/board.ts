export interface Post {
  boardId: number;
  title: string;
  viewCnt: number;
  content: string;
  category: string;
  createAt: string;
  updateAt: string;
}

export interface TableProps {
  currentPosts: Post[];
  currentPage: number;
  postPerPage: number;
}

export interface PostListProps {
  currentPosts: Post[];
  currentPage: number;
  postPerPage: number;
  formatDate: (dateString: string) => string;
}

export interface PaginationProps {
  currentPage: number;
  endPage: number;
  startPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export interface Category {
  category: string;
}

export type CategoryType = "bugreport" | "notice" | "write";
export interface BannerProps {
  category: CategoryType;
  bannerImage?: string;
}
