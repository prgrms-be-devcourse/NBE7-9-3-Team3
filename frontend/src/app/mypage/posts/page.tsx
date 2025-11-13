'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';

interface MyPost {
  id: number;
  title: string;
  displaying: 'PUBLIC' | 'PRIVATE';
}

interface Trade {
  tradeId: number;
  title: string;
  description: string;
  price: number;
  status: 'SELLING' | 'COMPLETED' | 'CANCELLED';
  boardType: 'FISH' | 'SECONDHAND';
  createdDate: string;
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

export default function MyPostsPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [posts, setPosts] = useState<MyPost[]>([]);
  const [trades, setTrades] = useState<Trade[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState<'posts' | 'trades'>('posts');
  const [selectedBoardType, setSelectedBoardType] = useState<'SHOWOFF' | 'QUESTION'>('SHOWOFF');

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || !user)) {
      router.push('/login');
      return;
    }

    if (isAuthenticated && user) {
      if (selectedTab === 'posts') {
        fetchMyPosts();
      } else {
        fetchMyTrades();
      }
    }
  }, [isAuthenticated, authLoading, user, router, selectedBoardType, selectedTab]);

  const fetchMyPosts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response: ApiResponse<MyPost[]> = await fetchApi(`/api/posts/my?boardType=${selectedBoardType}`);
      if (response.data) {
        setPosts(response.data);
      }
    } catch (error) {
      console.error('내가 작성한 글 조회 실패:', error);
      setError('내가 작성한 글을 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const fetchMyTrades = async () => {
    try {
      setLoading(true);
      setError(null);

      // 중고물품과 물고기 거래글을 모두 가져오기
      const [secondhandResponse, fishResponse] = await Promise.all([
        fetchApi('/api/market/secondhand/my?page=0&size=100'),
        fetchApi('/api/market/fish/my?page=0&size=100')
      ]);

      const allTrades: Trade[] = [];

      // 중고물품 거래글 추가
      if (secondhandResponse.data?.content) {
        allTrades.push(...secondhandResponse.data.content);
      }

      // 물고기 거래글 추가
      if (fishResponse.data?.content) {
        allTrades.push(...fishResponse.data.content);
      }

      // 최신순으로 정렬 (createdDate 기준 내림차순)
      allTrades.sort((a, b) => {
        return new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime();
      });

      setTrades(allTrades);
    } catch (error) {
      console.error('내가 작성한 거래글 조회 실패:', error);
      setError('내가 작성한 거래글을 불러올 수 없습니다.');
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
                <h1 className="text-2xl font-bold text-gray-900">내가 작성한 게시글 & 거래글</h1>
              </div>
            </div>
          </div>
        </div>

        {/* 탭 선택 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex space-x-4">
            <button
              onClick={() => setSelectedTab('posts')}
              className={`px-6 py-3 rounded-lg font-medium transition-colors ${
                selectedTab === 'posts'
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              게시글
            </button>
            <button
              onClick={() => setSelectedTab('trades')}
              className={`px-6 py-3 rounded-lg font-medium transition-colors ${
                selectedTab === 'trades'
                  ? 'bg-blue-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              거래글
            </button>
          </div>
        </div>

        {/* 게시판 타입 선택 (게시글 탭일 때만) */}
        {selectedTab === 'posts' && (
          <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
            <div className="flex space-x-4">
              <button
                onClick={() => setSelectedBoardType('SHOWOFF')}
                className={`px-6 py-3 rounded-lg font-medium transition-colors ${
                  selectedBoardType === 'SHOWOFF'
                    ? 'bg-green-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                자랑게시판
              </button>
              <button
                onClick={() => setSelectedBoardType('QUESTION')}
                className={`px-6 py-3 rounded-lg font-medium transition-colors ${
                  selectedBoardType === 'QUESTION'
                    ? 'bg-green-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                질문게시판
              </button>
            </div>
          </div>
        )}

        {/* 게시글/거래글 목록 */}
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
                onClick={selectedTab === 'posts' ? fetchMyPosts : fetchMyTrades}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                다시 시도
              </button>
            </div>
          ) : loading ? (
            <div className="p-8 text-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
              <p className="text-gray-600">
                {selectedTab === 'posts' ? '게시글을 불러오는 중...' : '거래글을 불러오는 중...'}
              </p>
            </div>
          ) : selectedTab === 'posts' ? (
            posts.length === 0 ? (
            <div className="p-8 text-center">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">
                {selectedBoardType === 'SHOWOFF' ? '자랑게시판' : '질문게시판'}에 작성한 글이 없습니다
              </h3>
              <p className="mt-1 text-sm text-gray-500">첫 번째 게시글을 작성해보세요.</p>
              <div className="mt-4">
                <button
                  onClick={() => router.push(`/posts/${selectedBoardType === 'SHOWOFF' ? 'showoff' : 'question'}`)}
                  className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                >
                  게시글 작성하기
                </button>
              </div>
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {posts.map((post, index) => (
                <div key={index} className="p-6 hover:bg-gray-50 transition-colors">
                  <div className="flex items-center justify-between">
                    <div className="flex-1 min-w-0">
                      <h3 className="text-lg font-medium text-gray-900 truncate">
                        {post.title}
                      </h3>
                      <div className="mt-2 flex items-center space-x-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          post.displaying === 'PUBLIC' 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-gray-100 text-gray-800'
                        }`}>
                          {post.displaying === 'PUBLIC' ? '공개' : '비공개'}
                        </span>
                        <span className="text-sm text-gray-500">
                          {selectedBoardType === 'SHOWOFF' ? '자랑게시판' : '질문게시판'}
                        </span>
                      </div>
                    </div>
                    
                    {/* 액션 버튼들 */}
                    <div className="flex-shrink-0 flex items-center space-x-2">
                      <button
                        onClick={() => {
                          const boardPath = selectedBoardType === 'SHOWOFF' ? 'showoff' : 'question';
                          router.push(`/posts/${boardPath}/${post.id}`);
                        }}
                        className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors text-sm"
                      >
                        상세보기
                      </button>
                      <button
                        onClick={() => {
                          const boardPath = selectedBoardType === 'SHOWOFF' ? 'showoff' : 'question';
                          router.push(`/posts/${boardPath}/${post.id}/edit`);
                        }}
                        className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors text-sm"
                      >
                        수정
                      </button>
                      <button
                        onClick={async () => {
                          if (!confirm('정말 삭제하시겠습니까?')) return;

                          try {
                            await fetchApi(`/api/posts/${post.id}`, {
                              method: 'DELETE'
                            });
                            alert('게시글이 삭제되었습니다.');
                            fetchMyPosts(); // 목록 새로고침
                          } catch (error) {
                            console.error('게시글 삭제 실패:', error);
                            alert('게시글 삭제에 실패했습니다.');
                          }
                        }}
                        className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors text-sm"
                      >
                        삭제
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            )
          ) : (
            trades.length === 0 ? (
              <div className="p-8 text-center">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-900">작성한 거래글이 없습니다</h3>
                <p className="mt-1 text-sm text-gray-500">첫 번째 거래글을 작성해보세요.</p>
                <div className="mt-4">
                  <button
                    onClick={() => router.push('/market/fish')}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors mr-2"
                  >
                    물고기 거래글 작성
                  </button>
                  <button
                    onClick={() => router.push('/market/secondhand')}
                    className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
                  >
                    중고물품 거래글 작성
                  </button>
                </div>
              </div>
            ) : (
              <div className="divide-y divide-gray-200">
                {trades.map((trade) => (
                  <div key={trade.tradeId} className="p-6 hover:bg-gray-50 transition-colors">
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <h3 className="text-lg font-medium text-gray-900 truncate">
                          {trade.title}
                        </h3>
                        <p className="text-sm text-gray-600 mt-1 truncate">
                          {trade.description}
                        </p>
                        <div className="mt-2 flex items-center space-x-4">
                          <span className="text-lg font-bold text-blue-600">
                            {trade.price.toLocaleString()}원
                          </span>
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            trade.status === 'SELLING' 
                              ? 'bg-green-100 text-green-800' 
                              : trade.status === 'COMPLETED'
                              ? 'bg-blue-100 text-blue-800'
                              : 'bg-gray-100 text-gray-800'
                          }`}>
                            {trade.status === 'SELLING' ? '판매중' : trade.status === 'COMPLETED' ? '거래완료' : '거래취소'}
                          </span>
                          <span className="text-sm text-gray-500">
                            {trade.boardType === 'FISH' ? '물고기 거래' : '중고물품 거래'}
                          </span>
                        </div>
                      </div>
                      
                      {/* 액션 버튼들 */}
                      <div className="flex-shrink-0 flex items-center space-x-2">
                        <button
                          onClick={() => {
                            const boardPath = trade.boardType === 'FISH' ? 'fish' : 'secondhand';
                            router.push(`/market/${boardPath}/${trade.tradeId}`);
                          }}
                          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors text-sm"
                        >
                          상세보기
                        </button>
                        <button
                          onClick={() => {
                            const boardPath = trade.boardType === 'FISH' ? 'fish' : 'secondhand';
                            router.push(`/market/${boardPath}/${trade.tradeId}/edit`);
                          }}
                          className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors text-sm"
                        >
                          수정
                        </button>
                        <button
                          onClick={async () => {
                            if (!confirm('정말 삭제하시겠습니까?')) return;

                            try {
                              const boardPath = trade.boardType === 'FISH' ? 'fish' : 'secondhand';
                              await fetchApi(`/api/market/${boardPath}/${trade.tradeId}`, {
                                method: 'DELETE'
                              });
                              alert('거래글이 삭제되었습니다.');
                              fetchMyTrades(); // 목록 새로고침
                            } catch (error) {
                              console.error('거래글 삭제 실패:', error);
                              alert('거래글 삭제에 실패했습니다.');
                            }
                          }}
                          className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors text-sm"
                        >
                          삭제
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