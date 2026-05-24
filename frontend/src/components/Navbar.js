import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';

function Navbar() {
  const [user, setUser] = useState(null);
  const [showMenu, setShowMenu] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      setUser(JSON.parse(userStr));
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  const getInitials = () => {
    if (!user) return '';
    return `${user.firstName?.[0]}${user.lastName?.[0]}`.toUpperCase();
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <img src="/logo.png" alt="Medora Logo" style={{ height: '40px', width: 'auto' }} />
      </div>

      <div className="navbar-links">
        <Link to="/" className={`navbar-link ${isActive('/') ? 'active' : ''}`}>
          Overview
        </Link>
        {user?.role !== 'PATIENT' && user?.role !== 'LAB_TECHNICIAN' && user?.role !== 'BILLING_ADMIN' && (
          <Link to="/patients" className={`navbar-link ${isActive('/patients') ? 'active' : ''}`}>
            Patients
          </Link>
        )}
        <Link to="/doctors" className={`navbar-link ${isActive('/doctors') ? 'active' : ''}`}>
          Doctors
        </Link>
        <Link to="/departments" className={`navbar-link ${isActive('/departments') ? 'active' : ''}`}>
          Departments
        </Link>
        {user?.role !== 'LAB_TECHNICIAN' && user?.role !== 'BILLING_ADMIN' && (
          <>
            <Link to="/appointments" className={`navbar-link ${isActive('/appointments') ? 'active' : ''}`}>
              Appointments
            </Link>
            {user?.role === 'PATIENT' && (
              <>
                <Link to="/medical-records" className={`navbar-link ${isActive('/medical-records') ? 'active' : ''}`}>
                  Medical Records
                </Link>
                <Link to="/billing" className={`navbar-link ${isActive('/billing') ? 'active' : ''}`}>
                  Billing
                </Link>
              </>
            )}
          </>
        )}
        {(user?.role === 'ADMIN' || user?.role === 'LAB_TECHNICIAN' || user?.role === 'DOCTOR') && (
          <Link to="/lab-tests" className={`navbar-link ${isActive('/lab-tests') ? 'active' : ''}`}>
            Lab Tests
          </Link>
        )}
        {!user?.role || user?.role === 'ADMIN' || user?.role === 'DOCTOR' ? (
          <>
            {user?.role !== 'LAB_TECHNICIAN' && user?.role !== 'BILLING_ADMIN' && (
              <Link to="/medical-records" className={`navbar-link ${isActive('/medical-records') ? 'active' : ''}`}>
                Medical Records
              </Link>
            )}
            {user?.role !== 'PATIENT' && user?.role !== 'LAB_TECHNICIAN' && user?.role !== 'BILLING_ADMIN' && (
              <>
                <Link to="/medical-reports" className={`navbar-link ${isActive('/medical-reports') ? 'active' : ''}`}>
                  Medical Reports
                </Link>
                <Link to="/procedures" className={`navbar-link ${isActive('/procedures') ? 'active' : ''}`}>
                  Procedures
                </Link>
                <Link to="/referrals" className={`navbar-link ${isActive('/referrals') ? 'active' : ''}`}>
                  Referrals
                </Link>
              </>
            )}
          </>
        ) : null}
        {(user?.role === 'ADMIN' || user?.role === 'BILLING_ADMIN') && (
          <Link to="/billing" className={`navbar-link ${isActive('/billing') ? 'active' : ''}`}>
            Billing
          </Link>
        )}
      </div>

      <div className="navbar-right">
        {user && (
          <div className="navbar-avatar" style={{ position: 'relative' }}>
            <div className="navbar-avatar-circle">{getInitials()}</div>
            <div className="navbar-avatar-name">{user.firstName}</div>
            <button
              onClick={() => setShowMenu(!showMenu)}
              style={{
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                marginLeft: '4px',
                color: 'var(--color-neutral-600)',
                fontSize: '12px'
              }}
            >
              ▼
            </button>
            {showMenu && (
              <div style={{
                position: 'absolute',
                top: '100%',
                right: 0,
                background: 'white',
                border: '1px solid var(--color-neutral-400)',
                borderRadius: '6px',
                minWidth: '150px',
                marginTop: '4px',
                boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
                zIndex: 1000
              }}>
                <div style={{
                  padding: '8px 0',
                  fontSize: '11px',
                  color: 'var(--color-neutral-600)',
                  borderBottom: '1px solid var(--color-neutral-400)',
                  paddingLeft: '12px',
                  paddingRight: '12px',
                  paddingTop: '8px',
                  paddingBottom: '8px'
                }}>
                  {user.role}
                </div>
                <button
                  onClick={handleLogout}
                  style={{
                    width: '100%',
                    padding: '8px 12px',
                    background: 'none',
                    border: 'none',
                    textAlign: 'left',
                    cursor: 'pointer',
                    fontSize: '12px',
                    color: 'var(--color-status-red)',
                    transition: 'background 0.2s'
                  }}
                  onMouseEnter={(e) => e.target.style.background = 'var(--color-neutral-200)'}
                  onMouseLeave={(e) => e.target.style.background = 'none'}
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
