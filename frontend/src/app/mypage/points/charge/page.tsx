'use client';

import React, { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';
import { PointChargeForm } from '@/type/point';

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

export default function PointChargePage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [formData, setFormData] = useState<PointChargeForm>({ amount: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const predefinedAmounts = [1000, 5000, 10000, 20000, 50000];

  const handleAmountChange = (amount: number) => {
    setFormData({ amount });
    setError(null);
    setSuccessMessage(null);
  };

  const handleCustomAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = parseInt(e.target.value) || 0;
    setFormData({ amount: value });
    setError(null);
    setSuccessMessage(null);
  };

  const handleCharge = async () => {
    if (!user || formData.amount <= 0) {
      setError('충전할 포인트를 입력해주세요.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      setSuccessMessage(null);

      // 백엔드 API에 맞춰서 amount를 path variable로 전달
      await fetchApi(`/api/points/members/charge/${formData.amount}`, {
        method: 'POST'
      });

      setSuccessMessage(`포인트 ${formData.amount.toLocaleString()}원 충전 완료되었습니다.`);
      setFormData({ amount: 0 });
    } catch (error) {
      console.error('포인트 충전 실패:', error);
      setError('포인트 충전에 실패했습니다. 다시 시도해주세요.');
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
    router.push('/login');
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">포인트 충전</h1>
              <p className="text-gray-600 mt-2">포인트를 충전하여 다양한 서비스를 이용하세요</p>
            </div>
            <button
              onClick={() => router.push('/mypage/points/history')}
              className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
            >
              내역 보기
            </button>
          </div>
        </div>

        {/* 충전 폼 */}
        <div className="bg-white rounded-lg shadow-sm p-6">
          <div className="space-y-6">
            {/* 빠른 충전 버튼들 */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">빠른 충전</h3>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                {predefinedAmounts.map((amount) => (
                  <button
                    key={amount}
                    onClick={() => handleAmountChange(amount)}
                    className={`p-4 rounded-lg border-2 transition-colors ${
                      formData.amount === amount
                        ? 'border-blue-500 bg-blue-50 text-blue-700'
                        : 'border-gray-200 hover:border-gray-300 text-gray-700'
                    }`}
                  >
                    <div className="text-lg font-semibold">{amount.toLocaleString()}원</div>
                    <div className="text-sm text-gray-500">{amount.toLocaleString()} 포인트</div>
                  </button>
                ))}
              </div>
            </div>

            {/* 직접 입력 */}
            <div>
              <h3 className="text-lg font-medium text-gray-900 mb-4">직접 입력</h3>
              <div className="relative">
                <input
                  type="number"
                  value={formData.amount || ''}
                  onChange={handleCustomAmountChange}
                  placeholder="충전할 금액을 입력하세요"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  min="1"
                />
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500">
                  원
                </div>
              </div>
            </div>

            {/* 충전 요약 */}
            {formData.amount > 0 && (
              <div className="bg-blue-50 rounded-lg p-4">
                <div className="flex justify-between items-center">
                  <span className="text-gray-700">충전 포인트:</span>
                  <span className="text-xl font-bold text-blue-600">
                    {formData.amount.toLocaleString()} 포인트
                  </span>
                </div>
                <div className="flex justify-between items-center mt-2">
                  <span className="text-gray-700">충전 금액:</span>
                  <span className="text-lg font-semibold text-gray-900">
                    {formData.amount.toLocaleString()}원
                  </span>
                </div>
              </div>
            )}

            {/* 성공 메시지 */}
            {successMessage && (
              <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                <div className="flex items-center">
                  <svg className="h-5 w-5 text-green-400 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                  <p className="text-green-600 font-medium">{successMessage}</p>
                </div>
              </div>
            )}

            {/* 에러 메시지 */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                <p className="text-red-600">{error}</p>
              </div>
            )}

            {/* 충전 버튼 */}
            <button
              onClick={handleCharge}
              disabled={loading || formData.amount <= 0}
              className={`w-full py-3 px-4 rounded-lg font-medium transition-colors ${
                loading || formData.amount <= 0
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-blue-500 text-white hover:bg-blue-600'
              }`}
            >
              {loading ? '충전 중...' : `${formData.amount.toLocaleString()}원 충전하기`}
            </button>
          </div>
        </div>

        {/* 안내사항 */}
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mt-6">
          <h4 className="font-medium text-yellow-800 mb-2">안내사항</h4>
          <ul className="text-sm text-yellow-700 space-y-1">
            <li>• 충전된 포인트는 즉시 사용 가능합니다.</li>
            <li>• 포인트 충전 내역은 포인트 내역에서 확인할 수 있습니다.</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
