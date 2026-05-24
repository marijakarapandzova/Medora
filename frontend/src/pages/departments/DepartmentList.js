import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { departmentService } from '../../services/departmentService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function DepartmentList() {
  const formatDepartmentName = (name) => {
    if (!name) return 'N/A';
    return name.replace(/_DEPT$/, '').replace(/_/g, ' ').split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()).join(' ');
  };

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.role === 'ADMIN';
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchDepartments();
  }, []);

  const fetchDepartments = async () => {
    try {
      setLoading(true);
      const response = await departmentService.getAllDepartments();
      setDepartments(response.data);
    } catch (err) {
      setError('Failed to fetch departments');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <Loading />;

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 style={{ fontSize: '36px', fontWeight: 'normal', color: '#7c3aed' }}>Hospital Departments</h1>
        {isAdmin && (
          <div className="space-x-2">
            <button
              onClick={() => navigate('/departments/new')}
              style={{
                background: '#bfdbfe',
                color: '#1e1035',
                padding: '8px 16px',
                borderRadius: '6px',
                border: 'none',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: '400'
              }}
              onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'}
              onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}
            >
              Add Department
            </button>
          </div>
        )}
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {departments.map((dept) => (
          <div key={dept.departmentId} className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition flex flex-col items-center">
            <h2 style={{ fontSize: '28px', fontWeight: 'normal', marginBottom: '20px', textAlign: 'center' }}>{formatDepartmentName(dept.departmentName)}</h2>
            <div className="flex gap-3 w-full">
              <Link
                to={`/departments/${dept.departmentId}/doctors`}
                style={{
                  flex: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: '#9333ea',
                  color: 'white',
                  padding: '6px 10px',
                  borderRadius: '50px',
                  textDecoration: 'none',
                  fontSize: '14px',
                  fontWeight: '400'
                }}
                onMouseEnter={(e) => e.currentTarget.style.background = '#7e22ce'}
                onMouseLeave={(e) => e.currentTarget.style.background = '#9333ea'}
              >
                View Doctors
              </Link>
              <Link
                to={`/departments/${dept.departmentId}`}
                style={{
                  flex: 1,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  background: '#7c3aed',
                  color: 'white',
                  padding: '6px 10px',
                  borderRadius: '50px',
                  textDecoration: 'none',
                  fontSize: '14px',
                  fontWeight: '400'
                }}
                onMouseEnter={(e) => e.currentTarget.style.background = '#6d28d9'}
                onMouseLeave={(e) => e.currentTarget.style.background = '#7c3aed'}
              >
                View Details
              </Link>
            </div>
          </div>
        ))}
      </div>

      {departments.length === 0 && (
        <div className="text-center py-12">
          <p className="text-gray-500">No departments found</p>
        </div>
      )}
    </div>
  );
}

export default DepartmentList;