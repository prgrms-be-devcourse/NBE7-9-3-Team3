export interface User {
  memberId: number;
  email: string;
  nickname: string;
  profileImage?: string;
  followerCount?: number;
  followingCount?: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  nickname: string;
  profileImage?: string;
}

export interface LoginResponse {
  memberId: number;
  email: string;
  nickname: string;
  profileImage?: string;
}

export interface SignupResponse {
  memberId: number;
  email: string;
  nickname: string;
  profileImage?: string;
  createDate: string;
}

export interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

