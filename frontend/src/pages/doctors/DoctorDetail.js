import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { doctorService } from '../../services/doctorService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function DoctorDetail() {
  const formatDepartmentName = (name) => {
    if (!name) return 'N/A';
    return name.replace(/_DEPT$/, '').replace(/_/g, ' ').split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()).join(' ');
  };

  const { id } = useParams();
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isPatient = user.role === 'PATIENT';
  const isAdmin = user.role === 'ADMIN';
  const isLabTechnician = user.role === 'LAB_TECHNICIAN';
  const isBillingAdmin = user.role === 'BILLING_ADMIN';

  const [doctor, setDoctor] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDoctor();
  }, [id]);

  const fetchDoctor = async () => {
    try {
      setLoading(true);
      const response = await doctorService.getDoctorById(id);
      setDoctor(response.data);
    } catch (err) {
      setError('Failed to fetch doctor details');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <Loading />;

  if (!doctor) {
    return (
      <div>
        <ErrorAlert message="Doctor not found" onClose={() => navigate('/doctors')} />
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold" style={{ color: '#7c3aed' }}>Dr. {doctor.firstName} {doctor.lastName}</h1>
        <div className="space-x-2">
          {isAdmin && (
            <Link to={`/doctors/${id}/edit`} className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700">
              Edit
            </Link>
          )}
          <button onClick={() => navigate('/doctors')} className="bg-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-400">
            Back
          </button>
        </div>
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4">Professional Information</h2>
          <div className="space-y-3">
            <InfoRow label="Email" value={doctor.emailAddress} />
            <InfoRow label="Specialization" value={doctor.specialization?.specializationName} />
            <InfoRow label="Level" value={doctor.level?.level} />
            <InfoRow label="Department" value={formatDepartmentName(doctor.department?.departmentName)} />
          </div>
        </div>

        {!isPatient && !isLabTechnician && !isBillingAdmin && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-bold mb-4">Quick Links</h2>
            <div className="space-y-2">
              <Link to={`/appointments?doctorId=${id}`} className="block p-3 bg-blue-50 hover:bg-purple-100 rounded text-purple-600">
                View My Appointments
              </Link>
              <Link to="/patients" className="block p-3 bg-green-50 hover:bg-green-100 rounded text-green-600">
                View Patients
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function InfoRow({ label, value }) {
  return (
    <div className="flex justify-between">
      <span className="text-gray-800">{label}:</span>
      <span className="text-gray-800">{value || 'N/A'}</span>
    </div>
  );
}

export default DoctorDetail;
