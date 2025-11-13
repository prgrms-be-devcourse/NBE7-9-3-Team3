export interface TradeChatRoom {
  roomId: number;
  tradeId: number;
  tradeTitle: string;
  sellerId: number;
  sellerNickname: string;
  buyerId: number;
  buyerNickname: string;
  status: 'ONGOING' | 'CLOSED';
  createDate: string;
}

export interface TradeChatMessage {
  messageId: number;
  senderId: number;
  senderNickname: string;
  content: string;
  sendDate: string;
}

