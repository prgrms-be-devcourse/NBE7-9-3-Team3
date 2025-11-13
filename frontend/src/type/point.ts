export enum TransactionType {
  CHARGE = 'CHARGE',     // 포인트 충전
  PURCHASE = 'PURCHASE', // 구매
  SALE = 'SALE'          // 판매
}

export interface PointHistory {
  type: TransactionType;
  date: string;
  points: number;
  afterPoint: number;
}

export interface PurchaseRequest {
  sellerId: number;
  amount: number;
}

export interface PointChargeForm {
  amount: number;
}
