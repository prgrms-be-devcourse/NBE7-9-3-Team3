export enum BoardType {
  FISH = 'FISH',
  SECONDHAND = 'SECONDHAND',
}

export enum TradeStatus {
  SELLING = 'SELLING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface Trade {
  tradeId: number;
  memberId: number;
  memberNickname: string;
  boardType: BoardType;
  title: string;
  description: string;
  price: number;
  status: TradeStatus;
  category: string;
  createdDate: string;
  images: string[];
}

export interface TradeComment {
  commentId: number;
  memberId: number;
  memberNickname: string;
  tradeId: number;
  comment: string;
  createdDate: string;
}

export interface TradeFormData {
  memberId: number;
  title: string;
  description: string;
  price: number;
  status: TradeStatus;
  category: string;
  imageUrls?: string[];
}

export interface CommentFormData {
  memberId: number;
  tradeId: number;
  content: string;
}

export interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
