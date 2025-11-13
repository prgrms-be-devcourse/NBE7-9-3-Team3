'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';

interface LikedPost {
  id: number;
  title: string;
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

export default function LikedPostsPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [likedPosts, setLikedPosts] = useState<LikedPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || !user)) {
      router.push('/login');
      return;
    }

    if (isAuthenticated && user) {
      fetchLikedPosts();
    }
  }, [isAuthenticated, authLoading, user, router]);

  const fetchLikedPosts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response: ApiResponse<LikedPost[]> = await fetchApi('/api/posts/likes/my');
      console.log('좋아요한 글 응답:', response); // 디버깅용
      
      if (response.data && Array.isArray(response.data)) {
        setLikedPosts(response.data);
      } else {
        console.log('예상과 다른 응답 구조:', response);
        setLikedPosts([]);
      }
    } catch (error) {
      console.error('좋아요한 글 조회 실패:', error);
      setError('좋아요한 글을 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => router.back()}
                className="p-2 hover:bg-gray-100 rounded-full transition-colors"
              >
                <svg className="h-6 w-6 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">좋아요한 글</h1>
                <p className="text-gray-600">내가 좋아요를 누른 게시글들을 확인해보세요</p>
              </div>
            </div>
          </div>
        </div>

        {/* 좋아요한 글 목록 */}
        <div className="bg-white rounded-lg shadow-sm">
          {error ? (
            <div className="p-8 text-center">
              <div className="text-red-500 mb-4">
                <svg className="mx-auto h-12 w-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
              </div>
              <h3 className="text-lg font-medium text-gray-900 mb-2">오류가 발생했습니다</h3>
              <p className="text-gray-500 mb-4">{error}</p>
              <button
                onClick={fetchLikedPosts}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                다시 시도
              </button>
            </div>
          ) : loading ? (
            <div className="p-8 text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
              <p className="text-gray-600">좋아요한 글을 불러오는 중...</p>
            </div>
          ) : likedPosts.length === 0 ? (
            <div className="p-8 text-center">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">좋아요한 글이 없습니다</h3>
              <p className="mt-1 text-sm text-gray-500">마음에 드는 게시글에 좋아요를 눌러보세요.</p>
              <div className="mt-4">
                <button
                  onClick={() => router.push('/posts/showoff')}
                  className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors mr-2"
                >
                  자랑게시판 보기
                </button>
                <button
                  onClick={() => router.push('/posts/question')}
                  className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
                >
                  질문게시판 보기
                </button>
              </div>
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {likedPosts.map((post) => {
                // "SHOWOFF - " 접두사 제거
                const cleanTitle = post.title.replace(/^SHOWOFF - /, '');
                
                return (
                  <div key={post.id} className="p-6 hover:bg-gray-50 transition-colors">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <h3 className="text-lg font-medium text-gray-900 truncate">
                          {cleanTitle}
                        </h3>
                        <div className="mt-2 flex items-center space-x-4">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                            좋아요한 글
                          </span>
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                            자랑게시판
                          </span>
                        </div>
                      </div>
                      
                      {/* 액션 버튼들 */}
                      <div className="flex-shrink-0 flex items-center space-x-2">
                        <button
                          onClick={() => {
                            // 자랑게시판 상세보기 페이지로 이동
                            router.push(`/posts/showoff/${post.id}`);
                          }}
                          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors text-sm"
                        >
                          상세보기
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
