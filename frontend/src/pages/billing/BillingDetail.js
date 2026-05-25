import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { billingService } from '../../services/billingService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';

function BillingDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isPatient = user.role === 'PATIENT';

  const [billing, setBilling] = useState(null);
  const [billingDetail, setBillingDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [updating, setUpdating] = useState(false);
  const [paymentStatus, setPaymentStatus] = useState('');
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    fetchBilling();
  }, [id]);

  const fetchBilling = async () => {
    try {
      setLoading(true);
      const response = await billingService.getBillingById(id);
      setBilling(response.data);
      setPaymentStatus(response.data.paymentStatus);

      // Fetch detailed billing information
      try {
        const detailResponse = await billingService.getBillingDetail(id);
        setBillingDetail(detailResponse.data);
      } catch (err) {
        console.error('Could not fetch billing details:', err);
      }
    } catch (err) {
      setError('Failed to fetch billing record');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdatePaymentStatus = async () => {
    try {
      setUpdating(true);
      const response = await billingService.updatePaymentStatus(id, {
        paymentStatus,
        paymentDate: new Date().toISOString().split('T')[0],
      });
      setBilling(response.data);
      setSuccess('Payment status updated successfully!');
    } catch (err) {
      setError('Failed to update payment status');
    } finally {
      setUpdating(false);
    }
  };

  const handleDownloadInvoice = async () => {
    try {
      setDownloading(true);
      await billingService.downloadInvoicePDF(id);
      setSuccess('Invoice downloaded successfully!');
    } catch (err) {
      setError('Failed to download invoice');
      console.error(err);
    } finally {
      setDownloading(false);
    }
  };

  if (loading) return <Loading />;

  // Prevent unauthorized access to billing
  if (user.role === 'DOCTOR' || user.role === 'LAB_TECHNICIAN') {
    return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
          <h1 className="text-2xl font-bold" style={{ color: '#7c3aed', marginBottom: '10px' }}>Access Denied</h1>
          <p style={{ color: 'var(--color-neutral-600)' }}>You do not have permission to access billing records.</p>
        </div>
    );
  }

  if (!billing) {
    return (
        <div>
          <ErrorAlert message="Billing record not found" onClose={() => navigate('/billing')} />
        </div>
    );
  }

  return (
      <div>
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold" style={{ color: '#7c3aed' }}>Billing Record #{billing.billId}</h1>
          <div className="space-x-2">
            <button
                onClick={handleDownloadInvoice}
                disabled={downloading}
                className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 disabled:opacity-50"
            >
              {downloading ? 'Downloading...' : 'Download Invoice (PDF)'}
            </button>
            <button onClick={() => navigate('/billing')} className="bg-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-400">
              Back
            </button>
          </div>
        </div>

        {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
        {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

        <div className={`grid gap-6 mb-6 ${isPatient ? 'grid-cols-1 lg:grid-cols-2' : 'grid-cols-1 lg:grid-cols-3'}`}>
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-bold mb-4">Billing Information</h2>
            <div className="space-y-3">
              <InfoRow label="Patient" value={billing.patientName} />
              <InfoRow label="Total Cost" value={`$${billing.totalCost}`} />
              <InfoRow label="Current Status" value={billing.paymentStatus} />
              <InfoRow label="Payment Date" value={billing.paymentDate || 'Not paid'} />
            </div>
          </div>

          {!isPatient && (
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-bold mb-4">Update Payment Status</h2>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Payment Status</label>
                    <select
                        value={paymentStatus}
                        onChange={(e) => setPaymentStatus(e.target.value)}
                        className="w-full px-4 py-2 border rounded-lg"
                    >
                      <option value="PENDING">Pending</option>
                      <option value="PAID">Paid</option>
                      <option value="CANCELLED">Cancelled</option>
                    </select>
                  </div>
                  <button
                      onClick={handleUpdatePaymentStatus}
                      disabled={updating || paymentStatus === billing.paymentStatus}
                      className="w-full bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 disabled:opacity-50"
                  >
                    {updating ? 'Updating...' : 'Update Status'}
                  </button>
                </div>
              </div>
          )}

          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-bold mb-4">Patient Details</h2>
            <div className="space-y-3">
              {billingDetail && (
                  <>
                    <InfoRow label="EMBG" value={billingDetail.patientEmbg} />
                    <InfoRow label="Phone" value={billingDetail.patientPhone} />
                    <InfoRow label="Bill Date" value={billingDetail.billDate} />
                  </>
              )}
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4">Itemized Services</h2>

          {billingDetail && (billingDetail.procedures.length > 0 || billingDetail.labTests.length > 0) ? (
              <div className="space-y-4">
                {billingDetail.procedures.length > 0 && (
                    <div>
                      <h3 className="font-semibold text-lg mb-2">Procedures</h3>
                      <table className="w-full">
                        <thead className="bg-gray-100">
                        <tr>
                          <th className="px-4 py-2 text-left">Description</th>
                          <th className="px-4 py-2 text-right">Cost</th>
                        </tr>
                        </thead>
                        <tbody>
                        {billingDetail.procedures.map((proc, idx) => (
                            <tr key={idx} className="border-t">
                              <td className="px-4 py-2">{proc.description}</td>
                              <td className="px-4 py-2 text-right">${proc.cost}</td>
                            </tr>
                        ))}
                        </tbody>
                      </table>
                    </div>
                )}

                {billingDetail.labTests.length > 0 && (
                    <div>
                      <h3 className="font-semibold text-lg mb-2">Lab Tests</h3>
                      <table className="w-full">
                        <thead className="bg-gray-100">
                        <tr>
                          <th className="px-4 py-2 text-left">Description</th>
                          <th className="px-4 py-2 text-right">Cost</th>
                        </tr>
                        </thead>
                        <tbody>
                        {billingDetail.labTests.map((test, idx) => (
                            <tr key={idx} className="border-t">
                              <td className="px-4 py-2">{test.description}</td>
                              <td className="px-4 py-2 text-right">${test.cost}</td>
                            </tr>
                        ))}
                        </tbody>
                      </table>
                    </div>
                )}

                <div className="border-t-2 pt-4 mt-4 flex justify-end">
                  <div className="text-right">
                    <p className="text-gray-600">Total Amount:</p>
                    <p className="text-2xl font-bold text-purple-600">${billing.totalCost}</p>
                  </div>
                </div>
              </div>
          ) : (
              <p className="text-gray-500">No services itemized for this billing record.</p>
          )}
        </div>
      </div>
  );
}

function InfoRow({ label, value }) {
  return (
      <div className="flex justify-between">
        <span className="text-gray-800">{label}:</span>
        <span className="text-gray-800">{value || 'N/A'}</span>
      </div>
  );
}

export default BillingDetail;
