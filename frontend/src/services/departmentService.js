import apiClient from './api';

const ENDPOINT = '/departments';

export const departmentService = {
  getAllDepartments: () => apiClient.get(ENDPOINT),

  getDepartmentById: (id) => apiClient.get(`${ENDPOINT}/${id}`),

  getDoctorsByDepartment: (departmentId) => apiClient.get(`${ENDPOINT}/${departmentId}/doctors`),

  createDepartment: (departmentData) => apiClient.post(ENDPOINT, departmentData),

  updateDepartment: (id, departmentData) => apiClient.put(`${ENDPOINT}/${id}`, departmentData),
};