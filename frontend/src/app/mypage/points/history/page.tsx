'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/client';
import { PointHistory, TransactionType } from '@/type/point';

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T | null;
}

export default function PointHistoryPage() {
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const router = useRouter();
  const [pointHistory, setPointHistory] = useState<PointHistory[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
      return;
    }

    if (isAuthenticated && user) {
      fetchPointHistory();
    }
  }, [isAuthenticated, authLoading, user, router]);

  const fetchPointHistory = async () => {
    try {
      setLoading(true);
      const response: ApiResponse<PointHistory[]> = await fetchApi(`/api/points/members/${user!.memberId}/history`);
      if (response.data) {
        setPointHistory(response.data);
      }
    } catch (error) {
      console.error('포인트 내역 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const getTransactionTypeText = (type: TransactionType) => {
    switch (type) {
      case TransactionType.CHARGE:
        return '충전';
      case TransactionType.PURCHASE:
        return '구매';
      case TransactionType.SALE:
        return '판매';
      default:
        return '알 수 없음';
    }
  };

  const getTransactionTypeColor = (type: TransactionType) => {
    switch (type) {
      case TransactionType.CHARGE:
        return 'text-green-600 bg-green-100';
      case TransactionType.PURCHASE:
        return 'text-red-600 bg-red-100';
      case TransactionType.SALE:
        return 'text-blue-600 bg-blue-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
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
            <div>
              <h1 className="text-3xl font-bold text-gray-900">포인트 내역</h1>
              <p className="text-gray-600 mt-2">포인트 충전 및 사용 내역을 확인하세요</p>
            </div>
            <button
              onClick={() => router.push('/mypage/points/charge')}
              className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
            >
              포인트 충전
            </button>
          </div>
        </div>

        {/* 포인트 내역 목록 */}
        <div className="bg-white rounded-lg shadow-sm">
          {pointHistory.length === 0 ? (
            <div className="text-center py-12">
              <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
              </svg>
              <h3 className="mt-2 text-sm font-medium text-gray-900">포인트 내역이 없습니다</h3>
              <p className="mt-1 text-sm text-gray-500">포인트를 충전하거나 사용해보세요.</p>
              <div className="mt-6">
                <button
                  onClick={() => router.push('/mypage/points/charge')}
                  className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                >
                  포인트 충전하기
                </button>
              </div>
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {pointHistory.map((history, index) => (
                <div key={index} className="p-6 hover:bg-gray-50 transition-colors">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className={`px-3 py-1 rounded-full text-sm font-medium ${getTransactionTypeColor(history.type)}`}>
                        {getTransactionTypeText(history.type)}
                      </div>
                      <div>
                        <p className="text-sm text-gray-900">
                          {history.type === TransactionType.CHARGE || history.type === TransactionType.SALE 
                            ? '+' 
                            : '-'}{history.points.toLocaleString()} 포인트
                        </p>
                        <p className="text-xs text-gray-500">{formatDate(history.date)}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-gray-900">
                        잔액: {history.afterPoint.toLocaleString()} 포인트
                      </p>
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
