import React, { useState } from 'react';
import { authService } from '../api/clients';
import type { UserLogin, OtpVerifyRequest, LoginResult } from '../types/auth';

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [loginResult, setLoginResult] = useState<LoginResult | null>(null);
  const [error, setError] = useState<string | null>(null);

  // === Универсальный обработчик ошибок от Spring AdviceController ===
  const extractErrorMessage = (err: any): string => {
    if (!err) return 'Неизвестная ошибка';

    const backendError = err.response?.data ?? err.message ?? err.toString();

    if (typeof backendError === 'string') return backendError;

    if (backendError.message) return backendError.message;

    if (backendError.error) return backendError.error;

    if (backendError.cause && backendError.cause.className)
      return `Ошибка: ${backendError.cause.className} → ${backendError.cause.method}`;

    try {
      return JSON.stringify(backendError);
    } catch {
      return 'Не удалось обработать ошибку';
    }
  };

  // === Авторизация по email + password ===
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      const result = await authService.login({ email, password } as UserLogin);
      console.log('Login response:', result);

      setLoginResult({
        email: result.email,
        requiresOtp: result.requiresOtp,
        token: result.token ?? null,
      });

      // если OTP не требуется → сразу заходим
      if (!result.requiresOtp && result.token) {
        localStorage.setItem('authToken', result.token);
        localStorage.setItem('user', JSON.stringify({ email: result.email }));
        window.location.href = '/usermanager';
      }
    } catch (err: any) {
      console.error('Login error:', err);
      setError(extractErrorMessage(err));
    }
  };

  // === Проверка OTP ===
  const handleVerifyOtp = async () => {
    if (!loginResult) return;
    setError(null);

    const request: OtpVerifyRequest = { email: loginResult.email, otp };

    try {
      const response = await authService.getToken(request);
      const token = response.token;

      if (!token) throw new Error('Token not received');

      localStorage.setItem('authToken', token);
      localStorage.setItem('user', JSON.stringify({ email: loginResult.email }));
      window.location.href = '/changepassword';
    } catch (err: any) {
      console.error('OTP error:', err);
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div style={{ maxWidth: 400, margin: '50px auto', textAlign: 'center' }}>
      <h2>Login</h2>

      {/* === Форма email + password === */}
      {!loginResult?.requiresOtp && (
        <>
          <form onSubmit={handleLogin}>
            <input
              type="email"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={{ marginBottom: 8, width: '100%', padding: 6 }}
            />
            <br />
            <input
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              style={{ marginBottom: 8, width: '100%', padding: 6 }}
            />
            <br />
            <button type="submit" style={{ padding: 8, width: '100%' }}>
              Login
            </button>
          </form>
        </>
      )}

      {/* === Форма для OTP === */}
      {loginResult?.requiresOtp && (
        <>
          <p>OTP sent to {loginResult.email}</p>
          <input
            type="text"
            placeholder="Enter OTP"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            style={{ marginBottom: 8, width: '100%', padding: 6 }}
          />
          <br />
          <button onClick={handleVerifyOtp} style={{ padding: 8, width: '100%' }}>
            Verify OTP
          </button>
        </>
      )}

      {error && (
        <p
          style={{
            color: 'red',
            backgroundColor: '#ffecec',
            border: '1px solid #f5aca6',
            padding: '6px 10px',
            borderRadius: 4,
            marginTop: 10,
          }}
        >
          {error}
        </p>
      )}
    </div>
  );
};

export default LoginPage;
