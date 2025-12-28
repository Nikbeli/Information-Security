import axios, { type AxiosInstance } from 'axios';
import type { InfoResponse } from './../types/info';
import type { UserCreate, UserGet, UserUpdate } from '../types/user';
import type { AuthResponse, LoginResult, OtpVerifyRequest, UserLogin } from '../types/auth';

const API_BASE = 'http://localhost:8988/api/v1';

// === Axios-инстансы ===
export const infoApi = axios.create({ baseURL: `${API_BASE}/info`, timeout: 10000 });
export const userApi = axios.create({ baseURL: `${API_BASE}/user`, timeout: 10000 });
export const authApi = axios.create({ baseURL: `${API_BASE}/auth`, timeout: 10000 });

// === INFO SERVICE ===
export const infoService = {
  getAbout: async (): Promise<InfoResponse> => {
    const response = await infoApi.get('/about');
    return response.data;
  },
};

// === USER SERVICE ===
export const userService = {
  getAllUsers: async (search?: string, pageable?: any): Promise<any> => {
    const params: any = {};
    if (search) params.search = search;
    if (pageable) Object.assign(params, pageable);
    const response = await userApi.get('/users', { params });
    return response.data;
  },

  registerUser: async (userData: UserCreate): Promise<UserGet> => {
    const response = await userApi.post('/register', userData);
    return response.data;
  },

  getUserByEmail: async (email: string): Promise<UserGet> => {
    const response = await userApi.get(`/users/${email}`);
    return response.data;
  },

  getUserById: async (id: string): Promise<UserGet> => {
    const response = await userApi.get(`/users/user/${id}/id`);
    return response.data;
  },

  updateUser: async (id: string, userData: UserUpdate): Promise<UserGet> => {
    const response = await userApi.put(`/users/${id}`, userData);
    return response.data;
  },

  // Добавляем этот метод
getMe: async (token?: string) => {
  const authToken = token || localStorage.getItem('authToken');
  if (!authToken) throw new Error('Token not found');

  const response = await userApi.get('/me', {
    headers: {
      Authorization: `Bearer ${authToken}`,
    },
  });

  return response.data; // возвращаем данные пользователя
},


  deleteUser: async (email: string): Promise<void> => {
    await userApi.delete(`/users/${email}`);
  },
};

// === AUTH SERVICE ===
export const authService = {
  login: async (credentials: UserLogin): Promise<LoginResult> => {
    const response = await authApi.post('/login', credentials);
    // ✅ маппим backend-ответ точно под LoginResult
    return {
      email: response.data.email,
      requiresOtp: response.data.requiresOtp,
      token: response.data.token ?? null,
    };
  },

  // получение токена через OTP
  getToken: async (otpData: OtpVerifyRequest): Promise<AuthResponse> => {
    const response = await authApi.post('/token', otpData);
    const token = response.data;
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;

    if (token && user) {
      localStorage.setItem('authToken', token);
      localStorage.setItem('user', JSON.stringify(user));
      return { token, user };
    }
    throw new Error('Token or user data missing');
  },

  // Проверка токена на валидность
  validateToken: async (token?: string): Promise<boolean> => {
    const authToken = token || localStorage.getItem('authToken');
    if (!authToken) throw new Error('Token not found');
    const response = await authApi.get(`/valid-token?token=${authToken}`);
    return response.data === 'Token is valid';
  },

  // Регистрация
  register: async (userData: UserCreate): Promise<AuthResponse> => {
    const response = await authApi.post('/register', userData);
    return response.data;
  },

    resetPassword: async (payload: { email: string; oldPassword: string; newPassword: string; confirmPassword: string; }) => {
    const response = await authApi.post('/reset-password', payload);
    return response.data;
  },

  // Выход
  logout: async (): Promise<void> => {
    try { await authApi.post('/logout'); } finally {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
  },
};

// === Интерцепторы ===
const setupAuthInterceptor = (instance: AxiosInstance) => {
  instance.interceptors.request.use((config: any) => {
    const token = localStorage.getItem('authToken');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });
};

const setupErrorInterceptor = (instance: AxiosInstance) => {
  instance.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );
};

// === Применяем интерцепторы ===
[infoApi, userApi, authApi].forEach(api => {
  setupAuthInterceptor(api);
  setupErrorInterceptor(api);
});
