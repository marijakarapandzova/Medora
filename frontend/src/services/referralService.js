import apiClient from './api';

const ENDPOINT = '/referrals';

export const referralService = {
  createReferral: (referralData) => {
    return apiClient.post(ENDPOINT, {
      medicalRecordId: referralData.medicalRecordId,
      fromDoctorId: referralData.fromDoctorId,
      toDoctorId: referralData.toDoctorId,
      reason: referralData.reason,
      referralDate: referralData.referralDate,
      appointmentDate: referralData.appointmentDate,
      appointmentTime: referralData.appointmentTime,
    });
  },

  getReferralById: (id) => apiClient.get(`${ENDPOINT}/${id}`),

  getReferralsByFromDoctor: (doctorId) => apiClient.get(`${ENDPOINT}/from-doctor/${doctorId}`),

  getReferralsByToDoctor: (doctorId) => apiClient.get(`${ENDPOINT}/to-doctor/${doctorId}`),

  getReferralsByPatient: (patientId) => apiClient.get(`${ENDPOINT}/patient/${patientId}`),
};
