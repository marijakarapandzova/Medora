import apiClient from './api';

const ENDPOINT = '/appointments';

export const appointmentService = {
  getAllAppointments: () => apiClient.get(ENDPOINT),

  getAppointmentById: (id) => apiClient.get(`${ENDPOINT}/${id}`),

  getAppointmentsForPatient: (patientId) => apiClient.get(`${ENDPOINT}/patient/${patientId}`),

  getAppointmentsForDoctor: (doctorId) => apiClient.get(`${ENDPOINT}/doctor/${doctorId}`),

  getDoctorSchedule: (doctorId, date) => apiClient.get(`${ENDPOINT}/doctor/${doctorId}/schedule`, {
    params: { date }
  }),

  createAppointment: (appointment) => apiClient.post(ENDPOINT, appointment),

  cancelAppointment: (id) => apiClient.patch(`${ENDPOINT}/${id}/cancel`),

  completeAppointment: (id) => apiClient.patch(`${ENDPOINT}/${id}/complete`),
};
