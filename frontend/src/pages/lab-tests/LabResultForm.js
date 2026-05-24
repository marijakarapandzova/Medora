import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { labService } from '../../services/labService';
import { patientService } from '../../services/patientService';
import { medicalRecordService } from '../../services/medicalRecordService';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';

function LabResultForm() {
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searchEmbg, setSearchEmbg] = useState('');
  const [patient, setPatient] = useState(null);
  const [pendingTests, setPendingTests] = useState([]);
  const [selectedTest, setSelectedTest] = useState(null);
  const [medicalRecordId, setMedicalRecordId] = useState(null);

  const [formData, setFormData] = useState({
    medicalRecordId: '',
    testId: '',
    results: '',
    resultDate: new Date().toISOString().split('T')[0],
  });

  const handleSearch = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (!searchEmbg.trim()) {
        setError('Please enter patient EMBG');
        setLoading(false);
        return;
      }

      const patientRes = await patientService.getPatientByEmbg(searchEmbg);
      setPatient(patientRes.data);

      // Get medical record
      const medicalRecordRes = await medicalRecordService.getMedicalRecordByPatientId(patientRes.data.patientId);
      setMedicalRecordId(medicalRecordRes.data.recordId);

      // Get pending tests
      const testsRes = await labService.getLabTestRequestsForPatient(patientRes.data.patientId);
      setPendingTests(testsRes.data || []);

      setFormData(prev => ({
        ...prev,
        medicalRecordId: medicalRecordRes.data.recordId
      }));
    } catch (err) {
      setError('Patient not found or error loading tests');
      setPatient(null);
      setPendingTests([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectTest = (test) => {
    setSelectedTest(test);
    setFormData(prev => ({
      ...prev,
      testId: test.testId,
      medicalRecordId: medicalRecordId || prev.medicalRecordId
    }));
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!formData.medicalRecordId || !formData.testId || !formData.results) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);

      const submitData = {
        medicalRecordId: parseInt(formData.medicalRecordId),
        testId: parseInt(formData.testId),
        results: formData.results,
        resultDate: formData.resultDate,
      };

      await labService.submitLabResult(submitData);

      setSuccess('Lab result submitted successfully!');
      setFormData({
        medicalRecordId: '',
        testId: '',
        results: '',
        resultDate: new Date().toISOString().split('T')[0],
      });
      setSelectedTest(null);
      setPatient(null);
      setSearchEmbg('');
      setPendingTests([]);

      // Navigate back to lab tests page after a short delay
      setTimeout(() => navigate('/lab-tests'), 2000);
    } catch (err) {
      setError('Failed to submit lab result: ' + (err.response?.data?.error || err.message));
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Submit Lab Result</h1>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
      {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

      {/* Search Patient Form */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-bold mb-4">Search Patient</h2>
        <form onSubmit={handleSearch} className="flex gap-4">
          <input
            type="text"
            value={searchEmbg}
            onChange={(e) => setSearchEmbg(e.target.value)}
            placeholder="Enter patient EMBG"
            className="flex-1 px-4 py-2 border rounded-lg"
          />
          <button
            type="submit"
            disabled={loading}
            className="bg-purple-600 text-white px-8 py-2 rounded hover:bg-purple-700 disabled:bg-gray-400"
          >
            {loading ? 'Searching...' : 'Search'}
          </button>
        </form>
      </div>

      {/* Patient Info and Pending Tests */}
      {patient && (
        <div className="space-y-6">
          {/* Patient Card */}
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-2xl font-bold mb-4">{patient.firstName} {patient.lastName}</h2>
            <div className="grid grid-cols-3 gap-4">
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

          {/* Pending Tests List */}
          {pendingTests.length > 0 ? (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-bold mb-4">Pending Lab Tests</h2>
              <div className="space-y-2">
                {pendingTests.map((test) => (
                  <div
                    key={test.testId}
                    onClick={() => handleSelectTest(test)}
                    className={`p-4 rounded-lg border-2 cursor-pointer transition ${
                      selectedTest?.testId === test.testId
                        ? 'border-purple-600 bg-blue-50'
                        : 'border-gray-200 bg-gray-50 hover:border-blue-400'
                    }`}
                  >
                    <p className="font-semibold text-lg">{test.testName}</p>
                    <p className="text-sm text-gray-600">Requested by: {test.doctorName}</p>
                    <p className="text-sm text-gray-600">Request Date: {test.requestDate}</p>
                    {test.notes && (
                      <p className="text-sm text-gray-600 mt-1">Notes: {test.notes}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="bg-blue-50 rounded-lg p-6 text-center">
              <p className="text-gray-600">No pending lab tests for this patient</p>
            </div>
          )}

          {/* Result Submission Form */}
          {selectedTest && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-bold mb-4">Submit Results for {selectedTest.testName}</h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Test ID</label>
                    <input
                      type="number"
                      value={formData.testId}
                      disabled
                      className="w-full px-4 py-2 border rounded-lg bg-gray-100"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Medical Record ID</label>
                    <input
                      type="number"
                      value={formData.medicalRecordId}
                      disabled
                      className="w-full px-4 py-2 border rounded-lg bg-gray-100"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Results *</label>
                  <textarea
                    name="results"
                    value={formData.results}
                    onChange={handleChange}
                    placeholder="Enter the lab test results"
                    className="w-full px-4 py-2 border rounded-lg"
                    rows="6"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-semibold mb-2">Result Date</label>
                  <input
                    type="date"
                    name="resultDate"
                    value={formData.resultDate}
                    onChange={handleChange}
                    className="w-full px-4 py-2 border rounded-lg"
                  />
                </div>

                <div className="flex gap-4 pt-6">
                  <button
                    type="submit"
                    disabled={loading}
                    style={{
                      flex: 1,
                      background: loading ? '#d1d5db' : '#bfdbfe',
                      color: '#1e1035',
                      padding: '8px 12px',
                      borderRadius: '6px',
                      border: 'none',
                      cursor: loading ? 'not-allowed' : 'pointer',
                      fontWeight: '400',
                      fontSize: '13px'
                    }}
                    onMouseEnter={(e) => !loading && (e.currentTarget.style.background = '#93c5fd')}
                    onMouseLeave={(e) => !loading && (e.currentTarget.style.background = '#bfdbfe')}
                  >
                    {loading ? 'Submitting...' : 'Submit Result'}
                  </button>
                  <button
                    type="button"
                    onClick={() => navigate('/lab-tests')}
                    className="flex-1 bg-gray-400 text-white py-2 rounded-lg hover:bg-gray-500"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}

      {/* No search performed */}
      {!patient && (
        <div className="bg-gray-50 rounded-lg p-12 text-center">
          <p className="text-gray-600 text-lg">Enter a patient EMBG to view pending lab tests</p>
        </div>
      )}
    </div>
  );
}

export default LabResultForm;
