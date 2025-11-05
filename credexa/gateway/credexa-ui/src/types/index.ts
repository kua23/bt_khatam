// Customer Types
export interface Customer {
  customerId: number;
  customerName: string;
  dateOfBirth?: string;
  email?: string;
  phoneNumber?: string;
  address?: string;
  panNumber?: string;
  aadharNumber?: string;
  customerType?: string;
  kycStatus?: string;
  createdDate?: string;
  lastModifiedDate?: string;
}

export interface CreateCustomerRequest {
  customerName: string;
  dateOfBirth?: string;
  email?: string;
  phoneNumber?: string;
  address?: string;
  panNumber?: string;
  aadharNumber?: string;
  customerType?: string;
}

// Product Types
export interface Product {
  productId: number;
  productCode: string;
  productName: string;
  description?: string;
  minPrincipalAmount: number;
  maxPrincipalAmount: number;
  minTermMonths: number;
  maxTermMonths: number;
  interestRate: number;
  compoundingFrequency: string;
  isActive: boolean;
  effectiveDate?: string;
  expiryDate?: string;
  features?: string;
}

// Calculator Types
export interface CalculatorRequest {
  principalAmount: number;
  interestRate: number;
  termInMonths: number;
  compoundingFrequency: string;
}

export interface CalculatorResponse {
  principalAmount: number;
  interestRate: number;
  termInMonths: number;
  compoundingFrequency: string;
  maturityAmount: number;
  interestEarned: number;
  effectiveInterestRate: number;
  calculationDate: string;
}

// FD Account Types
export interface FdAccount {
  accountNumber: string;
  productCode: string;
  customerId: number;
  principalAmount: number;
  interestRate: number;
  termMonths: number;
  maturityAmount: number;
  accountStatus: string;
  openDate: string;
  maturityDate: string;
  branchCode?: string;
  maturityInstruction?: string;
  autoRenewal: boolean;
  compoundingFrequency: string;
  lastInterestCreditDate?: string;
  remarks?: string;
  createdDate: string;
  lastModifiedDate?: string;
  roles?: AccountRole[];
  transactions?: Transaction[];
}

export interface AccountSummary {
  accountNumber: string;
  customerName: string;
  productName: string;
  principalAmount: number;
  currentValue: number;
  interestEarned: number;
  maturityDate: string;
  accountStatus: string;
  daysToMaturity: number;
}

export interface CreateStandardAccountRequest {
  productCode: string;
  customerId: number;
  principalAmount: number;
  termMonths: number;
  branchCode?: string;
  maturityInstruction?: string;
  autoRenewal?: boolean;
  remarks?: string;
}

export interface CustomizeAccountRequest {
  productCode: string;
  customerId: number;
  principalAmount: number;
  customTermMonths: number;
  customInterestRate: number;
  branchCode?: string;
  maturityInstruction?: string;
  autoRenewal?: boolean;
  remarks?: string;
}

export interface SearchAccountParams {
  customerId?: number;
  productCode?: string;
  branchCode?: string;
  accountStatus?: string;
  fromDate?: string;
  toDate?: string;
  minPrincipalAmount?: number;
  maxPrincipalAmount?: number;
}

// Transaction Types
export interface Transaction {
  transactionId: number;
  accountNumber: string;
  transactionType: string;
  amount: number;
  transactionDate: string;
  valueDate: string;
  description?: string;
  transactionReference: string;
  balanceAfter: number;
  initiatedBy?: string;
  approvedBy?: string;
  status: string;
  reversalReference?: string;
  createdDate: string;
}

export interface CreateTransactionRequest {
  accountNumber: string;
  transactionType: string;
  amount: number;
  description?: string;
}

export interface PrematureWithdrawalInquiry {
  accountNumber: string;
  principalAmount: number;
  interestEarned: number;
  penaltyAmount: number;
  netPayable: number;
  reducedInterestRate: number;
  originalMaturityDate: string;
  withdrawalDate: string;
  remarks: string;
}

export interface PrematureWithdrawalRequest {
  accountNumber: string;
  withdrawalAmount: number;
  reason?: string;
}

// Role Types
export interface AccountRole {
  roleId: number;
  accountNumber: string;
  customerId: number;
  roleType: string;
  ownershipPercentage?: number;
  isPrimary: boolean;
  isActive: boolean;
  effectiveDate: string;
  expiryDate?: string;
  remarks?: string;
  createdDate: string;
  lastModifiedDate?: string;
}

export interface AddRoleRequest {
  customerId: number;
  roleType: string;
  ownershipPercentage?: number;
  isPrimary?: boolean;
  remarks?: string;
}

export interface UpdateRoleRequest {
  roleType?: string;
  ownershipPercentage?: number;
  isPrimary?: boolean;
  remarks?: string;
}

// Auth/Login Types
export interface LoginRequest {
  usernameOrEmailOrMobile: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
  mobileNumber: string;
  roles: string[];
  preferredLanguage: string;
  preferredCurrency: string;
  loginTime: string;
  expiresIn: number;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  mobileNumber?: string;
  preferredLanguage?: string;
  preferredCurrency?: string;
}

export interface TokenValidationResponse {
  valid: boolean;
  username?: string;
  userId?: number;
  roles?: string[];
}

export interface BankConfigResponse {
  bankName: string;
  bankLogo?: string;
  defaultLanguage: string;
  defaultCurrency: string;
  supportedLanguages: string[];
  supportedCurrencies: string[];
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
}
