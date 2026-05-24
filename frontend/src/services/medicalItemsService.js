import apiClient from './api';

export const medicalItemsService = {
  getDiagnosesForPatient: (patientId) => apiClient.get(`/diagnoses/patient/${patientId}`),

  getPrescriptionsForMedicalRecord: (medicalRecordId) => apiClient.get(`/prescriptions/medical-record/${medicalRecordId}`),

  getAllergiesForMedicalRecord: (medicalRecordId) => apiClient.get(`/medical-records/${medicalRecordId}/allergies`),

  getSymptomsForMedicalRecord: (medicalRecordId) => apiClient.get(`/medical-records/${medicalRecordId}/symptoms`),
};
