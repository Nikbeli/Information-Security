import React, { useEffect, useState } from 'react';
import { userService } from '../api/clients';
import { useNavigate } from 'react-router-dom';

const UserManager: React.FC = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState<any[]>([]);
  const [search, setSearch] = useState('');
  const [newEmail, setNewEmail] = useState('');
  const [editingUser, setEditingUser] = useState<any | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('authToken');
    if (!token) {
      navigate('/login');
      return;
    }

    userService
      .getMe(token)
      .then((userData) => {
        if (userData.role !== 'ADMIN') {
          navigate('/login');
          return;
        }
        if (userData.role === 'USER') {
          navigate('/changepassword');
          return;
        }
        setUser(userData);
        loadUsers();
      })
      .catch((err) => {
        console.error('Ошибка получения профиля:', err);
        localStorage.removeItem('authToken');
        localStorage.removeItem('user');
        navigate('/login');
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  const loadUsers = async () => {
    try {
      const data = await userService.getAllUsers(search);
      setUsers(Array.isArray(data) ? data : data.content || data.users || []);
    } catch (e) {
      console.error('Ошибка загрузки пользователей:', e);
    }
  };

  const handleAddUser = async () => {
    if (!newEmail.trim()) return;
    try {
      await userService.registerUser({ email: newEmail });
      setNewEmail('');
      await loadUsers();
    } catch (e) {
      console.error('Ошибка добавления пользователя:', e);
      alert('Ошибка при добавлении пользователя');
    }
  };

  /**
   * СБРОС/УСТАНОВКА пароля админом + обновление остальных полей
   * Если у пользователя включены passwordRestrictions, проверяем правило:
   *  — все символы пароля должны быть уникальны (отсутствие повторяющихся символов).
   */
  const handleSaveEdit = async (id: string) => {
  try {
    if (!editingUser) return;

    const {
      email,
      role,
      accountLocked,
      passwordRestrictions,
      passwordExpirationMonths,
      minPasswordLength,
      failedAttempts,
      firstLogin,
      emailConfirmed,
      newPassword, // новое поле пароля
    } = editingUser;

    

     // ✅ Проверки пароля
      if (newPassword && newPassword.length > 0) {
        if (passwordRestrictions) {
          const chars = newPassword.split('');
          const unique = new Set(chars);
          if (unique.size !== chars.length) {
            alert('⚠️ Пароль не должен содержать повторяющихся символов.');
            return;
          }
        }
        if (minPasswordLength && newPassword.length < Number(minPasswordLength)) {
          alert(`⚠️ Пароль должен быть не короче ${minPasswordLength} символов.`);
          return;
        }
      }


    // отправляем всё на бэк одним запросом
    await userService.updateUser(id, {
      email,
      role,
      accountLocked,
      passwordRestrictions,
      passwordExpirationMonths,
      minPasswordLength,
      failedAttempts,
      firstLogin,
      emailConfirmed,
      password_hash: newPassword && newPassword.length > 0 ? newPassword : undefined, // только если ввели
    });

    setEditingUser(null);
    await loadUsers();
  } catch (e) {
    console.error('Ошибка обновления пользователя:', e);
    alert('Ошибка при обновлении пользователя');
  }
};


  const handleDeleteUser = async (email: string) => {
    if (!window.confirm('Удалить пользователя ' + email + '?')) return;
    try {
      await userService.deleteUser(email);
      await loadUsers();
    } catch (e) {
      console.error('Ошибка удаления пользователя:', e);
      alert('Ошибка при удалении пользователя');
    }
  };

  if (loading) return <p>Загрузка...</p>;
  if (!user) return null;

  return (
    <div>
      <h2>Панель управления пользователями</h2>
      <p>Привет, {user.email}</p>
      <p>Роль: {user.role}</p>

      <hr />
      <h3>Список пользователей</h3>

      <div style={{ marginBottom: 10 }}>
        <input
          type="text"
          placeholder="Поиск по email..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button onClick={loadUsers}>🔍 Найти</button>
      </div>

      <div style={{ marginBottom: 10 }}>
        <input
          type="email"
          placeholder="Введите email нового пользователя"
          value={newEmail}
          onChange={(e) => setNewEmail(e.target.value)}
        />
        <button onClick={handleAddUser}>➕ Добавить</button>
      </div>

      <table border={1} cellPadding={6} style={{ borderCollapse: 'collapse', width: '100%' }}>
        <thead>
          <tr>
            <th>Email</th>
            <th>Роль</th>
            <th>Заблокирован</th>
            <th>Огр. пароля</th>
            <th>Мин. длина</th>
            <th>Истечение пароля (мес)</th>
            <th>Попытки</th>
            <th>Первый вход</th>
            <th>Email подтверждён</th>
            <th>Хэш пароля</th>
            <th>Соль</th>
            <th>Дата смены пароля</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          {users.length === 0 && (
            <tr>
              <td colSpan={13} style={{ textAlign: 'center' }}>Нет пользователей</td>
            </tr>
          )}
          {users.map((u) => (
            <tr key={u.id}>
              <td>
                {editingUser?.id === u.id ? (
                  <input
                    type="email"
                    value={editingUser.email}
                    onChange={(e) =>
                      setEditingUser({ ...editingUser, email: e.target.value })
                    }
                  />
                ) : (
                  u.email
                )}
              </td>

              <td>
                {editingUser?.id === u.id ? (
                  <select
                    value={editingUser.role}
                    onChange={(e) =>
                      setEditingUser({ ...editingUser, role: e.target.value })
                    }
                  >
                    <option value="ADMIN">ADMIN</option>
                    <option value="USER">USER</option>
                  </select>
                ) : (
                  u.role
                )}
              </td>

              <td>
                {editingUser?.id === u.id ? (
                  <input
                    type="checkbox"
                    checked={!!editingUser.accountLocked}
                    onChange={(e) =>
                      setEditingUser({ ...editingUser, accountLocked: e.target.checked })
                    }
                  />
                ) : (
                  u.accountLocked ? '🔒' : '🔓'
                )}
              </td>

              <td>
                {editingUser?.id === u.id ? (
                  <input
                    type="checkbox"
                    checked={!!editingUser.passwordRestrictions}
                    onChange={(e) =>
                      setEditingUser({ ...editingUser, passwordRestrictions: e.target.checked })
                    }
                  />
                ) : (
                  u.passwordRestrictions ? 'Да' : 'Нет'
                )}
              </td>

              <td>
                {editingUser?.id === u.id ? (
                  <input
                    type="number"
                    value={editingUser.minPasswordLength ?? 0}
                    onChange={(e) =>
                      setEditingUser({ ...editingUser, minPasswordLength: +e.target.value })
                    }
                    style={{ width: 60 }}
                  />
                ) : (
                  u.minPasswordLength
                )}
              </td>

              <td>
                {editingUser?.id === u.id ? (
                  <input
                    type="number"
                    value={editingUser.passwordExpirationMonths ?? 0}
                    onChange={(e) =>
                      setEditingUser({
                        ...editingUser,
                        passwordExpirationMonths: +e.target.value,
                      })
                    }
                    style={{ width: 60 }}
                  />
                ) : (
                  u.passwordExpirationMonths
                )}
              </td>

              <td>
                {editingUser?.id === u.id ? (
                  <input
                    type="number"
                    value={editingUser.failedAttempts ?? 0}
                    onChange={(e) =>
                      setEditingUser({ ...editingUser, failedAttempts: +e.target.value })
                    }
                    style={{ width: 60 }}
                  />
                ) : (
                  u.failedAttempts
                )}
              </td>

              <td>
                {editingUser?.id === u.id ? (
                  <input
                    type="checkbox"
                    checked={!!editingUser.firstLogin}
                    onChange={(e) =>
                      setEditingUser({ ...editingUser, firstLogin: e.target.checked })
                    }
                  />
                ) : (
                  u.firstLogin ? 'Да' : 'Нет'
                )}
              </td>

              <td>
                {editingUser?.id === u.id ? (
                  <input
                    type="checkbox"
                    checked={!!editingUser.emailConfirmed}
                    onChange={(e) =>
                      setEditingUser({ ...editingUser, emailConfirmed: e.target.checked })
                    }
                  />
                ) : (
                  u.emailConfirmed ? '✅' : '❌'
                )}
              </td>

              <td style={{ fontSize: '0.8em', color: '#555' }}>{u.password_hash}</td>
              <td style={{ fontSize: '0.8em', color: '#555' }}>{u.salt}</td>
              <td>{u.passwordLastChanged ?? '—'}</td>

              <td>
                {editingUser?.id === u.id ? (
                  <>
                    {/* Поле для установки пароля админом */}
                    <div style={{ marginBottom: 6 }}>
                      <input
                        type="password"
                        placeholder="Новый пароль (админ)"
                        value={editingUser.newPassword ?? ''}
                        onChange={(e) =>
                          setEditingUser({ ...editingUser, newPassword: e.target.value })
                        }
                        style={{ width: 180 }}
                      />
                    </div>

                    <div>
                      <button onClick={() => handleSaveEdit(u.id)}>💾</button>
                      <button onClick={() => setEditingUser(null)}>❌</button>
                    </div>
                  </>
                ) : (
                  <>
                    <button onClick={() => setEditingUser(u)}>✏️</button>
                    <button onClick={() => handleDeleteUser(u.email)}>🗑</button>
                  </>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default UserManager;
