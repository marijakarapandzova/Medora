import React, { useState, useEffect } from 'react';
import { referralService } from '../../services/referralService';
import { patientService } from '../../services/patientService';
import { doctorService } from '../../services/doctorService';
import { medicalRecordService } from '../../services/medicalRecordService';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';

function ReferralList() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isDoctor = user.role === 'DOCTOR';

  const [referrals, setReferrals] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [patients, setPatients] = useState([]);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [searchType, setSearchType] = useState('patient'); // 'patient', 'fromDoctor', 'toDoctor'
  const [searchValue, setSearchValue] = useState('');
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);

  // For creating referral
  const [showCreateForm, setShowCreateForm] = useState(false);
  const getDefaultAppointmentDate = () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  };
  const [formData, setFormData] = useState({
    fromDoctorId: isDoctor ? (user.doctorId || '') : '',
    toDoctorId: '',
    patientId: '',
    recordId: '',
    reason: '',
    referralDate: new Date().toISOString().split('T')[0],
    appointmentDate: getDefaultAppointmentDate(),
    appointmentTime: '14:00',
  });

  useEffect(() => {
    loadDoctorsAndPatients();
  }, []);

  const loadDoctorsAndPatients = async () => {
    try {
      const [doctorsRes, patientsRes] = await Promise.all([
        doctorService.getAllDoctors(),
        patientService.getAllPatients(),
      ]);
      setDoctors(doctorsRes.data);
      setPatients(patientsRes.data);
    } catch (err) {
      console.error('Error loading data:', err);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (!searchValue.trim()) {
        setError('Please enter a search value');
        setLoading(false);
        return;
      }

      let response;
      if (searchType === 'patient') {
        response = await referralService.getReferralsByPatient(searchValue);
      } else if (searchType === 'fromDoctor') {
        response = await referralService.getReferralsByFromDoctor(searchValue);
      } else {
        response = await referralService.getReferralsByToDoctor(searchValue);
      }

      setReferrals(Array.isArray(response.data) ? response.data : [response.data]);
      setSearched(true);
    } catch (err) {
      setError('No referrals found');
      setReferrals([]);
      setSearched(true);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateReferral = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      if (!formData.fromDoctorId || !formData.toDoctorId || !formData.patientId || !formData.reason || !formData.appointmentDate || !formData.appointmentTime) {
        setError('Please fill in all required fields');
        return;
      }

      // Get patient's medical record ID
      const patientId = parseInt(formData.patientId);
      const medicalRecordRes = await medicalRecordService.getMedicalRecordByPatientId(patientId);
      const recordId = medicalRecordRes.data.recordId;

      const referralData = {
        medicalRecordId: recordId,
        fromDoctorId: parseInt(formData.fromDoctorId),
        toDoctorId: parseInt(formData.toDoctorId),
        reason: formData.reason,
        referralDate: formData.referralDate,
        appointmentDate: formData.appointmentDate,
        appointmentTime: formData.appointmentTime + ':00',
      };

      await referralService.createReferral(referralData);
      setSuccess('Referral created successfully!');
      setFormData({
        fromDoctorId: '',
        toDoctorId: '',
        patientId: '',
        recordId: '',
        reason: '',
        referralDate: new Date().toISOString().split('T')[0],
        appointmentDate: getDefaultAppointmentDate(),
        appointmentTime: '14:00',
      });
      setShowCreateForm(false);

      // Refresh referrals list
      setTimeout(() => {
        setSearched(false);
        setSearchValue('');
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create referral');
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  return (
    <div>
      <h1 className="text-3xl font-bold mb-6" style={{ color: '#7c3aed' }}>Doctor Referrals</h1>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
      {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

      {/* Create Referral Button */}
      <div className="mb-6">
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          style={{
            background: '#bfdbfe',
            color: '#1e1035',
            padding: '8px 24px',
            borderRadius: '6px',
            border: 'none',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: '400'
          }}
          onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'}
          onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}
        >
          {showCreateForm ? 'Cancel' : 'Create New Referral'}
        </button>
      </div>

      {/* Create Referral Form */}
      {showCreateForm && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-xl font-bold mb-4">Create New Referral</h2>
          <form onSubmit={handleCreateReferral} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-semibold mb-2">From Doctor *</label>
                {isDoctor ? (
                  <p className="px-4 py-2 bg-gray-100 rounded-lg text-gray-700">
                    Dr. {user.firstName} {user.lastName}
                  </p>
                ) : (
                  <select
                    name="fromDoctorId"
                    value={formData.fromDoctorId}
                    onChange={handleFormChange}
                    className="w-full px-4 py-2 border rounded-lg"
                    required
                  >
                    <option value="">Select referring doctor</option>
                    {doctors.map(doctor => (
                      <option key={doctor.doctorId} value={doctor.doctorId}>
                        Dr. {doctor.firstName} {doctor.lastName} ({doctor.specialization.specializationName})
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">To Doctor *</label>
                <select
                  name="toDoctorId"
                  value={formData.toDoctorId}
                  onChange={handleFormChange}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select receiving doctor</option>
                  {doctors.map(doctor => (
                    <option key={doctor.doctorId} value={doctor.doctorId}>
                      Dr. {doctor.firstName} {doctor.lastName} ({doctor.specialization.specializationName})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">Patient *</label>
                <select
                  name="patientId"
                  value={formData.patientId}
                  onChange={handleFormChange}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select patient</option>
                  {patients.map(patient => (
                    <option key={patient.patientId} value={patient.patientId}>
                      {patient.firstName} {patient.lastName} ({patient.embg})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">Referral Date *</label>
                <input
                  type="date"
                  name="referralDate"
                  value={formData.referralDate}
                  onChange={handleFormChange}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">Appointment Date *</label>
                <input
                  type="date"
                  name="appointmentDate"
                  value={formData.appointmentDate}
                  onChange={handleFormChange}
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
                  onChange={handleFormChange}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-semibold mb-2">Reason for Referral *</label>
              <textarea
                name="reason"
                value={formData.reason}
                onChange={handleFormChange}
                placeholder="e.g., Requires specialist evaluation for suspected cardiac condition"
                className="w-full px-4 py-2 border rounded-lg"
                rows="3"
                required
              />
            </div>

            <button
              type="submit"
              className="bg-purple-600 text-white px-6 py-2 rounded hover:bg-purple-700"
            >
              Create Referral
            </button>
          </form>
        </div>
      )}

      {/* Search Form */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-bold mb-4">Search Referrals</h2>
        <form onSubmit={handleSearch} className="space-y-4">
          <div className="flex gap-4">
            <div className="flex-1">
              <label className="block text-sm font-semibold mb-2">Search By</label>
              <select
                value={searchType}
                onChange={(e) => {
                  setSearchType(e.target.value);
                  setSearchValue('');
                  setSearched(false);
                }}
                className="w-full px-4 py-2 border rounded-lg"
              >
                <option value="patient">Patient ID</option>
                <option value="fromDoctor">Referring Doctor ID</option>
                <option value="toDoctor">Receiving Doctor ID</option>
              </select>
            </div>

            <div className="flex-1">
              <label className="block text-sm font-semibold mb-2">Enter ID</label>
              <input
                type="number"
                value={searchValue}
                onChange={(e) => setSearchValue(e.target.value)}
                placeholder="Enter ID"
                className="w-full px-4 py-2 border rounded-lg"
              />
            </div>

            <div className="flex items-end">
              <button
                type="submit"
                disabled={loading}
                className="bg-purple-600 text-white px-8 py-2 rounded-full hover:bg-purple-700 disabled:bg-gray-400"
              >
                {loading ? 'Searching...' : 'Search'}
              </button>
            </div>
          </div>
        </form>
      </div>

      {/* Referrals List */}
      {searched && referrals.length > 0 && (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold">From Doctor</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">To Doctor</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Patient</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Reason</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Referral Date</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Appointment Date</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Appointment Time</th>
              </tr>
            </thead>
            <tbody>
              {referrals.map(referral => (
                <tr key={referral.referralId} className="border-t hover:bg-gray-50">
                  <td className="px-6 py-3">{referral.fromDoctorName}</td>
                  <td className="px-6 py-3">{referral.toDoctorName}</td>
                  <td className="px-6 py-3">{referral.patientName}</td>
                  <td className="px-6 py-3">{referral.reason}</td>
                  <td className="px-6 py-3">{referral.referralDate}</td>
                  <td className="px-6 py-3">{referral.appointmentDate}</td>
                  <td className="px-6 py-3">{referral.appointmentTime}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* No results message */}
      {searched && referrals.length === 0 && (
        <div className="bg-blue-50 rounded-lg p-6 text-center">
          <p className="text-gray-600">No referrals found</p>
        </div>
      )}
    </div>
  );
}

export default ReferralList;
