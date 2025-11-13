'use client';

import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { User, LoginRequest, SignupRequest, LoginResponse, SignupResponse, ApiResponse } from '@/type/user';
import { fetchApi } from '@/lib/client';
import { uploadImage } from '@/lib/uploadImage';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  signup: (email: string, password: string, nickname: string, profileImage?: File) => Promise<void>;
  updateUser: (userData: Partial<User>) => void;
  refreshUser: () => Promise<void>;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const isAuthenticated = !!user;

  // 컴포넌트 마운트 시 사용자 정보 확인
  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      const data: ApiResponse<User> = await fetchApi('/api/members/me');
      if (data.data) {
        setUser(data.data);
      } else {
        // 데이터가 없으면 로그인하지 않은 상태
        setUser(null);
      }
    } catch (error) {
      // 로그인하지 않은 상태에서는 조용히 처리
      if (error instanceof Error && (
        error.message.includes('Failed to fetch') ||
        error.message.includes('HTTP 401') ||
        error.message.includes('HTTP 403') ||
        error.message.includes('HTTP 404') ||
        error.message.includes('Access Token')
      )) {
        // 로그인하지 않은 상태이므로 조용히 처리
        console.log('사용자가 로그인하지 않았습니다.');
        setUser(null);
      } else {
        console.error('Auth check failed:', error);
        setUser(null);
      }
    } finally {
      setLoading(false);
    }
  };


  const login = async (email: string, password: string) => {
    setLoading(true);
    try {
      const loginData: LoginRequest = { email, password };
      const data: ApiResponse<LoginResponse> = await fetchApi('/api/members/login', {
        method: 'POST',
        body: JSON.stringify(loginData),
      });

      if (data.data) {
        setUser({
          memberId: data.data.memberId,
          email: data.data.email,
          nickname: data.data.nickname,
          profileImage: data.data.profileImage,
        });
      }
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const signup = async (email: string, password: string, nickname: string, profileImage?: File) => {
    setLoading(true);
    try {
      // 1. S3에 이미지 업로드 (있는 경우에만)
      let profileImageUrl: string | undefined;
      if (profileImage) {
        profileImageUrl = await uploadImage(profileImage, 'profile');
      }

      // 2. JSON으로 데이터 전송
      const data: ApiResponse<SignupResponse> = await fetchApi('/api/members/join', {
        method: 'POST',
        body: JSON.stringify({
          email: email.trim(),
          password: password.trim(),
          nickname: nickname.trim(),
          profileImageUrl
        }),
      });

      if (data.data) {
        setUser({
          memberId: data.data.memberId,
          email: data.data.email,
          nickname: data.data.nickname,
          profileImage: data.data.profileImage,
        });
      }
    } catch (error) {
      console.error('Signup error:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setLoading(true);
    try {
      await fetchApi('/api/members/logout', {
        method: 'POST',
      });
    } catch (error) {
      // 로그아웃 에러는 조용히 처리 (이미 토큰이 없거나 만료된 경우)
      if (error instanceof Error && error.message.includes('Access Token')) {
        console.log('이미 로그아웃 상태입니다.');
      } else {
        console.error('Logout error:', error);
      }
    } finally {
      setUser(null);
      setLoading(false);
    }
  };

  const updateUser = (userData: Partial<User>) => {
    setUser(prev => prev ? { ...prev, ...userData } : null);
  };

  const refreshUser = async () => {
    try {
      console.log('사용자 정보 새로고침 시작');
      const data: ApiResponse<User> = await fetchApi('/api/members/me');
      console.log('새로고침된 사용자 데이터:', data);
      if (data.data) {
        setUser(data.data);
        console.log('사용자 정보 업데이트 완료');
      }
    } catch (error) {
      console.error('사용자 정보 새로고침 실패:', error);
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated,
    login,
    logout,
    signup,
    updateUser,
    refreshUser,
    loading,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
