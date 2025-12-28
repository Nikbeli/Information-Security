export interface UserCreate {
  email: string;
}

export interface UserGet {
  id: string;
  email: string;
  role: string;
  accountLocked: boolean;
  passwordRestrictions: boolean;
  minPasswordLength: number;
  passwordExpirationMonths: number;
  passwordLastChanged: string;
  firstLogin: boolean;
}

export interface UserUpdate {
  email?: string;
  role?: string;
  password_hash?: string;
  salt?: string;
  accountLocked?: boolean;
  passwordRestrictions?: boolean;
  passwordExpirationMonths?: number;
  passwordLastChanged?: string;
  minPasswordLength?: number;
  failedAttempts?: number;
  firstLogin?: boolean;
  emailConfirmed?: boolean;
}