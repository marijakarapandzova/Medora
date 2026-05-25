import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { patientService } from '../../services/patientService';
import { medicalRecordService } from '../../services/medicalRecordService';
import { labService } from '../../services/labService';
import { procedureService } from '../../services/procedureService';
import ErrorAlert from '../../components/ErrorAlert';

function MedicalRecordList() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const patientId = searchParams.get('patientId');
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isPatient = user.role === 'PATIENT';

  const [patient, setPatient] = useState(null);
  const [medicalData, setMedicalData] = useState(null);
  const [labResults, setLabResults] = useState([]);
  const [procedureResults, setProcedureResults] = useState([]);
  const [error, setError] = useState(null);
  const [embg, setEmbg] = useState(isPatient ? user.username : '');
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);


  React.useEffect(() => {
    if (patientId) {

      loadPatientRecords(patientId);
    } else if (isPatient && user.username) {
      handleAutoSearch();
    }
  }, [patientId]);

  const loadPatientRecords = async (pId) => {
    setError(null);
    setLoading(true);

    try {
      // Get patient by id
      const patientResponse = await patientService.getPatientById(pId);
      setPatient(patientResponse.data);

      // Get medical records for this patient
      const recordsResponse = await medicalRecordService.getMedicalRecordByPatientId(pId);
      setMedicalData(recordsResponse.data);

      // Get lab results
      try {
        const labRes = await labService.getLabResultsForMedicalRecord(recordsResponse.data.recordId);
        setLabResults(labRes.data || []);
      } catch (err) {
        setLabResults([]);
      }

      // Get procedure results
      try {
        const procRes = await procedureService.getProcedureResultsForMedicalRecord(recordsResponse.data.recordId);
        setProcedureResults(procRes.data || []);
      } catch (err) {
        setProcedureResults([]);
      }

      setSearched(true);
    } catch (err) {
      setError(`Failed to load medical records for patient`);
      setSearched(true);
      setPatient(null);
      setMedicalData(null);
      setLabResults([]);
      setProcedureResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleAutoSearch = async () => {
    const searchEmbg = user.username;
    setError(null);
    setLoading(true);

    try {
      // Search patient by EMBG
      const patientResponse = await patientService.getPatientByEmbg(searchEmbg);
      setPatient(patientResponse.data);

      // Get medical records for this patient
      const recordsResponse = await medicalRecordService.getMedicalRecordByPatientId(patientResponse.data.patientId);
      setMedicalData(recordsResponse.data);

      // Get lab results
      try {
        const labRes = await labService.getLabResultsForMedicalRecord(recordsResponse.data.recordId);
        setLabResults(labRes.data || []);
      } catch (err) {
        setLabResults([]);
      }

      // Get procedure results
      try {
        const procRes = await procedureService.getProcedureResultsForMedicalRecord(recordsResponse.data.recordId);
        setProcedureResults(procRes.data || []);
      } catch (err) {
        setProcedureResults([]);
      }

      setSearched(true);
    } catch (err) {
      setError(`Patient with EMBG ${searchEmbg} not found`);
      setSearched(true);
      setPatient(null);
      setMedicalData(null);
      setLabResults([]);
      setProcedureResults([]);
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

      // Search patient by EMBG
      const patientResponse = await patientService.getPatientByEmbg(embg);
      setPatient(patientResponse.data);

      // Get medical records for this patient
      const recordsResponse = await medicalRecordService.getMedicalRecordByPatientId(patientResponse.data.patientId);
      setMedicalData(recordsResponse.data);

      // Get lab results
      try {
        const labRes = await labService.getLabResultsForMedicalRecord(recordsResponse.data.recordId);
        setLabResults(labRes.data || []);
      } catch (err) {
        setLabResults([]);
      }

      // Get procedure results
      try {
        const procRes = await procedureService.getProcedureResultsForMedicalRecord(recordsResponse.data.recordId);
        setProcedureResults(procRes.data || []);
      } catch (err) {
        setProcedureResults([]);
      }

      setSearched(true);
    } catch (err) {
      setError(`Patient with EMBG ${embg} not found`);
      setSearched(true);
      setPatient(null);
      setMedicalData(null);
      setLabResults([]);
      setProcedureResults([]);
    } finally {
      setLoading(false);
    }
  };

  return (
      <div>
        <h1 className="text-3xl font-bold mb-6" style={{ color: '#7c3aed' }}>Medical Records</h1>

        {error && <ErrorAlert message={error} onClose={() => setError(null)} />}

        {/* Search Form - Only show for non-patients */}
        {!isPatient && (
            <div className="bg-white rounded-lg shadow p-6 mb-6">
              <h2 className="text-xl font-bold mb-4">Search Medical Records</h2>
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
        )}

        {/* Loading indicator for patients fetching their records */}
        {isPatient && loading && (
            <div className="bg-blue-50 rounded-lg shadow p-6 mb-6">
              <p className="text-sm text-gray-700">
                <strong>Loading your medical records...</strong>
              </p>
            </div>
        )}

        {/* Patient Info Message for Patients */}
        {isPatient && searched && !loading && (
            <div className="bg-blue-50 rounded-lg shadow p-6 mb-6">
              <p className="text-sm text-gray-700">
                <strong>Viewing your medical records</strong>
              </p>
            </div>
        )}

        {/* Patient Medical Records */}
        {searched && patient && (
            <div className="space-y-6">
              {/* Patient Info */}
              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex justify-between items-start mb-4">
                  <h2 className="text-2xl font-bold">{patient.firstName} {patient.lastName}</h2>
                  {!isPatient && (
                      <button
                          onClick={() => navigate(`/medical-records/${patient.patientId}`)}
                          className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700"
                      >
                        Add Medical Data
                      </button>
                  )}
                </div>
                <div className="grid grid-cols-4 gap-4">
                  <div>
                    <p className="text-sm text-gray-600">EMBG</p>
                    <p className="font-semibold">{patient.embg}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600">Email</p>
                    <p className="font-semibold">{patient.emailAddress}</p>
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

              {/* Medical Data Sections */}
              {medicalData && (
                  <>
                    {/* Diagnoses */}
                    {medicalData.diagnoses && medicalData.diagnoses.length > 0 && (
                        <div className="bg-white rounded-lg shadow p-6">
                          <h3 className="text-xl font-bold mb-4">Diagnoses</h3>
                          <div className="space-y-3">
                            {medicalData.diagnoses.map((diagnosis) => (
                                <div key={diagnosis.diagnosisId} className="border-l-4 border-purple-500 pl-4 py-2">
                                  <p className="font-semibold text-lg">{diagnosis.name}</p>
                                  {diagnosis.description && (
                                      <p className="text-gray-600 text-sm">{diagnosis.description}</p>
                                  )}
                                  <p className="text-xs text-gray-500">By: {diagnosis.doctorName}</p>
                                </div>
                            ))}
                          </div>
                        </div>
                    )}

                    {/* Symptoms */}
                    {medicalData.symptoms && medicalData.symptoms.length > 0 && (
                        <div className="bg-white rounded-lg shadow p-6">
                          <h3 className="text-xl font-bold mb-4">Symptoms</h3>
                          <div className="flex flex-wrap gap-2">
                            {medicalData.symptoms.map((symptom) => (
                                <span key={symptom.symptomId} className="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm">
                        {symptom.symptomName}
                      </span>
                            ))}
                          </div>
                        </div>
                    )}

                    {/* Allergies */}
                    {medicalData.allergies && medicalData.allergies.length > 0 && (
                        <div className="bg-white rounded-lg shadow p-6">
                          <h3 className="text-xl font-bold mb-4">Allergies</h3>
                          <div className="space-y-3">
                            {medicalData.allergies.map((allergy) => (
                                <div key={allergy.allergyId} className="border-l-4 border-red-500 pl-4 py-2">
                                  <p className="font-semibold">{allergy.allergyName}</p>
                                  <p className="text-sm">
                          <span className={`px-2 py-1 rounded text-white text-xs ${
                              allergy.severity === 'CRITICAL' ? 'bg-red-600' :
                                  allergy.severity === 'HIGH' ? 'bg-red-500' :
                                      allergy.severity === 'MEDIUM' ? 'bg-yellow-500' :
                                          'bg-green-500'
                          }`}>
                            {allergy.severity} Severity
                          </span>
                                  </p>
                                  {allergy.reaction && (
                                      <p className="text-gray-600 text-sm">Reaction: {allergy.reaction}</p>
                                  )}
                                </div>
                            ))}
                          </div>
                        </div>
                    )}

                    {/* Medical Reports */}
                    {medicalData.reports && medicalData.reports.length > 0 && (
                        <div className="bg-white rounded-lg shadow p-6">
                          <h3 className="text-xl font-bold mb-4">Medical Reports</h3>
                          <div className="space-y-3">
                            {medicalData.reports.map((report) => (
                                <div key={report.reportId} className="border-l-4 border-green-500 pl-4 py-2">
                                  <p className="font-semibold">Report from {report.doctorName}</p>
                                  <p className="text-gray-600 text-sm">{report.description}</p>
                                  <p className="text-xs text-gray-500">Date: {report.reportDate}</p>
                                </div>
                            ))}
                          </div>
                        </div>
                    )}

                    {/* Lab Test Results */}
                    {labResults && labResults.length > 0 && (
                        <div className="bg-white rounded-lg shadow p-6">
                          <h3 className="text-xl font-bold mb-4">Lab Test Results</h3>
                          <div className="space-y-3">
                            {labResults.map((result) => (
                                <div key={result.labResultId} className="border-l-4 border-purple-500 pl-4 py-2">
                                  <p className="font-semibold">{result.testName}</p>
                                  <p className="text-gray-600 text-sm">{result.result}</p>
                                  {result.notes && (
                                      <p className="text-gray-600 text-sm">Notes: {result.notes}</p>
                                  )}
                                  <p className="text-xs text-gray-500">Date: {result.resultDate}</p>
                                  {result.technicianName && (
                                      <p className="text-xs text-gray-500">Technician: {result.technicianName}</p>
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
                          <div className="space-y-3">
                            {procedureResults.map((result) => (
                                <div key={result.procedureResultId} className="border-l-4 border-orange-500 pl-4 py-2">
                                  <p className="font-semibold">{result.procedureName}</p>
                                  <p className="text-gray-600 text-sm">{result.result}</p>
                                  {result.notes && (
                                      <p className="text-gray-600 text-sm">Notes: {result.notes}</p>
                                  )}
                                  <p className="text-xs text-gray-500">Date: {result.resultDate}</p>
                                  {result.doctorName && (
                                      <p className="text-xs text-gray-500">Doctor: {result.doctorName}</p>
                                  )}
                                </div>
                            ))}
                          </div>
                        </div>
                    )}

                    {/* No data message */}
                    {(!medicalData.diagnoses || medicalData.diagnoses.length === 0) &&
                        (!medicalData.symptoms || medicalData.symptoms.length === 0) &&
                        (!medicalData.allergies || medicalData.allergies.length === 0) &&
                        (!medicalData.reports || medicalData.reports.length === 0) &&
                        (!labResults || labResults.length === 0) &&
                        (!procedureResults || procedureResults.length === 0) && (
                            <div className="bg-blue-50 rounded-lg p-6 text-center">
                              <p className="text-gray-600">No medical records found for this patient</p>
                            </div>
                        )}
                  </>
              )}
            </div>
        )}

        {/* No search performed */}
        {!searched && !isPatient && (
            <div className="bg-gray-50 rounded-lg p-12 text-center">
              <p className="text-gray-600 text-lg">Enter a patient EMBG to view their medical records</p>
            </div>
        )}

        {/* Loading message for patients */}
        {isPatient && loading && (
            <div className="bg-gray-50 rounded-lg p-12 text-center">
              <p className="text-gray-600 text-lg">Loading your medical records...</p>
            </div>
        )}
      </div>
  );
}

export default MedicalRecordList;
