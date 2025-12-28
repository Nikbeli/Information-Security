import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import type { InfoResponse } from './../types/info';
import { infoService } from './../api/clients';
import './WelcomePage.css';

const WelcomePage: React.FC = () => {
  const [info, setInfo] = useState<InfoResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const navigate = useNavigate();

  useEffect(() => {
    loadInfo();
  }, []);

  const loadInfo = async () => {
    try {
      setLoading(true);
      const data = await infoService.getAbout();
      setInfo(data);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Ошибка загрузки');
    } finally {
      setLoading(false);
    }
  };

  const handleNext = () => {
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="welcome-container">
        <div className="loading">Загрузка информации...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="welcome-container">
        <div className="error">
          <h3>Ошибка загрузки</h3>
          <p>{error}</p>
          <button onClick={loadInfo} className="retry-button">
            Повторить попытку
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="welcome-container">
      <div className="welcome-header">
        <h1>Система криптографической защиты</h1>
        <p>Добро пожаловать в систему управления пользователями</p>
      </div>

      {info && (
        <div className="info-panel">
          <InfoRow label="👤 Автор:" value={`${info.author} (${info.group})`} />
          <InfoRow label="🧪 Работа:" value={info.labNumber} />
          <InfoRow label="🔒 Тема:" value={info.topic} />
          <InfoRow label="⚙️ Алгоритмы:" value={info.algorithms} />
          <InfoRow label="🛡️ Политика паролей:" value={info.passwordPolicy} />
        </div>
      )}

      <div className="actions">
        <button 
          className="next-button"
          onClick={handleNext}
          disabled={!info}
        >
          Перейти к системе →
        </button>
      </div>
    </div>
  );
};

const InfoRow: React.FC<{ label: string; value: string }> = ({ label, value }) => (
  <div className="info-row">
    <div className="info-label">{label}</div>
    <div className="info-value">{value}</div>
  </div>
);

export default WelcomePage;