import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { doctorService } from '../../services/doctorService';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';

function DoctorForm() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    emailAddress: '',
    levelId: '',
    specializationId: '',
    departmentId: '',
  });
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (id) {
      fetchDoctor();
    }
  }, [id]);

  const fetchDoctor = async () => {
    try {
      setLoading(true);
      const response = await doctorService.getDoctorById(id);
      setFormData({
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        emailAddress: response.data.emailAddress,
        levelId: response.data.level?.levelId || '',
        specializationId: response.data.specialization?.specializationId || '',
        departmentId: response.data.department?.departmentId || '',
      });
    } catch (err) {
      setError('Failed to fetch doctor');
    } finally {
      setLoading(false);
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

    if (!formData.firstName || !formData.lastName || !formData.emailAddress) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);
      if (id) {
        await doctorService.updateDoctor(id, formData);
        setSuccess('Doctor updated successfully!');
      } else {
        await doctorService.createDoctor(formData);
        setSuccess('Doctor created successfully!');
      }
      setTimeout(() => navigate('/doctors'), 1500);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save doctor');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">
        {id ? 'Edit Doctor' : 'Add New Doctor'}
      </h1>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
      {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-semibold mb-2">First Name *</label>
            <input
              type="text"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-semibold mb-2">Last Name *</label>
            <input
              type="text"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              className="w-full px-4 py-2 border rounded-lg"
              required
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-semibold mb-2">Email *</label>
          <input
            type="email"
            name="emailAddress"
            value={formData.emailAddress}
            onChange={handleChange}
            className="w-full px-4 py-2 border rounded-lg"
            required
          />
        </div>

        <div className="grid grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-semibold mb-2">Level</label>
            <input
              type="number"
              name="levelId"
              value={formData.levelId}
              onChange={handleChange}
              placeholder="Level ID"
              className="w-full px-4 py-2 border rounded-lg"
            />
          </div>
          <div>
            <label className="block text-sm font-semibold mb-2">Specialization</label>
            <input
              type="number"
              name="specializationId"
              value={formData.specializationId}
              onChange={handleChange}
              placeholder="Spec. ID"
              className="w-full px-4 py-2 border rounded-lg"
            />
          </div>
          <div>
            <label className="block text-sm font-semibold mb-2">Department</label>
            <input
              type="number"
              name="departmentId"
              value={formData.departmentId}
              onChange={handleChange}
              placeholder="Dept. ID"
              className="w-full px-4 py-2 border rounded-lg"
            />
          </div>
        </div>

        <div className="flex gap-4 pt-4">
          <button
            type="submit"
            disabled={loading}
            className="bg-purple-600 text-white px-6 py-2 rounded hover:bg-purple-700 disabled:opacity-50"
          >
            {loading ? 'Saving...' : 'Save Doctor'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/doctors')}
            className="bg-gray-300 text-gray-700 px-6 py-2 rounded hover:bg-gray-400"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}

export default DoctorForm;
