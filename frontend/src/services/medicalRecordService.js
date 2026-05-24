import apiClient from './api';

const ENDPOINT = '/medical-records';

export const medicalRecordService = {
  getMedicalRecordById: (id) => apiClient.get(`${ENDPOINT}/${id}`),

  getMedicalRecordByPatientId: (patientId) => apiClient.get(`${ENDPOINT}/patient/${patientId}`),

  searchMedicalRecords: (params) => apiClient.get(`${ENDPOINT}/search`, { params }),

  getAllergiesForMedicalRecord: (medicalRecordId) => apiClient.get(`${ENDPOINT}/${medicalRecordId}/allergies`),

  getSymptomsForMedicalRecord: (medicalRecordId) => apiClient.get(`${ENDPOINT}/${medicalRecordId}/symptoms`),
};
