'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';

interface MemberData {
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

export default function EditPage() {
  const { user, isAuthenticated, loading: authLoading, refreshUser } = useAuth();
  const router = useRouter();
  const [memberData, setMemberData] = useState<MemberData | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // 폼 데이터
  const [formData, setFormData] = useState({
    email: '',
    currentPassword: '',
    newPassword: '',
    nickname: ''
  });

  useEffect(() => {
    if (!authLoading && (!isAuthenticated || !user)) {
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
      const response: ApiResponse<MemberData> = await fetchApi('/api/members/me');
      if (response.data) {
        setMemberData(response.data);
        setFormData({
          email: response.data.email,
          currentPassword: '',
          newPassword: '',
          nickname: response.data.nickname
        });
      }
    } catch (error) {
      console.error('회원 정보 조회 실패:', error);
      setError('회원 정보를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };


  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.currentPassword) {
      setError('현재 비밀번호를 입력해주세요.');
      return;
    }

    if (!formData.newPassword) {
      setError('새 비밀번호를 입력해주세요.');
      return;
    }

    if (formData.newPassword.length < 8 || formData.newPassword.length > 20) {
      setError('새 비밀번호는 8-20자 사이여야 합니다.');
      return;
    }

    try {
      setSaving(true);
      setError(null);
      setSuccess(null);

      const result = await fetchApi('/api/members/me', {
        method: 'PUT',
        body: JSON.stringify({
          email: formData.email,
          currentPassword: formData.currentPassword,
          newPassword: formData.newPassword,
          nickname: formData.nickname,
          profileImageUrl: null // 프로필 이미지는 별도 페이지에서 관리
        }),
      });

      console.log('API 응답:', result);

      if (result.resultCode === '200') {
        setSuccess('회원정보가 성공적으로 수정되었습니다.');
        // 폼 초기화
        setFormData(prev => ({
          ...prev,
          currentPassword: '',
          newPassword: ''
        }));
        // 회원 정보 다시 불러오기
        await fetchMemberData();
        // AuthContext의 사용자 정보도 새로고침 (navbar 업데이트용)
        await refreshUser();
        
        // 2초 후 마이페이지로 리다이렉트
        setTimeout(() => {
          router.push('/mypage');
        }, 1000);
      } else {
        setError(result.msg || '회원정보 수정에 실패했습니다.');
      }
    } catch (error) {
      console.error('회원정보 수정 실패:', error);
      setError('회원정보 수정 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  if (authLoading || loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (!isAuthenticated || !memberData) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
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
              <h1 className="text-2xl font-bold text-gray-900">프로필 편집</h1>
              <p className="text-gray-600">회원정보를 수정할 수 있습니다.</p>
            </div>
          </div>
        </div>

        {/* 편집 폼 */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 프로필 이미지 안내 */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div className="flex items-center">
                <svg className="h-5 w-5 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div className="ml-3">
                  <p className="text-sm text-blue-800">
                    프로필 이미지는 <button 
                      onClick={() => router.push('/mypage/profile-image')}
                      className="text-blue-600 hover:text-blue-800 underline font-medium"
                    >
                      프로필 이미지 수정 페이지
                    </button>에서 변경할 수 있습니다.
                  </p>
                </div>
              </div>
            </div>

            {/* 이메일 */}
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                이메일
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            {/* 닉네임 */}
            <div>
              <label htmlFor="nickname" className="block text-sm font-medium text-gray-700 mb-2">
                닉네임
              </label>
              <input
                type="text"
                id="nickname"
                name="nickname"
                value={formData.nickname}
                onChange={handleInputChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            {/* 현재 비밀번호 */}
            <div>
              <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 mb-2">
                현재 비밀번호
              </label>
              <input
                type="password"
                id="currentPassword"
                name="currentPassword"
                value={formData.currentPassword}
                onChange={handleInputChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            {/* 새 비밀번호 */}
            <div>
              <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-2">
                새 비밀번호
              </label>
              <input
                type="password"
                id="newPassword"
                name="newPassword"
                value={formData.newPassword}
                onChange={handleInputChange}
                required
                minLength={8}
                maxLength={20}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <p className="text-xs text-gray-500 mt-1">8-20자 사이로 입력해주세요.</p>
            </div>

            {/* 오류/성공 메시지 */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <div className="flex">
                  <svg className="h-5 w-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                  </svg>
                  <div className="ml-3">
                    <p className="text-sm text-red-800">{error}</p>
                  </div>
                </div>
              </div>
            )}

            {success && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <svg className="h-6 w-6 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div className="ml-3">
                    <h3 className="text-sm font-medium text-blue-800">성공!</h3>
                    <p className="text-sm text-blue-700 mt-1">{success}</p>
                    <p className="text-xs text-blue-600 mt-1">잠시 후 마이페이지로 이동합니다...</p>
                  </div>
                </div>
              </div>
            )}

            {/* 버튼들 */}
            <div className="flex space-x-4 pt-6">
              <button
                type="button"
                onClick={() => router.back()}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
              >
                취소
              </button>
              <button
                type="submit"
                disabled={saving}
                className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {saving ? '저장 중...' : '저장'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
