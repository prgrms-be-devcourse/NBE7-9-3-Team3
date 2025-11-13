'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { api } from '@/lib/client';
import { getWebSocketClient, ChatMessage } from '@/lib/websocket';
import { TradeChatMessage } from '@/type/chat';

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

interface User {
  memberId: number;
  nickname: string;
}

interface ChatRoomDetail {
  roomId: number;
  tradeId: number;
  tradeTitle: string;
  boardType: 'FISH' | 'SECONDHAND';
  sellerId: number;
  sellerNickname: string;
  buyerId: number;
  buyerNickname: string;
  createDate: string;
  status: string;
}

export default function ChatRoomPage() {
  const router = useRouter();
  const params = useParams();
  const roomId = Number(params.roomId);

  const [messages, setMessages] = useState<TradeChatMessage[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [wsConnected, setWsConnected] = useState(false);
  const [chatRoomInfo, setChatRoomInfo] = useState<ChatRoomDetail | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const wsClient = useRef(getWebSocketClient());
  const unsubscribeRef = useRef<(() => void) | null>(null);

  // 현재 로그인한 사용자 정보 가져오기
  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        const response = await api.get<ApiResponse<User>>('/api/members/me');
        setCurrentUser(response.data);
      } catch (err) {
        console.error('사용자 정보 조회 실패:', err);
      }
    };
    fetchCurrentUser();
  }, []);

  // 웹소켓 연결 및 메시지 구독
  useEffect(() => {
    let mounted = true;

    const initWebSocket = async () => {
      try {
        // 웹소켓이 연결되지 않았다면 연결
        if (!wsClient.current.isConnected()) {
          await wsClient.current.connect();
        }
        
        if (mounted) {
          setWsConnected(true);
          
          // 메시지 수신 구독
          const unsubscribe = wsClient.current.subscribe(roomId, (message: ChatMessage) => {
            if (mounted) {
              setMessages(prev => [...prev, message as TradeChatMessage]);
            }
          });
          
          unsubscribeRef.current = unsubscribe;
        }
      } catch (err) {
        console.error('웹소켓 연결 실패:', err);
        if (mounted) {
          setError('실시간 채팅 연결에 실패했습니다.');
        }
      }
    };

    initWebSocket();

    return () => {
      mounted = false;
      if (unsubscribeRef.current) {
        unsubscribeRef.current();
      }
    };
  }, [roomId]);

  // 채팅방 정보 조회
  useEffect(() => {
    const loadChatRoomInfo = async () => {
      try {
        const response = await api.get<ApiResponse<ChatRoomDetail>>(
          `/api/chat/rooms/${roomId}`
        );
        setChatRoomInfo(response.data);
      } catch (err) {
        console.error('채팅방 정보 조회 실패:', err);
      }
    };
    loadChatRoomInfo();
  }, [roomId]);

  // 이전 메시지 불러오기
  useEffect(() => {
    const loadMessages = async () => {
      try {
        setLoading(true);
        const response = await api.get<ApiResponse<TradeChatMessage[]>>(
          `/api/chat/rooms/messages/${roomId}`
        );
        setMessages(response.data);
      } catch (err) {
        setError(err instanceof Error ? err.message : '메시지를 불러오는데 실패했습니다.');
      } finally {
        setLoading(false);
      }
    };
    loadMessages();
  }, [roomId]);

  // 메시지가 추가될 때마다 스크롤을 아래로
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = () => {
    if (!inputMessage.trim() || !wsConnected || !currentUser) return;

    wsClient.current.sendMessage(roomId, inputMessage, currentUser.memberId);
    setInputMessage('');
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // 날짜별로 메시지 그룹화
  const groupMessagesByDate = (messages: TradeChatMessage[]) => {
    const groups: { [key: string]: TradeChatMessage[] } = {};
    
    messages.forEach(msg => {
      const date = new Date(msg.sendDate).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      });
      
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(msg);
    });
    
    return groups;
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // 게시글로 이동
  const goToTradePage = () => {
    if (!chatRoomInfo) return;
    
    const path = chatRoomInfo.boardType === 'FISH' 
      ? `/market/fish/${chatRoomInfo.tradeId}`
      : `/market/secondhand/${chatRoomInfo.tradeId}`;
    
    router.push(path);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50">
        <div className="text-gray-500">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50">
        <div className="text-center">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 mb-4">
            {error}
          </div>
          <button
            onClick={() => router.push('/mypage/chats')}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            채팅 목록으로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  const messageGroups = groupMessagesByDate(messages);

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      {/* 헤더 */}
      <div className="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button
            onClick={() => router.push('/mypage/chats')}
            className="px-4 py-2 bg-sky-500 text-white rounded-lg hover:bg-sky-600 transition-colors font-medium"
          >
            ← 뒤로
          </button>
          {chatRoomInfo && (
            <button
              onClick={goToTradePage}
              className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors font-medium"
            >
              게시글 보기
            </button>
          )}
          <div>
            <h1 className="font-semibold text-gray-900">채팅</h1>
            <div className="flex items-center gap-1 text-xs">
              <div className={`w-2 h-2 rounded-full ${wsConnected ? 'bg-green-500' : 'bg-gray-400'}`} />
              <span className="text-gray-600">
                {wsConnected ? '연결됨' : '연결 중...'}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* 메시지 영역 */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {Object.keys(messageGroups).length === 0 ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-center text-gray-500">
              <div className="text-4xl mb-2">💬</div>
              <p>첫 메시지를 보내보세요!</p>
            </div>
          </div>
        ) : (
          Object.entries(messageGroups).map(([date, msgs]) => (
            <div key={date}>
              {/* 날짜 구분선 */}
              <div className="flex items-center justify-center my-4">
                <div className="bg-gray-200 text-gray-600 text-xs px-3 py-1 rounded-full">
                  {date}
                </div>
              </div>

              {/* 메시지들 */}
              {msgs.map((msg) => {
                const isMyMessage = currentUser?.memberId === msg.senderId;
                
                return (
                  <div
                    key={msg.messageId}
                    className={`flex ${isMyMessage ? 'justify-end' : 'justify-start'} mb-3`}
                  >
                    <div className={`max-w-[70%] ${isMyMessage ? 'items-end' : 'items-start'} flex flex-col`}>
                      {!isMyMessage && (
                        <div className="text-xs text-gray-600 mb-1 px-1">
                          {msg.senderNickname}
                        </div>
                      )}
                      <div className="flex items-end gap-2">
                        {isMyMessage && (
                          <span className="text-xs text-gray-500 mb-1">
                            {formatTime(msg.sendDate)}
                          </span>
                        )}
                        <div
                          className={`rounded-2xl px-4 py-2 ${
                            isMyMessage
                              ? 'bg-blue-600 text-white'
                              : 'bg-white text-gray-900 border border-gray-200'
                          }`}
                        >
                          <p className="whitespace-pre-wrap break-words">{msg.content}</p>
                        </div>
                        {!isMyMessage && (
                          <span className="text-xs text-gray-500 mb-1">
                            {formatTime(msg.sendDate)}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* 입력 영역 */}
      <div className="bg-white border-t border-gray-200 p-4">
        <div className="flex gap-2">
          <textarea
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="메시지를 입력하세요..."
            disabled={!wsConnected}
            className="flex-1 resize-none border border-gray-300 rounded-lg px-4 py-2 text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed"
            rows={1}
            style={{ maxHeight: '100px' }}
          />
          <button
            onClick={sendMessage}
            disabled={!inputMessage.trim() || !wsConnected}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors font-medium"
          >
            전송
          </button>
        </div>
      </div>
    </div>
  );
}

