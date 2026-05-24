import apiClient from './api';

const ENDPOINT = '/medical-reports';

export const medicalReportService = {
  createReport: (reportData) => apiClient.post(ENDPOINT, reportData),
  getReportById: (reportId) => apiClient.get(`${ENDPOINT}/${reportId}`),
  getReportsForMedicalRecord: (medicalRecordId) => apiClient.get(`${ENDPOINT}/record/${medicalRecordId}`),
  updateReport: (reportId, description) => apiClient.put(`${ENDPOINT}/${reportId}`, { description }),
  deleteReport: (reportId) => apiClient.delete(`${ENDPOINT}/${reportId}`),
};
