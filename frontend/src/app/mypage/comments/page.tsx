'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';

interface PostComment {
  id: number;
  postId: number;
  postTitle: string;
  content: string;
  boardType: string;
  category: string;
}

interface TradeComment {
  id: number;
  tradeId: number;
  tradeTitle: string;
  content: string;
  boardType: string;
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

export default function MyCommentsPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [postComments, setPostComments] = useState<PostComment[]>([]);
  const [tradeComments, setTradeComments] = useState<TradeComment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState<'posts' | 'trades'>('posts');

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || !user)) {
      router.push('/login');
      return;
    }

    if (isAuthenticated && user) {
      if (selectedTab === 'posts') {
        fetchMyPostComments();
      } else {
        fetchMyTradeComments();
      }
    }
  }, [isAuthenticated, authLoading, user, router, selectedTab]);

  const fetchMyPostComments = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('게시글 댓글 API 호출 시작...'); // 디버깅용
      const response: ApiResponse<PostComment[]> = await fetchApi('/api/posts/comments/my');
      console.log('게시글 댓글 응답:', response); // 디버깅용
      console.log('게시글 댓글 데이터:', response.data); // 디버깅용
      if (response.data && Array.isArray(response.data)) {
        setPostComments(response.data);
        console.log('게시글 댓글 개수:', response.data.length); // 디버깅용
      } else {
        console.log('게시글 댓글 데이터가 없습니다:', response);
        setPostComments([]);
      }
    } catch (error) {
      console.error('내가 작성한 게시글 댓글 조회 실패:', error);
      setError('내가 작성한 게시글 댓글을 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const fetchMyTradeComments = async () => {
    try {
      setLoading(true);
      setError(null);
      const response: ApiResponse<TradeComment[]> = await fetchApi('/api/market/comments/my');
      console.log('거래글 댓글 응답:', response); // 디버깅용
      if (response.data) {
        setTradeComments(response.data);
      } else {
        console.log('거래글 댓글 데이터가 없습니다:', response);
        setTradeComments([]);
      }
    } catch (error) {
      console.error('내가 작성한 거래글 댓글 조회 실패:', error);
      setError('내가 작성한 거래글 댓글을 불러올 수 없습니다.');
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
                <h1 className="text-2xl font-bold text-gray-900">내가 작성한 댓글</h1>
                <p className="text-gray-600">게시글과 거래글에 작성한 댓글들을 확인해보세요</p>
              </div>
            </div>
          </div>
        </div>

        {/* 탭 선택 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex space-x-4">
            <button
              onClick={() => setSelectedTab('posts')}
              className={`px-6 py-3 rounded-lg font-medium transition-colors ${selectedTab === 'posts'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
              게시글 댓글
            </button>
            <button
              onClick={() => setSelectedTab('trades')}
              className={`px-6 py-3 rounded-lg font-medium transition-colors ${selectedTab === 'trades'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
            >
              거래글 댓글
            </button>
          </div>
        </div>

        {/* 댓글 목록 */}
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
                onClick={selectedTab === 'posts' ? fetchMyPostComments : fetchMyTradeComments}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                다시 시도
              </button>
            </div>
          ) : loading ? (
            <div className="p-8 text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
              <p className="text-gray-600">
                {selectedTab === 'posts' ? '게시글 댓글을 불러오는 중...' : '거래글 댓글을 불러오는 중...'}
              </p>
            </div>
          ) : selectedTab === 'posts' ? (
            postComments.length === 0 ? (
              <div className="p-8 text-center">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">작성한 게시글 댓글이 없습니다</h3>
                <p className="mt-1 text-sm text-gray-500">게시글에 댓글을 작성해보세요.</p>
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
                {postComments.map((comment) => (
                  <div key={comment.id} className="p-6 hover:bg-gray-50 transition-colors">
                    <div className="flex items-start justify-between">
                      <div className="flex-1 min-w-0">
                        <h3 className="text-lg font-medium text-gray-900 truncate">
                          {comment.postTitle}
                        </h3>
                        <p className="mt-2 text-gray-600">{comment.content}</p>
                        <div className="mt-2 flex items-center space-x-4">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                            게시글 댓글
                          </span>

                          {/* boardType에 따라 게시판 종류 표시 */}
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                            {comment.boardType === "SHOWOFF"
                              ? "자랑 게시판"
                              : comment.boardType === "QUESTION"
                                ? "질문 게시판"
                                : comment.boardType}
                          </span>
                          {comment.boardType === "QUESTION" && (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                              {comment.category === "FISH" ? "물고기" : comment.category === "AQUARIUM" ? "수조" : comment.category}
                            </span>
                          )}
                        </div>
                      </div>

                      {/* 상세보기 버튼 */}
                      <div className="flex-shrink-0 flex items-center space-x-2">
                        <button
                          onClick={() => {
                            router.push(`/posts/${comment.boardType.toLowerCase()}/${comment.postId}`);
                          }}
                          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors text-sm"
                        >
                          상세보기
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )
          ) : (
            tradeComments.length === 0 ? (
              <div className="p-8 text-center">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">작성한 거래글 댓글이 없습니다</h3>
                <p className="mt-1 text-sm text-gray-500">거래글에 댓글을 작성해보세요.</p>
                <div className="mt-4">
                  <button
                    onClick={() => router.push('/market/fish')}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors mr-2"
                  >
                    물고기 거래 보기
                  </button>
                  <button
                    onClick={() => router.push('/market/secondhand')}
                    className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
                  >
                    중고물품 거래 보기
                  </button>
                </div>
              </div>
            ) : (
              <div className="divide-y divide-gray-200">
                {tradeComments.map((comment) => (
                  <div key={comment.id} className="p-6 hover:bg-gray-50 transition-colors">
                    <div className="flex items-start justify-between">
                      <div className="flex-1 min-w-0">
                        <h3 className="text-lg font-medium text-gray-900 truncate">
                          {comment.tradeTitle}
                        </h3>
                        <p className="mt-2 text-gray-600">{comment.content}</p>
                        <div className="mt-2 flex items-center space-x-4">
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                            거래글 댓글
                          </span>
                          <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                            {comment.boardType === 'FISH' ? '물고기 거래' : '중고물품 거래'}
                          </span>
                        </div>
                      </div>

                      {/* 상세보기 버튼 */}
                      <div className="flex-shrink-0 flex items-center space-x-2">
                        <button
                          onClick={() => {
                            // 거래글 상세보기 페이지로 이동
                            const boardPath = comment.boardType === 'FISH' ? 'fish' : 'secondhand';
                            router.push(`/market/${boardPath}/${comment.tradeId}`);
                          }}
                          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors text-sm"
                        >
                          상세보기
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )
          )}
        </div>
      </div>
    </div>
  );
}