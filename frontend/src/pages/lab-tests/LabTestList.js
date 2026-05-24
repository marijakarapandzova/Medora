import React, { useState, useEffect } from 'react';
import { patientService } from '../../services/patientService';
import { labService } from '../../services/labService';
import ErrorAlert from '../../components/ErrorAlert';

function LabTestList() {
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isLabTechnician = user.role === 'LAB_TECHNICIAN';

  const [patient, setPatient] = useState(null);
  const [embg, setEmbg] = useState('');
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [availableTests, setAvailableTests] = useState([]);
  const [labTestRequests, setLabTestRequests] = useState([]);
  const [labResults, setLabResults] = useState([]);
  const [pendingTests, setPendingTests] = useState([]);
  const [selectedTest, setSelectedTest] = useState(null);

  const [showRequestForm, setShowRequestForm] = useState(false);
  const [requestData, setRequestData] = useState({
    testId: '',
    testDate: new Date().toISOString().split('T')[0],
    notes: '',
  });

  const [submitFormData, setSubmitFormData] = useState({
    results: '',
    resultDate: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    if (isLabTechnician) {
      loadPendingTests();
    }
  }, [isLabTechnician]);

  const loadPendingTests = async () => {
    try {
      setLoading(true);
      const response = await labService.getPendingLabTests();
      setPendingTests(response.data || []);
    } catch (err) {
      setError('Failed to load pending lab tests');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (!embg.trim()) {
        setError('Please enter an EMBG');
        setLoading(false);
        return;
      }

      const patientResponse = await patientService.getPatientByEmbg(embg);
      setPatient(patientResponse.data);
      setSearched(true);

      // Fetch available tests
      const testsResponse = await labService.getAllLabTests();
      setAvailableTests(testsResponse.data);

      // Fetch medical record for patient
      const { medicalRecordService } = await import('../../services/medicalRecordService');
      const medicalRecordRes = await medicalRecordService.getMedicalRecordByPatientId(patientResponse.data.patientId);

      // Fetch existing test requests for patient
      const requestsResponse = await labService.getLabTestRequestsForPatient(patientResponse.data.patientId);
      setLabTestRequests(requestsResponse.data);

      // Fetch lab results for medical record
      const resultsResponse = await labService.getLabResultsForMedicalRecord(medicalRecordRes.data.recordId);
      setLabResults(resultsResponse.data);
    } catch (err) {
      setError(`Patient with EMBG ${embg} not found`);
      setPatient(null);
    } finally {
      setLoading(false);
    }
  };

  const handleRequestTest = async (e) => {
    e.preventDefault();
    setError(null);

    if (!requestData.testId) {
      setError('Please select a test');
      return;
    }

    if (!patient) {
      setError('Patient not found');
      return;
    }

    try {
      setLoading(true);

      // Get doctor ID from localStorage (set during login)
      const doctorId = localStorage.getItem('doctorId') || 1;

      const request = {
        patientId: patient.patientId,
        medicalRecordId: patient.patientId, // Assuming medical record ID matches patient ID
        doctorId: parseInt(doctorId),
        testId: parseInt(requestData.testId),
        testDate: requestData.testDate,
        notes: requestData.notes,
      };

      await labService.requestLabTest(request);

      // Refresh the test requests
      const requestsResponse = await labService.getLabTestRequestsForPatient(patient.patientId);
      setLabTestRequests(requestsResponse.data);

      // Reset form
      setRequestData({
        testId: '',
        testDate: new Date().toISOString().split('T')[0],
        notes: '',
      });
      setShowRequestForm(false);
      setError(null);
    } catch (err) {
      setError('Failed to request lab test: ' + err.response?.data?.error || err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitResult = async (e) => {
    e.preventDefault();
    setError(null);

    if (!submitFormData.results.trim()) {
      setError('Please enter test results');
      return;
    }

    if (!selectedTest) {
      setError('No test selected');
      return;
    }

    try {
      setLoading(true);

      const { medicalRecordService } = await import('../../services/medicalRecordService');
      const medicalRecordRes = await medicalRecordService.getMedicalRecordByPatientId(selectedTest.patientId);

      const resultData = {
        medicalRecordId: medicalRecordRes.data.recordId,
        testId: selectedTest.testId,
        results: submitFormData.results,
        resultDate: submitFormData.resultDate,
      };

      await labService.submitLabResult(resultData);

      // Refresh pending tests
      await loadPendingTests();

      // Reset form
      setSelectedTest(null);
      setSubmitFormData({
        results: '',
        resultDate: new Date().toISOString().split('T')[0],
      });

      // Show success message
      setError(null);
      alert('Lab result submitted successfully!');
    } catch (err) {
      setError('Failed to submit lab result: ' + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  if (isLabTechnician) {
    return (
      <div>
        <h1 className="text-3xl font-bold mb-6" style={{ color: '#7c3aed' }}>Lab Tests - Submit Results</h1>

        {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

        {selectedTest ? (
          // Submit Result Form
          <div className="bg-white rounded-lg shadow p-6 mb-6">
            <h2 className="text-xl font-bold mb-4">Submit Lab Result</h2>
            <div className="mb-4 p-4 bg-gray-50 rounded-lg">
              <p className="mb-2"><strong>Test:</strong> {selectedTest.testName}</p>
              <p className="mb-2"><strong>Patient:</strong> {selectedTest.patientName}</p>
              <p className="mb-2"><strong>Doctor:</strong> {selectedTest.doctorName}</p>
              <p className="mb-2"><strong>Test Date:</strong> {selectedTest.testDate}</p>
              {selectedTest.notes && <p><strong>Notes:</strong> {selectedTest.notes}</p>}
            </div>

            <form onSubmit={handleSubmitResult} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold mb-2">Test Results *</label>
                <textarea
                  value={submitFormData.results}
                  onChange={(e) => setSubmitFormData({ ...submitFormData, results: e.target.value })}
                  placeholder="Enter detailed test results"
                  className="w-full px-4 py-2 border rounded-lg"
                  rows="4"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-semibold mb-2">Result Date *</label>
                <input
                  type="date"
                  value={submitFormData.resultDate}
                  onChange={(e) => setSubmitFormData({ ...submitFormData, resultDate: e.target.value })}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                />
              </div>

              <div className="flex gap-4">
                <button
                  type="submit"
                  disabled={loading}
                  style={{
                    background: loading ? '#d1d5db' : '#bfdbfe',
                    color: '#1e1035',
                    padding: '8px 24px',
                    borderRadius: '6px',
                    border: 'none',
                    cursor: loading ? 'not-allowed' : 'pointer',
                    fontSize: '14px',
                    fontWeight: '400'
                  }}
                  onMouseEnter={(e) => !loading && (e.currentTarget.style.background = '#93c5fd')}
                  onMouseLeave={(e) => !loading && (e.currentTarget.style.background = '#bfdbfe')}
                >
                  {loading ? 'Submitting...' : 'Submit Result'}
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setSelectedTest(null);
                    setSubmitFormData({
                      results: '',
                      resultDate: new Date().toISOString().split('T')[0],
                    });
                  }}
                  className="bg-gray-400 text-white px-6 py-2 rounded hover:bg-gray-500"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        ) : (
          // Pending Tests List
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <div className="p-6 border-b">
              <h2 className="text-xl font-bold">Pending Lab Tests ({pendingTests.length})</h2>
            </div>

            {pendingTests.length === 0 ? (
              <div className="p-6 text-center text-gray-600">
                No pending lab tests
              </div>
            ) : (
              <table className="w-full">
                <thead className="bg-gray-100">
                  <tr>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Test</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Patient</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Doctor</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Requested</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Test Date</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Notes</th>
                    <th className="px-6 py-3 text-left text-sm font-semibold">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingTests.map((test, index) => (
                    <tr key={index} className="border-t hover:bg-gray-50">
                      <td className="px-6 py-3 font-medium">{test.testName}</td>
                      <td className="px-6 py-3">{test.patientName}</td>
                      <td className="px-6 py-3">{test.doctorName}</td>
                      <td className="px-6 py-3 text-green-600">{test.requestDate}</td>
                      <td className="px-6 py-3 text-purple-600">{test.testDate}</td>
                      <td className="px-6 py-3 text-gray-600 text-sm">{test.notes || '-'}</td>
                      <td className="px-6 py-3">
                        <button
                          onClick={() => setSelectedTest(test)}
                          style={{
                            background: '#bfdbfe',
                            color: '#1e1035',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            border: 'none',
                            cursor: 'pointer',
                            fontSize: '12px',
                            fontWeight: '400'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.background = '#93c5fd'}
                          onMouseLeave={(e) => e.currentTarget.style.background = '#bfdbfe'}
                        >
                          Submit Result
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-3xl font-bold mb-6" style={{ color: '#7c3aed' }}>Lab Tests</h1>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

      {/* Search Form */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-bold mb-4">Search Patient</h2>
        <form onSubmit={handleSearch} className="space-y-4">
          <div className="flex gap-4">
            <div className="flex-1">
              <label className="block text-sm font-semibold mb-2">Patient EMBG</label>
              <input
                type="text"
                value={embg}
                onChange={(e) => setEmbg(e.target.value)}
                placeholder="e.g., 1402994123456"
                className="w-full px-4 py-2 border rounded-lg"
              />
            </div>
            <div className="flex items-end">
              <button
                type="submit"
                disabled={loading}
                className="bg-purple-600 text-white px-8 py-2 rounded-full hover:bg-purple-700 disabled:bg-gray-400"
              >
                {loading ? 'Searching...' : 'Search'}
              </button>
            </div>
          </div>
        </form>
      </div>

      {/* Patient Lab Tests */}
      {searched && patient && (
        <div className="space-y-6">
          {/* Patient Info */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex justify-between items-start mb-4">
              <h2 className="text-2xl font-bold">{patient.firstName} {patient.lastName}</h2>
              {!showRequestForm && (
                <button
                  onClick={() => setShowRequestForm(true)}
                  className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
                >
                  Request Lab Test
                </button>
              )}
            </div>
            <div className="grid grid-cols-4 gap-4">
              <div>
                <p className="text-sm text-gray-600">EMBG</p>
                <p className="font-semibold">{patient.embg}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Blood Type</p>
                <p className="font-semibold">{patient.bloodType || 'N/A'}</p>
              </div>
              <div>
                <p className="text-sm text-gray-600">Date of Birth</p>
                <p className="font-semibold">{patient.dateOfBirth}</p>
              </div>
            </div>
          </div>

          {/* Request Lab Test Form */}
          {showRequestForm && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-xl font-bold mb-4">Request Lab Test</h3>
              <form onSubmit={handleRequestTest} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Test</label>
                    <select
                      value={requestData.testId}
                      onChange={(e) => setRequestData({ ...requestData, testId: e.target.value })}
                      className="w-full px-4 py-2 border rounded-lg"
                      required
                    >
                      <option value="">Select a test</option>
                      {availableTests.map((test) => (
                        <option key={test.testId} value={test.testId}>
                          {test.testName} (${test.cost})
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Test Date</label>
                    <input
                      type="date"
                      value={requestData.testDate}
                      onChange={(e) => setRequestData({ ...requestData, testDate: e.target.value })}
                      className="w-full px-4 py-2 border rounded-lg"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-2">Notes</label>
                  <textarea
                    value={requestData.notes}
                    onChange={(e) => setRequestData({ ...requestData, notes: e.target.value })}
                    placeholder="Additional notes for the lab technician"
                    className="w-full px-4 py-2 border rounded-lg"
                    rows="3"
                  />
                </div>
                <div className="flex gap-4">
                  <button
                    type="submit"
                    disabled={loading}
                    className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700 disabled:bg-gray-400"
                  >
                    {loading ? 'Requesting...' : 'Request Test'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowRequestForm(false)}
                    className="bg-gray-400 text-white px-6 py-2 rounded hover:bg-gray-500"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* Test Requests */}
          {labTestRequests && labTestRequests.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-xl font-bold mb-4">Lab Test Requests</h3>
              <div className="space-y-3">
                {labTestRequests.map((request) => (
                  <div key={request.testId} className="border-l-4 border-purple-500 pl-4 py-2">
                    <p className="font-semibold text-lg">{request.testName}</p>
                    <p className="text-sm text-gray-600">Requested by: {request.doctorName}</p>
                    <p className="text-sm text-gray-600">Test Date: {request.requestDate}</p>
                    {request.notes && (
                      <p className="text-sm text-gray-600">Notes: {request.notes}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Lab Test Results */}
          {labResults && labResults.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-xl font-bold mb-4">Lab Test Results</h3>
              <div className="space-y-4">
                {labResults.map((result) => (
                  <div key={result.resultId} className="border-l-4 border-green-500 pl-4 py-3 bg-green-50 rounded">
                    <p className="font-semibold text-lg text-green-700">{result.testName}</p>
                    <p className="text-sm text-gray-700 mt-2"><strong>Results:</strong> {result.results}</p>
                    <p className="text-sm text-gray-600">Result Date: {result.resultDate}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Link to submit results */}
          {labTestRequests && labTestRequests.length > 0 && (
            <div className="bg-yellow-50 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-yellow-800 mb-3">Lab Technician: Submit Test Results</h3>
              <p className="text-sm text-gray-700 mb-4">
                {labTestRequests.length} test{labTestRequests.length !== 1 ? 's' : ''} awaiting results
              </p>
              <a
                href="/lab-tests/results"
                className="inline-block bg-yellow-600 text-white px-6 py-2 rounded hover:bg-yellow-700"
              >
                Submit Lab Results
              </a>
            </div>
          )}

          {/* No requests message */}
          {!labTestRequests || labTestRequests.length === 0 && !labResults?.length && (
            <div className="bg-blue-50 rounded-lg p-6 text-center">
              <p className="text-gray-600">No lab test requests for this patient</p>
            </div>
          )}
        </div>
      )}

      {/* No search performed */}
      {!searched && (
        <div className="bg-gray-50 rounded-lg p-12 text-center">
          <p className="text-gray-600 text-lg">Enter a patient EMBG to request lab tests</p>
        </div>
      )}
    </div>
  );
}

export default LabTestList;
