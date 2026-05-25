import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { appointmentService } from '../../services/appointmentService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function AppointmentList() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchParams] = useSearchParams();
  const doctorId = searchParams.get('doctorId');
  const patientId = searchParams.get('patientId');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    fetchAppointments();
  }, [doctorId, patientId]);

  const fetchAppointments = async () => {
    try {
      setLoading(true);
      let response;
      if (patientId) {
        response = await appointmentService.getAppointmentsForPatient(patientId);
      } else if (doctorId) {
        response = await appointmentService.getAppointmentsForDoctor(doctorId);
      } else if (user.role === 'PATIENT') {
        response = await appointmentService.getAppointmentsForPatient(user.patientId);
      } else if (user.role === 'DOCTOR') {
        response = await appointmentService.getAppointmentsForDoctor(user.doctorId);
      } else {
        response = await appointmentService.getAllAppointments();
      }
      setAppointments(response.data);
    } catch (err) {
      setError('Failed to fetch appointments');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelAppointment = async (id) => {
    if (window.confirm('Are you sure you want to cancel this appointment?')) {
      try {
        await appointmentService.cancelAppointment(id);
        setAppointments(appointments.map(apt =>
            apt.appointmentId === id ? { ...apt, status: 'CANCELLED' } : apt
        ));
      } catch (err) {
        setError('Failed to cancel appointment');
      }
    }
  };

  if (loading) return <Loading />;

  return (
      <div>
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold" style={{ color: '#7c3aed' }}>
            {patientId ? 'Patient Appointments' : doctorId ? 'My Appointments' : 'All Appointments'}
          </h1>
          <Link to="/appointments/new" style={{
            display: 'inline-block',
            background: '#bfdbfe',
            color: '#1e1035',
            padding: '8px 16px',
            borderRadius: '6px',
            textDecoration: 'none',
            fontSize: '14px',
            fontWeight: '400'
          }} onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'} onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}>
            New Appointment
          </Link>
        </div>

        {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-100">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-semibold">Patient</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Doctor</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Date</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Time</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Status</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Actions</th>
            </tr>
            </thead>
            <tbody>
            {appointments.map(appointment => (
                <tr key={appointment.appointmentId} className="border-t hover:bg-gray-50">
                  <td className="px-6 py-3">{appointment.patient?.firstName} {appointment.patient?.lastName}</td>
                  <td className="px-6 py-3">Dr. {appointment.doctor?.firstName} {appointment.doctor?.lastName}</td>
                  <td className="px-6 py-3">{appointment.appointmentDate}</td>
                  <td className="px-6 py-3">{appointment.appointmentTime}</td>
                  <td className="px-6 py-3">
                  <span className={`px-3 py-1 rounded text-sm font-semibold ${
                      appointment.status === 'SCHEDULED' ? 'bg-purple-100 text-purple-800' :
                          appointment.status === 'COMPLETED' ? 'bg-green-100 text-green-800' :
                              'bg-red-100 text-red-800'
                  }`}>
                    {appointment.status}
                  </span>
                  </td>
                  <td className="px-6 py-3">
                    {appointment.status === 'SCHEDULED' && (
                        (user.role === 'DOCTOR' && appointment.doctor?.doctorId !== user.doctorId) ? null : (
                            <button
                                onClick={() => handleCancelAppointment(appointment.appointmentId)}
                                className="text-red-600 hover:underline px-3 py-2 text-sm font-medium"
                            >
                              Cancel
                            </button>
                        )
                    )}
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>
      </div>
  );
}

export default AppointmentList;
