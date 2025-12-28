import { Routes, Route } from 'react-router-dom'
import WelcomePage from './pages/WelcomePage'
import LoginPage from './pages/LoginPage'
import './App.css'
import UserManager from './pages/UserManager'
import ResetPasswordPage from './pages/ResetPasswordPage'

function App() {
return (
    <Routes>
      <Route path="/" element={<WelcomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/usermanager" element={<UserManager />} />
      <Route path="/changepassword" element={<ResetPasswordPage />} />
    </Routes>
  )
}

export default App
