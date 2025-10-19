import type { UserGet } from "./user";

export interface UserLogin {
  email: string;
  password: string;
}

export interface LoginResult {
  email: string;
  requiresOtp: boolean; // точно как с бекенда
  token?: string | null; // можно оставить, если токен может прийти
}

export interface OtpVerifyRequest {
  email: string;
  otp: string;
}

export interface AuthResponse {
  token: string;
  user: UserGet;
  requiresOtp?: boolean;
}

export interface OtpResponse {
  message: string;
  otpSent: boolean;
}