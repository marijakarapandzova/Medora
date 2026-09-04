import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { patientService } from '../../services/patientService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function PatientList() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.role === 'ADMIN';
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchPatients();
  }, []);

  const fetchPatients = async () => {
    try {
      setLoading(true);
      const response = await patientService.getAllPatients();
      setPatients(response.data);
    } catch (err) {
      setError('Failed to fetch patients');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const filteredPatients = patients.filter(patient =>
    patient.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    patient.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    patient.embg.includes(searchTerm)
  );

  if (loading) return <Loading />;

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold" style={{ color: '#7c3aed' }}>Patients</h1>
        {isAdmin && (
          <Link to="/patients/new" style={{
            display: 'inline-block',
            background: '#bfdbfe',
            color: '#1e1035',
            padding: '8px 16px',
            borderRadius: '6px',
            textDecoration: 'none',
            fontSize: '14px',
            fontWeight: '400'
          }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
            Add Patient
          </Link>
        )}
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div className="mb-6">
        <input
          type="text"
          placeholder="Search by name or EMBG..."
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
              <th className="px-6 py-3 text-left text-sm font-semibold">EMBG</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Email</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Phone</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredPatients.map(patient => (
              <tr key={patient.patientId} className="border-t hover:bg-gray-50">
                <td className="px-6 py-3">{patient.firstName} {patient.lastName}</td>
                <td className="px-6 py-3">{patient.embg}</td>
                <td className="px-6 py-3">{patient.emailAddress}</td>
                <td className="px-6 py-3">{patient.phoneNumber}</td>
                <td className="px-6 py-3">
                  <Link to={`/patients/${patient.patientId}`} className="text-purple-600 hover:underline">
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

export default PatientList;
