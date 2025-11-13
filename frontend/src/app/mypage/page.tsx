'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';

interface MemberResponse {
  memberId: number;
  email: string;
  nickname: string;
  profileImage: string | null;
  followerCount: number;
  followingCount: number;
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

export default function MyPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [memberData, setMemberData] = useState<MemberResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 로딩이 완료된 후에만 체크
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
      return;
    }

    if (isAuthenticated && user) {
      fetchMemberData();
    }
  }, [isAuthenticated, authLoading, user, router]);

  const fetchMemberData = async () => {
    try {
      setLoading(true);
      const response: ApiResponse<MemberResponse> = await fetchApi('/api/members/me');
      if (response.data) {
        setMemberData(response.data);
      }
    } catch (error) {
      console.error('회원 정보 조회 실패:', error);
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

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (!memberData) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">회원 정보를 불러올 수 없습니다</h2>
          <button
            onClick={fetchMemberData}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-center space-x-6">
            {/* 프로필 이미지 */}
            <div className="flex-shrink-0">
              {memberData.profileImage ? (
                <img
                  src={memberData.profileImage}
                  alt="프로필 이미지"
                  className="h-24 w-24 rounded-full object-cover border-4 border-white shadow-lg"
                />
              ) : (
                <div className="h-24 w-24 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-2xl font-bold">
                  {memberData.nickname.charAt(0).toUpperCase()}
                </div>
              )}
            </div>
            
            {/* 사용자 정보 */}
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{memberData.nickname}</h1>
              <p className="text-gray-600 mb-4">{memberData.email}</p>
              
              {/* 포인트 버튼들 */}
              <div className="mb-4">
                <div className="flex items-center space-x-3">
                  <button
                    onClick={() => router.push('/mypage/points/charge')}
                    className="px-4 py-2 bg-yellow-500 text-white text-sm rounded-lg hover:bg-yellow-600 transition-colors"
                  >
                    포인트 충전
                  </button>
                  <button
                    onClick={() => router.push('/mypage/points/history')}
                    className="px-4 py-2 bg-gray-500 text-white text-sm rounded-lg hover:bg-gray-600 transition-colors"
                  >
                    포인트 내역
                  </button>
                </div>
              </div>
              
              {/* 팔로워/팔로잉 수 */}
              <div className="flex space-x-6">
                <button
                  onClick={() => router.push('/mypage/followers')}
                  className="text-center hover:bg-gray-100 px-4 py-2 rounded-lg transition-colors"
                >
                  <div className="text-2xl font-bold text-gray-900">{memberData.followerCount}</div>
                  <div className="text-sm text-gray-500">팔로워</div>
                </button>
                <button
                  onClick={() => router.push('/mypage/following')}
                  className="text-center hover:bg-gray-100 px-4 py-2 rounded-lg transition-colors"
                >
                  <div className="text-2xl font-bold text-gray-900">{memberData.followingCount}</div>
                  <div className="text-sm text-gray-500">팔로우</div>
                </button>
              </div>
            </div>

            {/* 편집 버튼들 */}
            <div className="flex-shrink-0 flex space-x-3">
              <button
                onClick={() => router.push('/mypage/profile-image')}
                className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
              >
                프로필 이미지 변경
              </button>
              <button
                onClick={() => router.push('/mypage/edit')}
                className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
              >
                회원 정보 수정
              </button>
            </div>
          </div>
        </div>

        {/* 통계 카드들 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
          <button
            onClick={() => router.push('/mypage/posts')}
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow text-left"
          >
            <div className="flex items-center">
              <div className="p-3 bg-blue-100 rounded-lg">
                <svg className="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">작성한 게시글 & 거래글</p>
              </div>
            </div>
          </button>

          <button
            onClick={() => router.push('/mypage/comments')}
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow text-left"
          >
            <div className="flex items-center">
              <div className="p-3 bg-green-100 rounded-lg">
                <svg className="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">작성한 댓글</p>
              </div>
            </div>
          </button>

          <button
            onClick={() => router.push('/mypage/liked-posts')}
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow text-left"
          >
            <div className="flex items-center">
              <div className="p-3 bg-purple-100 rounded-lg">
                <svg className="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">좋아요한 글</p>
              </div>
            </div>
          </button>

          <button
            onClick={() => router.push('/mypage/chats')}
            className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow text-left"
          >
            <div className="flex items-center">
              <div className="p-3 bg-indigo-100 rounded-lg">
                <svg className="h-6 w-6 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-500">거래 채팅</p>
              </div>
            </div>
          </button>

        </div>

      </div>
    </div>
  );
}
