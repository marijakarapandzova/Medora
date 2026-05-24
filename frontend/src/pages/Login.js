import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ErrorAlert from '../components/ErrorAlert';
import SuccessAlert from '../components/SuccessAlert';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!username.trim()) {
      setError('Username is required');
      return;
    }
    if (!password.trim()) {
      setError('Password is required');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username,
          password,
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Login failed');
      }

      const data = await response.json();

      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify({
        userId: data.userId,
        patientId: data.patientId,
        doctorId: data.doctorId,
        username: data.username,
        role: data.role,
        firstName: data.firstName,
        lastName: data.lastName,
      }));

      setSuccess('Login successful! Redirecting...');
      setTimeout(() => {
        navigate('/');
      }, 1000);
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-blue-200 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg shadow-2xl p-8 w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-5xl font-bold text-purple-600 mb-2">Medora</h1>
          <p className="text-gray-600">Hospital Management System</p>
        </div>

        {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
        {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

        <form onSubmit={handleLogin} className="space-y-6">
          <div>
            <label className="block text-sm font-normal text-gray-700 mb-2">
              Username (EMBG for Patients)
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username or EMBG"
              className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-purple-500 transition"
              disabled={loading}
            />
          </div>

          <div>
            <label className="block text-sm font-normal text-gray-700 mb-2">
              Password
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-purple-500 transition"
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-purple-600 text-white font-bold py-3 rounded-lg hover:bg-purple-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="mt-8 p-4 bg-purple-50 rounded-lg border border-purple-200">
          <p className="text-sm text-gray-700 font-normal mb-2">Demo Credentials:</p>
          <p className="text-sm text-gray-600 mb-2">
            <strong>Admin:</strong> username: admin | password: admin123
          </p>
          <p className="text-sm text-gray-600 mb-2">
            <strong>Doctor:</strong> username: ivan.stojanov@medora.com | password: doctor123
          </p>
          <p className="text-sm text-gray-600">
            <strong>Patient:</strong> username: [EMBG] | password: password123
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;
