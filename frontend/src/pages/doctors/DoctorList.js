import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { doctorService } from '../../services/doctorService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function DoctorList() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.role === 'ADMIN';
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchDoctors();
  }, []);

  const fetchDoctors = async () => {
    try {
      setLoading(true);
      const response = await doctorService.getAllDoctors();
      setDoctors(response.data);
    } catch (err) {
      setError('Failed to fetch doctors');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const filteredDoctors = doctors.filter(doctor =>
    doctor.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    doctor.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    doctor.emailAddress.includes(searchTerm)
  );

  if (loading) return <Loading />;

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold" style={{ color: '#7c3aed' }}>Doctors</h1>
        {isAdmin && (
          <Link to="/doctors/new" style={{
            display: 'inline-block',
            background: '#bfdbfe',
            color: '#1e1035',
            padding: '8px 16px',
            borderRadius: '6px',
            textDecoration: 'none',
            fontSize: '14px',
            fontWeight: '400'
          }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
            Add Doctor
          </Link>
        )}
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div className="mb-6">
        <input
          type="text"
          placeholder="Search by name or email..."
          className="w-full px-4 py-2 border rounded-lg"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-100">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-semibold">Name</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Email</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Specialization</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Level</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Department</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredDoctors.map(doctor => (
              <tr key={doctor.doctorId} className="border-t hover:bg-gray-50">
                <td className="px-6 py-3">{doctor.firstName} {doctor.lastName}</td>
                <td className="px-6 py-3">{doctor.emailAddress}</td>
                <td className="px-6 py-3">{doctor.specialization?.specializationName || 'N/A'}</td>
                <td className="px-6 py-3">{doctor.level?.level || 'N/A'}</td>
                <td className="px-6 py-3">{doctor.department?.departmentName ? doctor.department.departmentName.replace(/_DEPT$/, '').replace(/_/g, ' ').split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()).join(' ') : 'N/A'}</td>
                <td className="px-6 py-3">
                  <Link to={`/doctors/${doctor.doctorId}`} style={{ color: '#7c3aed', textDecoration: 'none', fontWeight: '400' }} onMouseEnter={(e) => e.currentTarget.style.textDecoration = 'underline'} onMouseLeave={(e) => e.currentTarget.style.textDecoration = 'none'}>
                    View
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default DoctorList;
