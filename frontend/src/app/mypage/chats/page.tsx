'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/client';
import { TradeChatRoom } from '@/type/chat';

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

export default function ChatsPage() {
  const router = useRouter();
  const [chatRooms, setChatRooms] = useState<TradeChatRoom[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadChatRooms();
  }, []);

  const loadChatRooms = async () => {
    try {
      setLoading(true);
      const response = await api.get<ApiResponse<TradeChatRoom[]>>('/api/chat/rooms/me');
      setChatRooms(response.data);
    } catch (err) {
      setError(err instanceof Error ? err.message : '채팅방 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const diffDays = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    } else if (diffDays === 1) {
      return '어제';
    } else if (diffDays < 7) {
      return `${diffDays}일 전`;
    } else {
      return date.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-4xl mx-auto">
          <div className="flex items-center justify-center h-64">
            <div className="text-gray-500">로딩 중...</div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-4xl mx-auto">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
            {error}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-4xl mx-auto">
        {/* 헤더 */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">거래 채팅</h1>
          <p className="text-gray-600 mt-1">진행 중인 채팅방 목록입니다</p>
        </div>

        {/* 채팅방 목록 */}
        {chatRooms.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <div className="text-gray-400 text-lg mb-2">💬</div>
            <p className="text-gray-600">진행 중인 채팅이 없습니다</p>
            <p className="text-sm text-gray-500 mt-1">거래 게시글에서 채팅을 시작해보세요</p>
          </div>
        ) : (
          <div className="space-y-2">
            {chatRooms.map((room) => (
              <div
                key={room.roomId}
                onClick={() => router.push(`/mypage/chats/${room.roomId}`)}
                className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer p-4 border border-gray-100"
              >
                <div className="flex justify-between items-start mb-2">
                  <div className="flex-1">
                    <h3 className="font-semibold text-gray-900 mb-1">
                      {room.tradeTitle}
                    </h3>
                    <div className="flex items-center gap-3 text-sm">
                      <div className="flex items-center gap-1">
                        <span className="text-blue-600 font-medium">판매자:</span>
                        <span className="text-gray-700">{room.sellerNickname}</span>
                      </div>
                      <span className="text-gray-300">|</span>
                      <div className="flex items-center gap-1">
                        <span className="text-green-600 font-medium">구매자:</span>
                        <span className="text-gray-700">{room.buyerNickname}</span>
                      </div>
                    </div>
                  </div>
                  <div className="flex flex-col items-end gap-1">
                    <span className="text-xs text-gray-500">
                      {formatDate(room.createDate)}
                    </span>
                    {room.status === 'ONGOING' ? (
                      <span className="px-2 py-0.5 bg-green-100 text-green-700 text-xs rounded-full">
                        진행중
                      </span>
                    ) : (
                      <span className="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded-full">
                        종료
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 뒤로가기 */}
        <div className="mt-6">
          <button
            onClick={() => router.push('/mypage')}
            className="w-full py-3 bg-gray-100 hover:bg-gray-200 rounded-lg text-gray-700 font-medium transition-colors"
          >
            마이페이지로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
}

