import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { billingService } from '../../services/billingService';
import Loading from '../../components/Loading';
import ErrorAlert from '../../components/ErrorAlert';

function BillingList() {
  const [billings, setBillings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchParams] = useSearchParams();
  const patientId = searchParams.get('patientId');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    fetchBillings();
  }, [patientId]);

  const fetchBillings = async () => {
    try {
      setLoading(true);
      let response;
      if (patientId) {
        response = await billingService.getBillingHistoryForPatient(patientId);
      } else if (user.role === 'PATIENT') {
        // Patients can only view their own billing history
        response = await billingService.getBillingHistoryForPatient(user.patientId);
      } else {
        response = await billingService.getAllBillings();
      }
      setBillings(response.data);
    } catch (err) {
      setError('Failed to fetch billing records');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <Loading />;

  return (
    <div>
      <h1 className="text-3xl font-bold mb-6" style={{ color: '#7c3aed' }}>{patientId ? 'Patient Billing History' : 'Billing Records'}</h1>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

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
            {billings.map(billing => (
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
      </div>
    </div>
  );
}

export default BillingList;
