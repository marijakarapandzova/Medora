import React, { useState, useEffect } from 'react';
import { patientService } from '../../services/patientService';
import { procedureService } from '../../services/procedureService';
import { medicalRecordService } from '../../services/medicalRecordService';
import ErrorAlert from '../../components/ErrorAlert';

function ProcedureList() {
  const [patient, setPatient] = useState(null);
  const [embg, setEmbg] = useState('');
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [availableProcedures, setAvailableProcedures] = useState([]);
  const [performedProcedures, setPerformedProcedures] = useState([]);
  const [procedureResults, setProcedureResults] = useState([]);

  const [showRequestForm, setShowRequestForm] = useState(false);
  const [requestData, setRequestData] = useState({
    procedureId: '',
    procedureDate: new Date().toISOString().split('T')[0],
    notes: '',
  });

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

      // Fetch medical record for patient
      const medicalRecordRes = await medicalRecordService.getMedicalRecordByPatientId(patientResponse.data.patientId);

      // Fetch available procedures from database
      try {
        const proceduresResponse = await procedureService.getAllProcedures();
        console.log('Procedures response:', proceduresResponse);
        console.log('Procedures data:', proceduresResponse.data);
        setAvailableProcedures(Array.isArray(proceduresResponse.data) ? proceduresResponse.data : []);
      } catch (procErr) {
        console.error('Failed to fetch procedures:', procErr);
        setError('Failed to fetch procedures: ' + procErr.message);
        setAvailableProcedures([]);
      }

      // Fetch performed procedures for patient
      try {
        const performedRes = await procedureService.getPerformedProceduresForPatient(patientResponse.data.patientId);
        setPerformedProcedures(Array.isArray(performedRes.data) ? performedRes.data : []);
      } catch (err) {
        console.error('Failed to fetch performed procedures:', err);
        setPerformedProcedures([]);
      }

      // Fetch procedure results for medical record
      try {
        const resultsResponse = await procedureService.getProcedureResultsForMedicalRecord(medicalRecordRes.data.recordId);
        setProcedureResults(Array.isArray(resultsResponse.data) ? resultsResponse.data : []);
      } catch (err) {
        console.error('Failed to fetch procedure results:', err);
        setProcedureResults([]);
      }
    } catch (err) {
      console.error('Search error:', err);
      setError(`Error: ${err.response?.data?.error || err.message || 'Patient not found'}`);
      setPatient(null);
    } finally {
      setLoading(false);
    }
  };

  const handleRequestProcedure = async (e) => {
    e.preventDefault();
    setError(null);

    if (!requestData.procedureId) {
      setError('Please select a procedure');
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
        procedureId: parseInt(requestData.procedureId),
        doctorId: parseInt(doctorId),
        patientId: patient.patientId,
        procedureDate: requestData.procedureDate,
        notes: requestData.notes,
      };

      console.log('Sending request:', request);
      await procedureService.recordProcedure(request);

      // Refresh the performed procedures
      const performedRes = await procedureService.getPerformedProceduresForPatient(patient.patientId);
      setPerformedProcedures(performedRes.data);

      // Reset form
      setRequestData({
        procedureId: '',
        procedureDate: new Date().toISOString().split('T')[0],
        notes: '',
      });
      setShowRequestForm(false);
      setError(null);
    } catch (err) {
      console.error('Error details:', err);
      console.error('Response:', err.response);
      const errorMsg = err.response?.data?.error || err.message || 'Unknown error';
      setError('Failed to request procedure: ' + errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1 className="text-3xl font-bold mb-6" style={{ color: '#7c3aed' }}>Procedures</h1>

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

      {/* Patient Procedures */}
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
                  Request Procedure
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

          {/* Request Procedure Form */}
          {showRequestForm && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-xl font-bold mb-4">Request Procedure</h3>
              <form onSubmit={handleRequestProcedure} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Procedure</label>
                    <select
                      value={requestData.procedureId}
                      onChange={(e) => setRequestData({ ...requestData, procedureId: e.target.value })}
                      className="w-full px-4 py-2 border rounded-lg"
                      required
                    >
                      <option value="">Select a procedure</option>
                      {availableProcedures.map((proc) => (
                        <option key={proc.procedureId} value={proc.procedureId}>
                          {proc.procedureType}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-2">Procedure Date</label>
                    <input
                      type="date"
                      value={requestData.procedureDate}
                      onChange={(e) => setRequestData({ ...requestData, procedureDate: e.target.value })}
                      className="w-full px-4 py-2 border rounded-lg"
                    />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-semibold mb-2">Notes</label>
                  <textarea
                    value={requestData.notes}
                    onChange={(e) => setRequestData({ ...requestData, notes: e.target.value })}
                    placeholder="Additional notes for the procedure"
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
                    {loading ? 'Requesting...' : 'Request Procedure'}
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

          {/* Performed Procedures */}
          {performedProcedures && performedProcedures.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-xl font-bold mb-4">Performed Procedures</h3>
              <div className="space-y-3">
                {performedProcedures.map((proc) => (
                  <div key={proc.performedId} className="border-l-4 border-purple-500 pl-4 py-2">
                    <p className="font-semibold text-lg">{proc.procedure?.procedureType || 'Procedure'}</p>
                    <p className="text-sm text-gray-600">Requested by: {proc.doctor?.firstName} {proc.doctor?.lastName}</p>
                    <p className="text-sm text-gray-600">Procedure Date: {proc.procedureDate}</p>
                    {proc.notes && (
                      <p className="text-sm text-gray-600">Notes: {proc.notes}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Procedure Results */}
          {procedureResults && procedureResults.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-xl font-bold mb-4">Procedure Results</h3>
              <div className="space-y-4">
                {procedureResults.map((result) => (
                  <div key={result.resultId} className="border-l-4 border-orange-500 pl-4 py-3 bg-orange-50 rounded">
                    <p className="font-semibold text-lg text-orange-700">{result.procedure?.procedureType || 'Procedure'}</p>
                    <p className="text-sm text-gray-700 mt-2"><strong>Outcome:</strong> {result.resultDescription}</p>
                    <p className="text-sm text-gray-600">Result Date: {result.resultDate}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Link to submit results */}
          {performedProcedures && performedProcedures.length > 0 && (
            <div className="bg-yellow-50 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-yellow-800 mb-3">Submit Procedure Results</h3>
              <p className="text-sm text-gray-700 mb-4">
                {performedProcedures.length} procedure{performedProcedures.length !== 1 ? 's' : ''} awaiting results
              </p>
              <a
                href="/procedures/results"
                className="inline-block bg-yellow-600 text-white px-6 py-2 rounded hover:bg-yellow-700"
              >
                Submit Procedure Results
              </a>
            </div>
          )}

          {/* No procedures message */}
          {!performedProcedures || (performedProcedures.length === 0 && !procedureResults?.length) && (
            <div className="bg-blue-50 rounded-lg p-6 text-center">
              <p className="text-gray-600">No procedures for this patient</p>
            </div>
          )}
        </div>
      )}

      {/* No search performed */}
      {!searched && (
        <div className="bg-gray-50 rounded-lg p-12 text-center">
          <p className="text-gray-600 text-lg">Enter a patient EMBG to request procedures</p>
        </div>
      )}
    </div>
  );
}

export default ProcedureList;
