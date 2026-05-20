export type AccountType = "REAL" | "VIRTUAL";
export type AccountStatus = "ACTIVE" | "SUSPENDED" | "CLOSED";

export interface Account {
  id: number;
  accountNumber: string;
  accountName: string;
  accountType: AccountType;
  currency: string;
  /** 현금 잔고 */
  balance: number;
  /** 출금 가능 잔고 */
  availableBalance: number;
  status: AccountStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface AccountSearchRequest {
  accountNumber?: string;
  accountType?: AccountType;
  status?: AccountStatus;
  currency?: string;
}

export interface AccountCreateRequest {
  accountName: string;
  accountType: AccountType;
  currency: string;
}

export interface AccountUpdateRequest {
  accountName: string;
}

export interface MoneyMoveRequest {
  amount: number;
  referenceId: string;
}
