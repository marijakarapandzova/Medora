import React from 'react';
import { Navigate } from 'react-router-dom';

function ProtectedRoute({ children, requiredRoles = [] }) {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');

  // If no token, redirect to login
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // If roles are specified and user's role is not in the list, redirect to dashboard
  if (requiredRoles.length > 0 && userStr) {
    const user = JSON.parse(userStr);
    if (!requiredRoles.includes(user.role)) {
      return <Navigate to="/" replace />;
    }
  }

  return children;
}

export default ProtectedRoute;
