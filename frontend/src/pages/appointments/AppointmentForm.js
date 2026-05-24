import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { appointmentService } from '../../services/appointmentService';
import { patientService } from '../../services/patientService';
import { doctorService } from '../../services/doctorService';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';

function AppointmentForm() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isPatient = user.role === 'PATIENT';
  const isDoctor = user.role === 'DOCTOR';

  const [formData, setFormData] = useState({
    patientId: isPatient ? (user.patientId || '') : '',
    doctorId: isDoctor ? (user.doctorId || '') : '',
    appointmentDate: '',
    appointmentTime: '',
  });
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      // For patients, we don't need to fetch all patients
      if (!isPatient) {
        const patientsRes = await patientService.getAllPatients();
        setPatients(patientsRes.data);
      }

      const doctorsRes = await doctorService.getAllDoctors();
      setDoctors(doctorsRes.data);
    } catch (err) {
      setError('Failed to fetch doctors');
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.patientId || !formData.doctorId || !formData.appointmentDate || !formData.appointmentTime) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);
      await appointmentService.createAppointment({
        patientId: parseInt(formData.patientId),
        doctorId: parseInt(formData.doctorId),
        appointmentDate: formData.appointmentDate,
        appointmentTime: formData.appointmentTime,
      });
      setSuccess('Appointment created successfully!');
      setTimeout(() => navigate('/appointments'), 1500);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create appointment');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Create Appointment</h1>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
      {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-4">
        {isPatient ? (
          <div className="bg-blue-50 rounded-lg p-4 mb-4">
            <p className="text-sm text-gray-700">
              <strong>Patient:</strong> {user.firstName} {user.lastName} ({user.username})
            </p>
          </div>
        ) : (
          <div>
            <label className="block text-sm font-semibold mb-2">Patient *</label>
            <select
              name="patientId"
              value={formData.patientId}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg"
              required
            >
              <option value="">Select Patient</option>
              {patients.map(patient => (
                <option key={patient.patientId} value={patient.patientId}>
                  {patient.firstName} {patient.lastName} ({patient.embg})
                </option>
              ))}
            </select>
          </div>
        )}

        {isDoctor ? (
          <div>
            <label className="block text-sm font-semibold mb-2">Doctor</label>
            <p className="px-4 py-2 bg-gray-100 rounded-lg text-gray-700">
              Dr. {user.firstName} {user.lastName}
            </p>
          </div>
        ) : (
          <div>
            <label className="block text-sm font-semibold mb-2">Doctor *</label>
            <select
              name="doctorId"
              value={formData.doctorId}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg"
              required
            >
              <option value="">Select Doctor</option>
              {doctors.map(doctor => (
                <option key={doctor.doctorId} value={doctor.doctorId}>
                  Dr. {doctor.firstName} {doctor.lastName}
                </option>
              ))}
            </select>
          </div>
        )}

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-semibold mb-2">Appointment Date *</label>
            <input
              type="date"
              name="appointmentDate"
              value={formData.appointmentDate}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-semibold mb-2">Appointment Time *</label>
            <input
              type="time"
              name="appointmentTime"
              value={formData.appointmentTime}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg"
              required
            />
          </div>
        </div>

        <div className="flex gap-4 pt-4">
          <button
            type="submit"
            disabled={loading}
            className="bg-purple-600 text-white px-6 py-2 rounded hover:bg-purple-700 disabled:opacity-50"
          >
            {loading ? 'Creating...' : 'Create Appointment'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/appointments')}
            className="bg-gray-300 text-gray-700 px-6 py-2 rounded hover:bg-gray-400"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

export default AppointmentForm;
