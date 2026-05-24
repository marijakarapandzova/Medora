import apiClient from './api';

const ENDPOINT = '/doctors';

export const doctorService = {
  getAllDoctors: () => apiClient.get(ENDPOINT),

  getDoctorById: (id) => apiClient.get(`${ENDPOINT}/${id}`),

  getDoctorByEmail: (email) => apiClient.get(`${ENDPOINT}/email/${email}`),

  getDoctorsByDepartment: (departmentId) => apiClient.get(`${ENDPOINT}/department/${departmentId}`),

  getDoctorsBySpecialization: (specializationId) => apiClient.get(`${ENDPOINT}/specialization/${specializationId}`),

  getDoctorsByLevel: (levelId) => apiClient.get(`${ENDPOINT}/level/${levelId}`),

  createDoctor: (doctor) => apiClient.post(ENDPOINT, doctor),

  updateDoctor: (id, doctor) => apiClient.put(`${ENDPOINT}/${id}`, doctor),
};
