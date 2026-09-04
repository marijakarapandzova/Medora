import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { patientService } from '../../services/patientService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function PatientDetail() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isAdmin = user.role === 'ADMIN';
  const isLabTechnician = user.role === 'LAB_TECHNICIAN';
  const { id } = useParams();
  const navigate = useNavigate();
  const [patient, setPatient] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPatient = async () => {
      try {
        setLoading(true);
        const response = await patientService.getPatientById(id);
        setPatient(response.data);
      } catch (err) {
        setError('Failed to fetch patient details');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchPatient();
  }, [id]);

  if (loading) return <Loading />;

  if (!patient) {
    return (
      <div>
        <ErrorAlert message="Patient not found" onClose={() => navigate('/patients')} />
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold" style={{ color: '#7c3aed' }}>{patient.firstName} {patient.lastName}</h1>
        <div className="space-x-2">
          {isAdmin && (
            <Link to={`/patients/${id}/edit`} className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700">
              Edit
            </Link>
          )}
          <button onClick={() => navigate('/patients')} className="bg-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-400">
            Back
          </button>
        </div>
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4">Personal Information</h2>
          <div className="space-y-3">
            <InfoRow label="EMBG" value={patient.embg} />
            <InfoRow label="Email" value={patient.emailAddress} />
            <InfoRow label="Phone" value={patient.phoneNumber} />
            <InfoRow label="Date of Birth" value={patient.dateOfBirth} />
            <InfoRow label="Gender" value={patient.gender} />
            <InfoRow label="Blood Type" value={patient.bloodType} />
          </div>
        </div>

        {!isLabTechnician && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-bold mb-4">Quick Links</h2>
            <div className="space-y-2">
              <Link to={`/appointments?patientId=${id}`} className="block p-3 bg-blue-50 hover:bg-purple-100 rounded text-purple-600">
                View Appointments
              </Link>
              <Link to={`/medical-records?patientId=${id}`} className="block p-3 bg-green-50 hover:bg-green-100 rounded text-green-600">
                View Medical Records
              </Link>
              <Link to={`/billing?patientId=${id}`} className="block p-3 bg-purple-50 hover:bg-purple-100 rounded text-purple-600">
                View Billing History
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
      <span className="font-semibold text-gray-600">{label}:</span>
      <span className="text-gray-800">{value || 'N/A'}</span>
    </div>
  );
}

export default PatientDetail;
