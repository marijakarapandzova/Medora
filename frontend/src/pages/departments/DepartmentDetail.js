import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { departmentService } from '../../services/departmentService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function DepartmentDetail() {
  const formatDepartmentName = (name) => {
    if (!name) return 'N/A';
    return name.replace(/_DEPT$/, '').replace(/_/g, ' ').split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()).join(' ');
  };

  const { departmentId } = useParams();
  const navigate = useNavigate();
  const [department, setDepartment] = useState(null);
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDepartmentAndDoctors();
  }, [departmentId]);

  const fetchDepartmentAndDoctors = async () => {
    try {
      setLoading(true);

      // Fetch department details
      const deptResponse = await departmentService.getDepartmentById(departmentId);
      setDepartment(deptResponse.data);

      // Fetch doctors for this department
      const doctorsResponse = await departmentService.getDoctorsByDepartment(departmentId);
      setDoctors(doctorsResponse.data);
    } catch (err) {
      setError('Failed to fetch data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <Loading />;

  if (!department) {
    return (
      <div>
        <ErrorAlert message="Department not found" onClose={() => navigate('/departments')} />
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold" style={{ color: '#7c3aed' }}>{formatDepartmentName(department.departmentName)}</h1>
        <button
          onClick={() => navigate('/departments')}
          className="bg-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-400"
        >
          Back
        </button>
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4">Department Information</h2>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="font-semibold text-gray-600">Department ID:</span>
              <span className="text-gray-800">{department.departmentId}</span>
            </div>
            <div className="flex justify-between">
              <span className="font-semibold text-gray-600">Name:</span>
              <span className="text-gray-800">{formatDepartmentName(department.departmentName)}</span>
            </div>
            <div className="flex justify-between">
              <span className="font-semibold text-gray-600">Total Doctors:</span>
              <span className="text-gray-800 font-bold">{doctors.length}</span>
            </div>
          </div>
        </div>

        <div className="bg-blue-50 rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4">Quick Actions</h2>
          <div className="space-y-2">
            <button
              onClick={() => navigate(`/departments/${departmentId}/doctors`)}
              className="w-full bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"
            >
              View All Doctors
            </button>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow p-6 mt-6">
        <h2 className="text-xl font-bold mb-4">Assigned Doctors</h2>
        {doctors.length > 0 ? (
          <table className="w-full">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-4 py-2 text-left">Name</th>
                <th className="px-4 py-2 text-left">Email</th>
                <th className="px-4 py-2 text-left">Actions</th>
              </tr>
            </thead>
            <tbody>
              {doctors.map((doctor) => (
                <tr key={doctor.doctorId} className="border-t hover:bg-gray-50">
                  <td className="px-4 py-2">
                    {doctor.firstName} {doctor.lastName}
                  </td>
                  <td className="px-4 py-2">{doctor.emailAddress}</td>
                  <td className="px-4 py-2">
                    <button
                      onClick={() => navigate(`/doctors/${doctor.doctorId}`)}
                      className="text-purple-600 hover:underline text-sm"
                    >
                      View Profile
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="text-gray-500 text-center py-4">No doctors assigned to this department</p>
        )}
      </div>
    </div>
  );
}

export default DepartmentDetail;
