import api from './api';

const labService = {
  // Get all lab tests
  getAllLabTests: () => {
    return api.get('/lab-tests');
  },

  // Get a specific lab test
  getLabTestById: (testId) => {
    return api.get(`/lab-tests/${testId}`);
  },

  // Create a new lab test definition
  createLabTest: (testData) => {
    return api.post('/lab-tests', testData);
  },

  // Update a lab test
  updateLabTest: (testId, testData) => {
    return api.put(`/lab-tests/${testId}`, testData);
  },

  // Request a lab test for a patient (UC013)
  requestLabTest: (requestData) => {
    return api.post('/lab-tests/request', requestData);
  },

  // Get lab test requests for a patient
  getLabTestRequestsForPatient: (patientId) => {
    return api.get(`/lab-tests/requests/patient/${patientId}`);
  },

  // Get lab test requests by doctor
  getLabTestRequestsByDoctor: (doctorId) => {
    return api.get(`/lab-tests/requests/doctor/${doctorId}`);
  },

  // Submit lab results (UC014)
  submitLabResult: (resultData) => {
    return api.post('/lab-tests/results', resultData);
  },

  // Get lab results for a medical record
  getLabResultsForMedicalRecord: (medicalRecordId) => {
    return api.get(`/lab-tests/results/medical-record/${medicalRecordId}`);
  },

  // Get pending lab tests (for lab technicians)
  getPendingLabTests: () => {
    return api.get('/lab-tests/requests/pending');
  },
};

export { labService };
