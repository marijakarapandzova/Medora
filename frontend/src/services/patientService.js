import apiClient from './api';

const ENDPOINT = '/patients';

export const patientService = {
  getAllPatients: () => apiClient.get(ENDPOINT),

  getPatientById: (id) => apiClient.get(`${ENDPOINT}/${id}`),

  getPatientByEmbg: (embg) => apiClient.get(`${ENDPOINT}/embg/${embg}`),

  getPatientByEmail: (email) => apiClient.get(`${ENDPOINT}/email/${email}`),

  createPatient: (patient) => apiClient.post(ENDPOINT, patient),

  updatePatient: (id, patient) => apiClient.put(`${ENDPOINT}/${id}`, patient),
};
