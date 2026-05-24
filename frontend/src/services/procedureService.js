import api from './api';

const procedureService = {
  // Get all available procedures
  getAllProcedures: () => {
    return api.get('/performed-procedures/available');
  },

  // Request a procedure for a patient
  requestProcedure: (requestData) => {
    return api.post('/performed-procedures/request', requestData);
  },

  // Get performed procedures for a patient
  getPerformedProceduresForPatient: (patientId) => {
    return api.get(`/performed-procedures/patient/${patientId}`);
  },

  // Get procedures for a medical record
  getProceduresForMedicalRecord: (medicalRecordId) => {
    return api.get(`/performed-procedures/medical-record/${medicalRecordId}`);
  },

  // Record a procedure (UC016)
  recordProcedure: (procedureData) => {
    return api.post('/performed-procedures/record', null, {
      params: {
        procedureId: procedureData.procedureId,
        doctorId: procedureData.doctorId,
        patientId: procedureData.patientId,
        diagnosisId: procedureData.diagnosisId,
        procedureDate: procedureData.procedureDate
      }
    });
  },

  // Submit procedure result (UC017)
  submitProcedureResult: (resultData) => {
    return api.post('/performed-procedures/results', resultData);
  },

  // Get procedure results for a medical record
  getProcedureResultsForMedicalRecord: (medicalRecordId) => {
    return api.get(`/performed-procedures/results/medical-record/${medicalRecordId}`);
  },

  // Record procedure outcome (older endpoint)
  recordProcedureOutcome: (procedureId, notes) => {
    return api.patch(`/performed-procedures/${procedureId}/outcome`, null, {
      params: { notes }
    });
  }
};

export { procedureService };
