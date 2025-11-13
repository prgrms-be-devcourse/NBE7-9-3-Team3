'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';

interface SearchUser {
  memberId: number;
  nickname: string;
  profileImage: string | null;
  isFollowing: boolean;
}

interface SearchResponse {
  members: SearchUser[];
  totalCount: number;
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

export default function SearchPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<SearchUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [following, setFollowing] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || !user)) {
      router.push('/login');
      return;
    }
  }, [isAuthenticated, authLoading, user, router]);

  const handleSearch = async (query: string) => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const response: ApiResponse<SearchResponse> = await fetchApi(
        `/api/members/search?nickname=${encodeURIComponent(query)}&page=0&size=20`
      );
      if (response.data) {
        setSearchResults(response.data.members);
      }
    } catch (error) {
      console.error('검색 실패:', error);
      setError('검색 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleFollow = async (memberId: number) => {
    try {
      setFollowing(prev => new Set(prev).add(memberId));
      
      const response = await fetchApi(`/api/follows/${memberId}`, {
        method: 'POST',
      });
      
      if (response.resultCode === '200') {
        // 검색 결과에서 팔로우 상태 업데이트
        setSearchResults(prev => 
          prev.map(user => 
            user.memberId === memberId 
              ? { ...user, isFollowing: true }
              : user
          )
        );
      } else {
        console.error('팔로우 실패:', response.msg);
      }
    } catch (error) {
      console.error('팔로우 실패:', error);
    } finally {
      setFollowing(prev => {
        const newSet = new Set(prev);
        newSet.delete(memberId);
        return newSet;
      });
    }
  };

  const handleUnfollow = async (memberId: number) => {
    try {
      setFollowing(prev => new Set(prev).add(memberId));
      
      const response = await fetchApi(`/api/follows/${memberId}`, {
        method: 'DELETE',
      });
      
      if (response.resultCode === '200') {
        // 검색 결과에서 팔로우 상태 업데이트
        setSearchResults(prev => 
          prev.map(user => 
            user.memberId === memberId 
              ? { ...user, isFollowing: false }
              : user
          )
        );
      } else {
        console.error('언팔로우 실패:', response.msg);
      }
    } catch (error) {
      console.error('언팔로우 실패:', error);
    } finally {
      setFollowing(prev => {
        const newSet = new Set(prev);
        newSet.delete(memberId);
        return newSet;
      });
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
                <h1 className="text-2xl font-bold text-gray-900">회원 검색</h1>
                <p className="text-gray-600">닉네임으로 회원을 검색하고 팔로우하세요</p>
              </div>
            </div>
          </div>
        </div>

        {/* 검색 입력 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex space-x-4">
            <div className="flex-1">
              <input
                type="text"
                placeholder="닉네임을 입력하세요..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSearch(searchQuery);
                  }
                }}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <button
              onClick={() => handleSearch(searchQuery)}
              disabled={loading}
              className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              {loading ? '검색 중...' : '검색'}
            </button>
          </div>
        </div>

        {/* 검색 결과 */}
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
                onClick={() => handleSearch(searchQuery)}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                다시 시도
              </button>
            </div>
          ) : searchResults.length === 0 && searchQuery ? (
            <div className="p-8 text-center">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">검색 결과가 없습니다</h3>
              <p className="mt-1 text-sm text-gray-500">다른 검색어를 시도해보세요.</p>
            </div>
          ) : searchResults.length > 0 ? (
            <div className="divide-y divide-gray-200">
              {searchResults.map((user) => (
                <div key={user.memberId} className="p-6 hover:bg-gray-50 transition-colors">
                  <div className="flex items-center space-x-4">
                    {/* 프로필 이미지 */}
                    <div className="flex-shrink-0">
                      {user.profileImage ? (
                        <img
                          src={user.profileImage}
                          alt={user.nickname}
                          className="h-12 w-12 rounded-full object-cover border-2 border-gray-200"
                        />
                      ) : (
                        <div className="h-12 w-12 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-lg font-bold">
                          {user.nickname.charAt(0).toUpperCase()}
                        </div>
                      )}
                    </div>
                    
                    {/* 사용자 정보 */}
                    <div className="flex-1 min-w-0">
                      <p className="text-lg font-medium text-gray-900 truncate">
                        {user.nickname}
                      </p>
                      <p className="text-sm text-gray-500">
                        ID: {user.memberId}
                      </p>
                    </div>
                    
                    {/* 액션 버튼 */}
                    <div className="flex-shrink-0">
                      {user.isFollowing ? (
                        <button
                          onClick={() => handleUnfollow(user.memberId)}
                          disabled={following.has(user.memberId)}
                          className={`px-4 py-2 rounded-lg transition-colors ${
                            following.has(user.memberId)
                              ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                              : 'bg-red-500 text-white hover:bg-red-600'
                          }`}
                        >
                          {following.has(user.memberId) ? '언팔로우 중...' : '언팔로우'}
                        </button>
                      ) : (
                        <button
                          onClick={() => handleFollow(user.memberId)}
                          disabled={following.has(user.memberId)}
                          className={`px-4 py-2 rounded-lg transition-colors ${
                            following.has(user.memberId)
                              ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                              : 'bg-blue-500 text-white hover:bg-blue-600'
                          }`}
                        >
                          {following.has(user.memberId) ? '팔로우 중...' : '팔로우'}
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-8 text-center">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">회원을 검색해보세요</h3>
              <p className="mt-1 text-sm text-gray-500">닉네임을 입력하여 회원을 찾아보세요.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
