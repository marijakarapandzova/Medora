import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { billingService } from '../../services/billingService';
import { patientService } from '../../services/patientService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';

function BillingList() {
  const [billings, setBillings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [searchParams] = useSearchParams();
  const patientId = searchParams.get('patientId');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  // Filters
  const [statusFilter, setStatusFilter] = useState('');
  const [patientFilter, setPatientFilter] = useState('');
  const [patients, setPatients] = useState([]);

  useEffect(() => {
    const fetchBillings = async () => {
      try {
        setLoading(true);
        let response;
        if (patientId) {
          response = await billingService.getBillingHistoryForPatient(patientId);
        } else if (user.role === 'PATIENT') {
          response = await billingService.getBillingHistoryForPatient(user.patientId);
        } else {
          response = await billingService.getAllBillings();
        }
        setBillings(response.data || []);
      } catch (err) {
        setError('Failed to fetch billing records');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchBillings();
  }, [patientId, user.role, user.patientId]);

  // Load patients for filter dropdown (for billing admin)
  useEffect(() => {
    if (user.role === 'BILLING_ADMIN') {
      const loadPatients = async () => {
        try {
          const response = await patientService.getAllPatients();
          setPatients(response.data || []);
        } catch (err) {
          console.error('Failed to load patients:', err);
        }
      };
      loadPatients();
    }
  }, [user.role]);

  // Filter billings based on selected filters
  const filteredBillings = billings.filter(billing => {
    if (statusFilter && billing.paymentStatus !== statusFilter) {
      return false;
    }
    if (patientFilter && billing.patientName !== patientFilter) {
      return false;
    }
    return true;
  });

  const pendingBillings = billings.filter(b => b.paymentStatus === 'PENDING');

  const handleUpdateStatus = async (billId, newStatus) => {
    try {
      const updateData = {
        paymentStatus: newStatus
      };
      if (newStatus === 'PAID') {
        updateData.paymentDate = new Date().toISOString().split('T')[0];
      }

      console.log('Updating billing status:', { billId, updateData });
      await billingService.updatePaymentStatus(billId, updateData);
      setSuccess(`Billing status updated to ${newStatus}`);

      // Refresh the list
      const response = user.role === 'BILLING_ADMIN'
          ? await billingService.getAllBillings()
          : await billingService.getBillingHistoryForPatient(patientId || user.patientId);
      setBillings(response.data || []);
    } catch (err) {
      setError(`Failed to update billing status: ${err.response?.data?.error || err.message}`);
      console.error('Update status error:', err);
    }
  };

  // Prevent unauthorized access to billing
  if (user.role === 'DOCTOR' || user.role === 'LAB_TECHNICIAN') {
    return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
          <h1 className="text-2xl font-bold" style={{ color: '#7c3aed', marginBottom: '10px' }}>Access Denied</h1>
          <p style={{ color: 'var(--color-neutral-600)' }}>You do not have permission to access billing records.</p>
        </div>
    );
  }

  if (loading) return <Loading />;

  return (
      <div>
        <h1 className="text-3xl font-bold mb-6" style={{ color: '#7c3aed' }}>{patientId ? 'Patient Billing History' : 'Billing Records'}</h1>

        {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
        {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

        {/* Filters for Billing Admin */}
        {user.role === 'BILLING_ADMIN' && (
            <div className="bg-white rounded-lg shadow p-6 mb-6">
              <h2 className="text-lg font-bold mb-4">Filters</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-semibold mb-2">Status</label>
                  <select
                      value={statusFilter}
                      onChange={(e) => setStatusFilter(e.target.value)}
                      className="w-full px-4 py-2 border rounded-lg"
                  >
                    <option value="">All Statuses</option>
                    <option value="PENDING">Pending</option>
                    <option value="PAID">Paid</option>
                    <option value="PROCESSING">Processing</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-2">Patient</label>
                  <select
                      value={patientFilter}
                      onChange={(e) => setPatientFilter(e.target.value)}
                      className="w-full px-4 py-2 border rounded-lg"
                  >
                    <option value="">All Patients</option>
                    {patients.map(p => (
                        <option key={p.patientId} value={`${p.firstName} ${p.lastName}`}>
                          {p.firstName} {p.lastName}
                        </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
        )}

        {/* Pending Billing Records Section (for Billing Admin) */}
        {user.role === 'BILLING_ADMIN' && pendingBillings.length > 0 && (
            <div className="bg-yellow-50 rounded-lg shadow p-6 mb-6 border-l-4 border-yellow-500">
              <h2 className="text-lg font-bold mb-4">Pending Billing Records ({pendingBillings.length})</h2>
              <div className="space-y-3">
                {pendingBillings.map(billing => (
                    <div key={billing.billId} className="bg-white rounded p-4 flex justify-between items-center">
                      <div>
                        <p className="font-semibold">{billing.patientName}</p>
                        <p className="text-sm text-gray-600">${billing.totalCost}</p>
                      </div>
                      <button
                          onClick={() => handleUpdateStatus(billing.billId, 'PAID')}
                          className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 text-sm"
                      >
                        Mark Paid
                      </button>
                    </div>
                ))}
              </div>
            </div>
        )}

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-100">
            <tr>
              <th className="px-6 py-3 text-left text-sm font-semibold">Patient</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Total Cost</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Payment Status</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Payment Date</th>
              <th className="px-6 py-3 text-left text-sm font-semibold">Actions</th>
            </tr>
            </thead>
            <tbody>
            {filteredBillings.map(billing => (
                <tr key={billing.billId} className="border-t hover:bg-gray-50">
                  <td className="px-6 py-3">{billing.patientName}</td>
                  <td className="px-6 py-3">${billing.totalCost}</td>
                  <td className="px-6 py-3">
                  <span className={`px-3 py-1 rounded text-sm font-semibold ${
                      billing.paymentStatus === 'PENDING' ? 'bg-yellow-100 text-yellow-800' :
                          billing.paymentStatus === 'PAID' ? 'bg-green-100 text-green-800' :
                              'bg-red-100 text-red-800'
                  }`}>
                    {billing.paymentStatus}
                  </span>
                  </td>
                  <td className="px-6 py-3">{billing.paymentDate || 'Not paid'}</td>
                  <td className="px-6 py-3">
                    <Link to={`/billing/${billing.billId}`} style={{ color: '#7c3aed', textDecoration: 'none', fontWeight: '400' }} onMouseEnter={(e) => e.currentTarget.style.textDecoration = 'underline'} onMouseLeave={(e) => e.currentTarget.style.textDecoration = 'none'}>
                      View
                    </Link>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>

          {filteredBillings.length === 0 && (
              <div className="p-6 text-center text-gray-500">
                {billings.length === 0 ? 'No billing records found' : 'No records match the selected filters'}
              </div>
          )}
        </div>
      </div>
  );
}

export default BillingList;
