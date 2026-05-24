import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { departmentService } from '../../services/departmentService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function DoctorsByDepartment() {
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
        <div>
          <h1 className="text-3xl font-bold" style={{ color: '#7c3aed' }}>{formatDepartmentName(department.departmentName)}</h1>
          <p className="text-gray-600">Doctors in this department: {doctors.length}</p>
        </div>
        <button
          onClick={() => navigate('/departments')}
          className="bg-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-400"
        >
          Back
        </button>
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      <div className="bg-white rounded-lg shadow overflow-hidden">
        {doctors.length > 0 ? (
          <table className="w-full">
            <thead className="bg-gray-100">
              <tr>
                <th className="px-6 py-3 text-left text-sm font-semibold">Name</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Email</th>
                <th className="px-6 py-3 text-left text-sm font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody>
              {doctors.map((doctor) => (
                <tr key={doctor.doctorId} className="border-t hover:bg-gray-50">
                  <td className="px-6 py-3">
                    {doctor.firstName} {doctor.lastName}
                  </td>
                  <td className="px-6 py-3">{doctor.emailAddress}</td>
                  <td className="px-6 py-3">
                    <button
                      onClick={() => navigate(`/doctors/${doctor.doctorId}`)}
                      className="text-purple-600 hover:underline"
                    >
                      View Profile
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="p-6 text-center">
            <p className="text-gray-500">No doctors assigned to this department</p>
          </div>
        )}
      </div>
    </div>
  );
}

export default DoctorsByDepartment;