import apiClient from './api';

const ENDPOINT = '/billing';

export const billingService = {
  getAllBillings: () => apiClient.get(ENDPOINT),

  getBillingById: (id) => apiClient.get(`${ENDPOINT}/${id}`),

  getBillingHistoryForPatient: (patientId) => apiClient.get(`${ENDPOINT}/patient/${patientId}`),

  getBillingDetail: (id) => apiClient.get(`${ENDPOINT}/${id}/detail`),

  generateBillingRecord: (billing) => apiClient.post(ENDPOINT, billing),

  updatePaymentStatus: (id, paymentData) => apiClient.patch(`${ENDPOINT}/${id}/payment-status`, paymentData),

  downloadInvoicePDF: async (billId) => {
    try {
      const response = await apiClient.get(`${ENDPOINT}/${billId}/invoice-pdf`, {
        responseType: 'blob'
      });
      // Create a blob URL and download
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `invoice-${billId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      throw error;
    }
  },
};
