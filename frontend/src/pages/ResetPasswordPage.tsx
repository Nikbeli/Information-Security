import React, { useState } from 'react';
import { authService } from '../api/clients';
import { useNavigate } from 'react-router-dom';

const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // === Локальные проверки ===
    if (!email || !oldPassword || !newPassword || !confirmPassword) {
      setMessage('⚠️ Все поля обязательны для заполнения.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setMessage('❌ Пароли не совпадают.');
      return;
    }

    if (oldPassword === newPassword) {
      setMessage('❌ Новый пароль не должен совпадать со старым.');
      return;
    }

    setLoading(true);
    setMessage(null);

    try {
      // === Отправка запроса на бэк ===
      const response = await authService.resetPassword({
        email,
        oldPassword,
        newPassword,
        confirmPassword,
      });

      // Успех
      setSuccess(true);
      setMessage('✅ Пароль успешно изменён! Перенаправление...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      console.error('Ошибка сброса пароля:', err);

      // Берём сообщение прямо с бэка, если есть
      let backendMsg = 'Не удалось изменить пароль. Проверьте введённые данные.';
      if (err?.response?.data) {
        const data = err.response.data;
        if (typeof data === 'string') backendMsg = data;
        else if (data?.message) backendMsg = data.message;
        else if (data?.error) backendMsg = data.error;
      }

      setMessage(`❌ ${backendMsg}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        maxWidth: 420,
        margin: '80px auto',
        background: '#fff',
        padding: '30px 25px',
        borderRadius: 12,
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
      }}
    >
      <h2 style={{ textAlign: 'center', marginBottom: 20 }}>🔒 Смена пароля</h2>

      {message && (
        <p
          style={{
            textAlign: 'center',
            color: success ? 'green' : 'red',
            fontWeight: 500,
          }}
        >
          {message}
        </p>
      )}

      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: 12 }}>
          <label>Email:</label>
          <input
            type="email"
            value={email}
            placeholder="Введите ваш email"
            onChange={(e) => setEmail(e.target.value)}
            style={{
              width: '100%',
              padding: 10,
              borderRadius: 8,
              border: '1px solid #ccc',
              marginTop: 5,
            }}
          />
        </div>

        <div style={{ marginBottom: 12 }}>
          <label>Старый пароль:</label>
          <input
            type="password"
            value={oldPassword}
            placeholder="Введите текущий пароль"
            onChange={(e) => setOldPassword(e.target.value)}
            style={{
              width: '100%',
              padding: 10,
              borderRadius: 8,
              border: '1px solid #ccc',
              marginTop: 5,
            }}
          />
        </div>

        <div style={{ marginBottom: 12 }}>
          <label>Новый пароль:</label>
          <input
            type="password"
            value={newPassword}
            placeholder="Введите новый пароль"
            onChange={(e) => setNewPassword(e.target.value)}
            style={{
              width: '100%',
              padding: 10,
              borderRadius: 8,
              border: '1px solid #ccc',
              marginTop: 5,
            }}
          />
        </div>

        <div style={{ marginBottom: 20 }}>
          <label>Подтверждение нового пароля:</label>
          <input
            type="password"
            value={confirmPassword}
            placeholder="Повторите новый пароль"
            onChange={(e) => setConfirmPassword(e.target.value)}
            style={{
              width: '100%',
              padding: 10,
              borderRadius: 8,
              border: '1px solid #ccc',
              marginTop: 5,
            }}
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          style={{
            width: '100%',
            padding: 10,
            background: loading ? '#aaa' : '#007bff',
            color: '#fff',
            border: 'none',
            borderRadius: 8,
            cursor: loading ? 'not-allowed' : 'pointer',
            fontSize: 16,
            fontWeight: 600,
          }}
        >
          {loading ? 'Обработка...' : 'Изменить пароль'}
        </button>

        <button
          type="button"
          onClick={() => navigate('/login')}
          style={{
            marginTop: 10,
            width: '100%',
            padding: 10,
            background: '#f0f0f0',
            color: '#333',
            border: 'none',
            borderRadius: 8,
            cursor: 'pointer',
            fontSize: 15,
          }}
        >
          Назад ко входу
        </button>
      </form>
    </div>
  );
};

export default ResetPasswordPage;
