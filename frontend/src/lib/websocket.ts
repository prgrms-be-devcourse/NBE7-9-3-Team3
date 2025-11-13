import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';
import { api } from './client';

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

interface ChatMessage {
  messageId: number;
  senderId: number;
  senderNickname: string;
  content: string;
  sendDate: string;
}

type MessageCallback = (message: ChatMessage) => void;

class WebSocketClient {
  private client: Client | null = null;
  private token: string | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;

  /**
   * 웹소켓 토큰 발급 및 연결 초기화
   */
  async connect(): Promise<void> {
    try {
      // 1. 웹소켓 토큰 발급
      const response = await api.get<ApiResponse<string>>('/api/chat/token');
      this.token = response.data;

      // 2. SockJS 연결
      const socket = new SockJS(`${process.env.NEXT_PUBLIC_API_BASE_URL}/ws`);

      // 3. 연결 완료를 기다리기 위한 Promise 생성
      return new Promise((resolve, reject) => {
        // 4. STOMP 클라이언트 설정
        this.client = new Client({
          webSocketFactory: () => socket as any,
          connectHeaders: {
            Authorization: `Bearer ${this.token}`,
          },
          debug: (str) => {
            console.log('[STOMP]', str);
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: () => {
            console.log('✅ 웹소켓 연결 성공');
            this.reconnectAttempts = 0;
            resolve(); // 연결 완료를 알림
          },
          onStompError: (frame) => {
            console.error('❌ STOMP 에러:', frame.headers['message']);
            console.error('상세:', frame.body);
            reject(new Error(frame.headers['message'] || 'STOMP 연결 실패'));
          },
          onWebSocketClose: () => {
            console.log('웹소켓 연결 종료');
            this.handleReconnect();
          },
        });

        // 5. 연결 활성화
        this.client.activate();
      });
    } catch (error) {
      console.error('웹소켓 연결 실패:', error);
      throw error;
    }
  }

  /**
   * 재연결 처리
   */
  private async handleReconnect(): Promise<void> {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}...`);
      
      setTimeout(async () => {
        try {
          await this.connect();
        } catch (error) {
          console.error('재연결 실패:', error);
        }
      }, 3000);
    } else {
      console.error('최대 재연결 시도 횟수 초과');
    }
  }

  /**
   * 특정 채팅방 구독
   */
  subscribe(roomId: number, callback: MessageCallback): (() => void) | null {
    console.log(`[채팅방 ${roomId}] 구독 시도 - 연결 상태: ${this.client?.connected}`);
    
    if (!this.client?.connected) {
      console.error('❌ 웹소켓이 연결되지 않았습니다.');
      return null;
    }

    console.log(`✅ [채팅방 ${roomId}] 구독 시작`);
    const subscription = this.client.subscribe(`/receive/${roomId}`, (message: IMessage) => {
      try {
        const chatMessage: ChatMessage = JSON.parse(message.body);
        console.log(`📨 [채팅방 ${roomId}] 메시지 수신:`, chatMessage);
        callback(chatMessage);
      } catch (error) {
        console.error('메시지 파싱 실패:', error);
      }
    });

    // 구독 취소 함수 반환
    return () => {
      console.log(`🔌 [채팅방 ${roomId}] 구독 취소`);
      subscription.unsubscribe();
    };
  }

  /**
   * 메시지 전송 (senderId와 content 전송)
   */
  sendMessage(roomId: number, content: string, senderId: number): void {
    if (!this.client?.connected) {
      console.error('❌ 웹소켓이 연결되지 않았습니다.');
      return;
    }

    console.log(`📤 [채팅방 ${roomId}] 메시지 전송:`, { senderId, content });
    this.client.publish({
      destination: `/send/${roomId}`,
      body: JSON.stringify({ 
        senderId: senderId,
        content: content 
      }),
    });
  }

  /**
   * 연결 종료
   */
  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.token = null;
      console.log('웹소켓 연결 종료됨');
    }
  }

  /**
   * 연결 상태 확인
   */
  isConnected(): boolean {
    return this.client?.connected ?? false;
  }
}

// 싱글톤 인스턴스
let wsClient: WebSocketClient | null = null;

/**
 * 웹소켓 클라이언트 싱글톤 인스턴스 가져오기
 */
export function getWebSocketClient(): WebSocketClient {
  if (!wsClient) {
    wsClient = new WebSocketClient();
  }
  return wsClient;
}

export type { ChatMessage, MessageCallback };

