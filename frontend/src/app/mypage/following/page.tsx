'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';

interface FollowUser {
  memberId: number;
  nickname: string;
  profileImage: string | null;
  following?: boolean;
}

interface FollowListResponse {
  users: FollowUser[];
  totalCount: number;
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

export default function FollowingPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [following, setFollowing] = useState<FollowUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [unfollowing, setUnfollowing] = useState<Set<number>>(new Set());
  const [searchQuery, setSearchQuery] = useState('');
  const [filteredFollowing, setFilteredFollowing] = useState<FollowUser[]>([]);

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || !user)) {
      router.push('/login');
      return;
    }

    if (isAuthenticated && user) {
      fetchFollowing();
    }
  }, [isAuthenticated, authLoading, user, router]);

  const fetchFollowing = async () => {
    try {
      setLoading(true);
      setError(null);
      const response: ApiResponse<FollowListResponse> = await fetchApi(`/api/follows/${user!.memberId}/followings`);
      if (response.data) {
        setFollowing(response.data.users);
        setFilteredFollowing(response.data.users);
      }
    } catch (error) {
      console.error('팔로잉 목록 조회 실패:', error);
      setError('팔로잉 목록을 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    if (!query.trim()) {
      setFilteredFollowing(following);
    } else {
      const filtered = following.filter(user => 
        user.nickname.toLowerCase().includes(query.toLowerCase())
      );
      setFilteredFollowing(filtered);
    }
  };

  const handleUnfollow = async (memberId: number) => {
    try {
      setUnfollowing(prev => new Set(prev).add(memberId));
      
      const response = await fetchApi(`/api/follows/${memberId}`, {
        method: 'DELETE',
      });
      
      if (response.resultCode === '200') {
        // 목록에서 제거
        setFollowing(prev => prev.filter(user => user.memberId !== memberId));
        setFilteredFollowing(prev => prev.filter(user => user.memberId !== memberId));
      } else {
        console.error('언팔로우 실패:', response.msg);
      }
    } catch (error) {
      console.error('언팔로우 실패:', error);
    } finally {
      setUnfollowing(prev => {
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

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
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
                <h1 className="text-2xl font-bold text-gray-900">팔로우</h1>
                <p className="text-gray-600">{following.length}명</p>
              </div>
            </div>
          </div>
        </div>

        {/* 팔로우 검색 섹션 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">팔로우 검색</h2>
          <div className="flex space-x-4">
            <div className="flex-1">
              <input
                type="text"
                placeholder="팔로우 중에서 닉네임을 검색해보세요..."
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>
        </div>

        {/* 팔로우 목록 */}
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
                onClick={fetchFollowing}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                다시 시도
              </button>
            </div>
          ) : filteredFollowing.length === 0 && !searchQuery ? (
            <div className="p-8 text-center">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">아직 팔로우하는 사용자가 없습니다</h3>
              <p className="mt-1 text-sm text-gray-500">관심 있는 사용자를 팔로우해보세요.</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {filteredFollowing.map((user) => (
                <div key={`following-${user.memberId}`} className="p-6 hover:bg-gray-50 transition-colors">
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
                      <button
                        onClick={() => handleUnfollow(user.memberId)}
                        disabled={unfollowing.has(user.memberId)}
                        className={`px-4 py-2 rounded-lg transition-colors ${
                          unfollowing.has(user.memberId)
                            ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                            : 'bg-red-500 text-white hover:bg-red-600'
                        }`}
                      >
                        {unfollowing.has(user.memberId) ? '언팔로우 중...' : '언팔로우'}
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}