'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';
import { uploadImage } from '@/lib/uploadImage';

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

export default function ProfileImagePage() {
  const { user, isAuthenticated, loading: authLoading, refreshUser } = useAuth();
  const router = useRouter();
  const [memberData, setMemberData] = useState<MemberData | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [selectedImage, setSelectedImage] = useState<File | null>(null);
  const [previewImage, setPreviewImage] = useState<string | null>(null);

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
        setPreviewImage(response.data.profileImage);
      }
    } catch (error) {
      console.error('회원 정보 조회 실패:', error);
      setError('회원 정보를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedImage(file);
      
      // 미리보기 생성
      const reader = new FileReader();
      reader.onload = (e) => {
        setPreviewImage(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleImageUpdate = async () => {
    if (!selectedImage) {
      setError('새로운 프로필 이미지를 선택해주세요.');
      return;
    }

    try {
      setSaving(true);
      setError(null);
      setSuccess(null);

      // 1. S3에 이미지 업로드
      const profileImageUrl = await uploadImage(selectedImage, 'profile');

      // 2. JSON으로 데이터 전송
      const result = await fetchApi('/api/members/me/profile-image', {
        method: 'PUT',
        body: JSON.stringify({ profileImageUrl }),
      });

      console.log('프로필 이미지 API 응답:', result);

      if (result.resultCode === 'SUCCESS' || result.resultCode === '200') {
        setSuccess('프로필 이미지가 성공적으로 업데이트되었습니다.');
        // AuthContext의 사용자 정보도 새로고침 (navbar 업데이트용)
        await refreshUser();

        // 2초 후 마이페이지로 리다이렉트
        setTimeout(() => {
          router.push('/mypage');
        }, 2000);
      } else {
        setError(result.msg || '프로필 이미지 업데이트에 실패했습니다.');
      }
    } catch (error) {
      console.error('프로필 이미지 업데이트 실패:', error);
      setError('프로필 이미지 업데이트 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  const handleImageRemove = async () => {
    if (!confirm('프로필 이미지를 삭제하시겠습니까?')) {
      return;
    }

    try {
      setSaving(true);
      setError(null);
      setSuccess(null);

      // null 또는 빈 문자열을 전송하여 이미지 삭제
      const result = await fetchApi('/api/members/me/profile-image', {
        method: 'PUT',
        body: JSON.stringify({ profileImageUrl: '' }),
      });

      if (result.resultCode === 'SUCCESS' || result.resultCode === '200') {
        setSuccess('프로필 이미지가 삭제되었습니다.');
        setPreviewImage(null);
        setSelectedImage(null);
        await refreshUser();

        setTimeout(() => {
          router.push('/mypage');
        }, 2000);
      } else {
        setError(result.msg || '프로필 이미지 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error('프로필 이미지 삭제 실패:', error);
      setError('프로필 이미지 삭제 중 오류가 발생했습니다.');
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
              <h1 className="text-2xl font-bold text-gray-900">프로필 이미지 수정</h1>
              <p className="text-gray-600">프로필 이미지를 변경하거나 삭제할 수 있습니다.</p>
            </div>
          </div>
        </div>

        {/* 프로필 이미지 수정 폼 */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="space-y-6">
            {/* 현재 프로필 이미지 */}
            <div className="text-center">
              <h3 className="text-lg font-medium text-gray-900 mb-4">현재 프로필 이미지</h3>
              <div className="flex justify-center">
                {previewImage ? (
                  <img
                    src={previewImage}
                    alt="현재 프로필 이미지"
                    className="h-32 w-32 rounded-full object-cover border-4 border-white shadow-lg"
                  />
                ) : (
                  <div className="h-32 w-32 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-4xl font-bold">
                    {memberData.nickname.charAt(0).toUpperCase()}
                  </div>
                )}
              </div>
            </div>

            {/* 이미지 선택 */}
            <div>
              <label htmlFor="profileImage" className="block text-sm font-medium text-gray-700 mb-2">
                새로운 프로필 이미지 선택
              </label>
              <input
                type="file"
                id="profileImage"
                name="profileImage"
                accept="image/*"
                onChange={handleImageChange}
                className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
              />
              <p className="text-xs text-gray-500 mt-1">JPG, PNG 파일만 업로드 가능합니다. (최대 5MB)</p>
            </div>

            {/* 미리보기 */}
            {selectedImage && previewImage && (
              <div className="text-center">
                <h3 className="text-lg font-medium text-gray-900 mb-4">미리보기</h3>
                <div className="flex justify-center">
                  <img
                    src={previewImage}
                    alt="미리보기"
                    className="h-32 w-32 rounded-full object-cover border-4 border-blue-200 shadow-lg"
                  />
                </div>
              </div>
            )}

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
              
              {selectedImage && (
                <button
                  type="button"
                  onClick={handleImageUpdate}
                  disabled={saving}
                  className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? '업로드 중...' : '이미지 업데이트'}
                </button>
              )}
              
              {memberData.profileImage && (
                <button
                  type="button"
                  onClick={handleImageRemove}
                  disabled={saving}
                  className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {saving ? '삭제 중...' : '이미지 삭제'}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
