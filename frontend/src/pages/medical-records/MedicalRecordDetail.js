import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { medicalRecordService } from '../../services/medicalRecordService';
import { doctorService } from '../../services/doctorService';
import { labService } from '../../services/labService';
import { procedureService } from '../../services/procedureService';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';
import Loading from '../../components/Loading';
import apiClient from '../../services/api';

function MedicalRecordDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isDoctor = user.role === 'DOCTOR';

  const [record, setRecord] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [activeTab, setActiveTab] = useState('view');
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState(isDoctor ? user.doctorId : '');

  // Diagnosis form
  const [diagnosisForm, setDiagnosisForm] = useState({
    diagnosisId: '',
    doctorId: '',
  });

  // Symptoms form
  const [symptomsForm, setSymptomsForm] = useState({
    symptomId: '',
  });

  // Allergies form
  const [allergyForm, setAllergyForm] = useState({
    allergyId: '',
    severity: 'MEDIUM',
    reaction: '',
  });

  // Prescription form
  const [prescriptionForm, setPrescriptionForm] = useState({
    prescriptionId: '',
    dosage: '',
    frequency: '',
    duration: '',
    reason: '',
  });

  // Dropdown options
  const [diagnoses, setDiagnoses] = useState([]);
  const [symptoms, setSymptoms] = useState([]);
  const [allergies, setAllergies] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [labTests, setLabTests] = useState([]);
  const [procedures, setProcedures] = useState([]);

  // Lab test form
  const [labTestForm, setLabTestForm] = useState({
    testId: '',
    testDate: new Date().toISOString().split('T')[0],
    notes: '',
  });

  // Procedure form
  const [procedureForm, setProcedureForm] = useState({
    procedureId: '',
    procedureDate: new Date().toISOString().split('T')[0],
    notes: '',
    diagnosisId: '',
  });

  const [labResults, setLabResults] = useState([]);
  const [procedureResults, setProcedureResults] = useState([]);

  useEffect(() => {
    fetchRecord();
    fetchDoctors();
    fetchDropdownOptions();
  }, [id]);

  const fetchDropdownOptions = async () => {
    try {
      console.log('Fetching dropdown options...');
      const diagnosesRes = await apiClient.get('/medical-records/dropdown/diagnoses');
      console.log('Diagnoses:', diagnosesRes.data);

      const symptomsRes = await apiClient.get('/medical-records/dropdown/symptoms');
      console.log('Symptoms:', symptomsRes.data);

      const allergiesRes = await apiClient.get('/medical-records/dropdown/allergies');
      console.log('Allergies:', allergiesRes.data);

      const prescriptionsRes = await apiClient.get('/medical-records/dropdown/prescriptions');
      console.log('Prescriptions:', prescriptionsRes.data);

      const testsRes = await labService.getAllLabTests();
      console.log('Lab Tests:', testsRes.data);

      const proceduresRes = await procedureService.getAllProcedures();
      console.log('Procedures:', proceduresRes.data);

      setDiagnoses(diagnosesRes.data || []);
      setSymptoms(symptomsRes.data || []);
      setAllergies(allergiesRes.data || []);
      setPrescriptions(prescriptionsRes.data || []);
      setLabTests(testsRes.data || []);
      setProcedures(proceduresRes.data || []);
      console.log('Dropdown options set successfully');
    } catch (err) {
      console.error('Error fetching dropdown options:', err);
      setError('Failed to load dropdown options: ' + (err.response?.data?.error || err.message));
    }
  };

  const fetchRecord = async () => {
    try {
      setLoading(true);
      const response = await medicalRecordService.getMedicalRecordByPatientId(id);
      setRecord(response.data);

      // Fetch lab results
      try {
        const resultsResponse = await labService.getLabResultsForMedicalRecord(response.data.recordId);
        setLabResults(resultsResponse.data || []);
      } catch (err) {
        console.error('Failed to fetch lab results:', err);
        setLabResults([]);
      }

      // Fetch procedure results
      try {
        const procedureResultsResponse = await procedureService.getProcedureResultsForMedicalRecord(response.data.recordId);
        setProcedureResults(procedureResultsResponse.data || []);
      } catch (err) {
        console.error('Failed to fetch procedure results:', err);
        setProcedureResults([]);
      }
    } catch (err) {
      setError('Failed to fetch medical record');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchDoctors = async () => {
    try {
      if (!isDoctor) {
        const response = await doctorService.getAllDoctors();
        setDoctors(response.data);
        if (response.data.length > 0 && !selectedDoctorId) {
          setSelectedDoctorId(response.data[0].doctorId);
        }
      }
    } catch (err) {
      console.error('Failed to fetch doctors', err);
    }
  };

  const handleAddDiagnosis = async (e) => {
    e.preventDefault();
    try {
      if (!diagnosisForm.diagnosisId) {
        setError('Please select a diagnosis');
        return;
      }
      if (!selectedDoctorId) {
        setError('Please select a doctor');
        return;
      }
      const selectedDiagnosis = diagnoses.find(d => d.id === parseInt(diagnosisForm.diagnosisId));
      await apiClient.post('/diagnoses', {
        patientId: parseInt(record.patientId),
        doctorId: parseInt(selectedDoctorId),
        name: selectedDiagnosis.name,
        description: '',
      });
      setSuccess('Diagnosis recorded successfully');
      setDiagnosisForm({ diagnosisId: '', doctorId: '' });
      setTimeout(() => fetchRecord(), 1000);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to record diagnosis');
    }
  };

  const handleAddSymptom = async (e) => {
    e.preventDefault();
    try {
      if (!symptomsForm.symptomId) {
        setError('Please select a symptom');
        return;
      }
      await apiClient.post(`/medical-records/${record.recordId}/symptoms`, {
        symptomId: parseInt(symptomsForm.symptomId),
        severity: 'MEDIUM',
      });
      setSuccess('Symptom recorded successfully');
      setSymptomsForm({ symptomId: '' });
      setTimeout(() => fetchRecord(), 1000);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to record symptom');
    }
  };

  const handleAddAllergy = async (e) => {
    e.preventDefault();
    try {
      if (!allergyForm.allergyId) {
        setError('Please select an allergy');
        return;
      }
      await apiClient.post(`/medical-records/${record.recordId}/allergies`, {
        allergyId: parseInt(allergyForm.allergyId),
        severity: allergyForm.severity,
        reaction: allergyForm.reaction,
      });
      setSuccess('Allergy recorded successfully');
      setAllergyForm({ allergyId: '', severity: 'MEDIUM', reaction: '' });
      setTimeout(() => fetchRecord(), 1000);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to record allergy');
    }
  };

  const handleAddPrescription = async (e) => {
    e.preventDefault();
    try {
      if (!prescriptionForm.prescriptionId) {
        setError('Please select a prescription medication');
        return;
      }
      if (!prescriptionForm.dosage.trim()) {
        setError('Dosage is required');
        return;
      }
      if (!prescriptionForm.frequency.trim()) {
        setError('Frequency is required');
        return;
      }
      if (!prescriptionForm.duration.trim()) {
        setError('Duration is required');
        return;
      }
      const selectedPrescription = prescriptions.find(p => p.id === parseInt(prescriptionForm.prescriptionId));
      await apiClient.post('/prescriptions', {
        medicalRecordId: record.recordId,
        medicationName: selectedPrescription.name,
        dosage: prescriptionForm.dosage,
        frequency: prescriptionForm.frequency,
        duration: prescriptionForm.duration,
        notes: prescriptionForm.reason,
      });
      setSuccess('Prescription recorded successfully');
      setPrescriptionForm({
        prescriptionId: '',
        dosage: '',
        frequency: '',
        duration: '',
        reason: '',
      });
      setTimeout(() => fetchRecord(), 1000);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to record prescription');
    }
  };

  if (loading) return <Loading />;

  if (!record) {
    return (
      <div>
        <ErrorAlert message="Medical record not found" onClose={() => navigate('/medical-records')} />
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 style={{ fontSize: '36px', fontWeight: 'normal' }}>Medical Record</h1>
        <button
          onClick={() => navigate('/medical-records')}
          className="bg-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-400"
        >
          Back
        </button>
      </div>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
      {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

      {/* Patient Info */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 style={{ fontSize: '20px', fontWeight: 'normal', marginBottom: '16px' }}>Patient Information</h2>
        <div className="grid grid-cols-4 gap-4">
          <div>
            <p className="font-normal text-sm text-gray-600">Patient Name</p>
            <p className="font-normal">{record.patientName}</p>
          </div>
          <div>
            <p className="font-normal text-sm text-gray-600">EMBG</p>
            <p className="font-normal">{record.embg}</p>
          </div>
          <div>
            <p className="font-normal text-sm text-gray-600">Record ID</p>
            <p className="font-normal">{record.recordId}</p>
          </div>
          <div>
            <p className="font-normal text-sm text-gray-600">Patient ID</p>
            <p className="font-normal">{record.patientId}</p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-6 border-b">
        <button
          onClick={() => setActiveTab('view')}
          style={{ fontWeight: 'normal' }}
          className={`px-6 py-3 ${
            activeTab === 'view'
              ? 'border-b-2 border-purple-600 text-purple-600'
              : 'text-purple-600 hover:text-purple-700'
          }`}
        >
          View Medical Data
        </button>
        <span className="text-gray-600 px-3" style={{ fontWeight: 'normal' }}>|</span>
        <button
          onClick={() => setActiveTab('diagnosis')}
          style={{ fontWeight: 'normal' }}
          className={`px-6 py-3 ${
            activeTab === 'diagnosis'
              ? 'border-b-2 border-purple-600 text-purple-600'
              : 'text-purple-600 hover:text-purple-700'
          }`}
        >
          Record Diagnosis
        </button>
        <span className="text-gray-600 px-3" style={{ fontWeight: 'normal' }}>|</span>
        <button
          onClick={() => setActiveTab('symptoms')}
          style={{ fontWeight: 'normal' }}
          className={`px-6 py-3 ${
            activeTab === 'symptoms'
              ? 'border-b-2 border-purple-600 text-purple-600'
              : 'text-purple-600 hover:text-purple-700'
          }`}
        >
          Record Symptoms
        </button>
        <span className="text-gray-600 px-3" style={{ fontWeight: 'normal' }}>|</span>
        <button
          onClick={() => setActiveTab('allergies')}
          style={{ fontWeight: 'normal' }}
          className={`px-6 py-3 ${
            activeTab === 'allergies'
              ? 'border-b-2 border-purple-600 text-purple-600'
              : 'text-purple-600 hover:text-purple-700'
          }`}
        >
          Record Allergies
        </button>
        <span className="text-gray-600 px-3" style={{ fontWeight: 'normal' }}>|</span>
        <button
          onClick={() => setActiveTab('prescription')}
          style={{ fontWeight: 'normal' }}
          className={`px-6 py-3 ${
            activeTab === 'prescription'
              ? 'border-b-2 border-purple-600 text-purple-600'
              : 'text-purple-600 hover:text-purple-700'
          }`}
        >
          Record Prescription
        </button>
        <span className="text-gray-600 px-3" style={{ fontWeight: 'normal' }}>|</span>
        <button
          onClick={() => setActiveTab('lab-tests')}
          style={{ fontWeight: 'normal' }}
          className={`px-6 py-3 ${
            activeTab === 'lab-tests'
              ? 'border-b-2 border-purple-600 text-purple-600'
              : 'text-purple-600 hover:text-purple-700'
          }`}
        >
          Request Lab Test
        </button>
        <span className="text-gray-600 px-3" style={{ fontWeight: 'normal' }}>|</span>
        <button
          onClick={() => setActiveTab('procedures')}
          style={{ fontWeight: 'normal' }}
          className={`px-6 py-3 ${
            activeTab === 'procedures'
              ? 'border-b-2 border-purple-600 text-purple-600'
              : 'text-purple-600 hover:text-purple-700'
          }`}
        >
          Request Procedure
        </button>
      </div>

      {/* View Medical Data Tab */}
      {activeTab === 'view' && (
        <div className="space-y-6">
          {/* Diagnoses */}
          {record.diagnoses && record.diagnoses.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-normal" style={{ fontSize: '20px', marginBottom: '16px' }}>Diagnoses</h3>
              <div className="space-y-3">
                {record.diagnoses.map((diagnosis) => (
                  <div key={diagnosis.diagnosisId} className="border-l-4 border-purple-500 pl-4 py-2">
                    <p className="font-normal" style={{ fontSize: '18px' }}>{diagnosis.name}</p>
                    {diagnosis.description && (
                      <p className="font-normal text-gray-600 text-sm">{diagnosis.description}</p>
                    )}
                    <p className="font-normal text-xs text-gray-500">By: {diagnosis.doctorName}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Symptoms */}
          {record.symptoms && record.symptoms.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-normal" style={{ fontSize: '20px', marginBottom: '16px' }}>Symptoms</h3>
              <div className="flex flex-wrap gap-2">
                {record.symptoms.map((symptom) => (
                  <span
                    key={symptom.symptomId}
                    className="bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full text-sm font-normal"
                  >
                    {symptom.symptomName}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Allergies */}
          {record.allergies && record.allergies.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-normal" style={{ fontSize: '20px', marginBottom: '16px' }}>Allergies</h3>
              <div className="space-y-3">
                {record.allergies.map((allergy) => (
                  <div key={allergy.allergyId} className="border-l-4 border-red-500 pl-4 py-2">
                    <p className="font-normal">{allergy.allergyName}</p>
                    <p className="font-normal" style={{ fontSize: '14px' }}>
                      <span
                        className="font-normal"
                        style={{
                          padding: '4px 8px',
                          borderRadius: '4px',
                          color: 'white',
                          fontSize: '12px',
                          background: allergy.severity === 'CRITICAL' ? '#dc2626' : allergy.severity === 'HIGH' ? '#ef4444' : allergy.severity === 'MEDIUM' ? '#eab308' : '#22c55e'
                        }}
                      >
                        {allergy.severity} Severity
                      </span>
                    </p>
                    {allergy.reaction && (
                      <p className="font-normal text-gray-600 text-sm">Reaction: {allergy.reaction}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Prescriptions */}
          {record.prescriptions && record.prescriptions.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-normal" style={{ fontSize: '20px', marginBottom: '16px' }}>Prescriptions</h3>
              <div className="space-y-3">
                {record.prescriptions.map((prescription) => (
                  <div key={prescription.prescriptionId} className="border-l-4 border-purple-500 pl-4 py-2">
                    <p className="font-normal">{prescription.medicationName}</p>
                    <p className="font-normal text-sm text-gray-600">Dosage: {prescription.dosage}</p>
                    <p className="font-normal text-sm text-gray-600">Frequency: {prescription.frequency}</p>
                    <p className="font-normal text-sm text-gray-600">Duration: {prescription.duration}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Reports */}
          {record.reports && record.reports.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-normal" style={{ fontSize: '20px', marginBottom: '16px' }}>Medical Reports</h3>
              <div className="space-y-3">
                {record.reports.map((report) => (
                  <div key={report.reportId} className="border-l-4 border-green-500 pl-4 py-2">
                    <p style={{ fontWeight: 'normal' }}>Report from {report.doctorName}</p>
                    <p className="font-normal text-gray-600 text-sm">{report.description}</p>
                    <p className="font-normal text-xs text-gray-500">Date: {report.reportDate}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Lab Results */}
          {labResults && labResults.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-normal" style={{ fontSize: '20px', marginBottom: '16px' }}>Lab Test Results</h3>
              <div className="space-y-3">
                {labResults.map((result) => (
                  <div key={result.resultId} className="border-l-4 border-cyan-500 pl-4 py-3 bg-cyan-50 rounded">
                    <p className="font-normal" style={{ fontSize: '18px', color: '#0891b2' }}>{result.testName}</p>
                    <p className="font-normal text-sm text-gray-700 mt-2">Results: {result.results}</p>
                    <p className="font-normal text-sm text-gray-600">Result Date: {result.resultDate}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Procedure Results */}
          {procedureResults && procedureResults.length > 0 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-normal" style={{ fontSize: '20px', marginBottom: '16px' }}>Procedure Results</h3>
              <div className="space-y-3">
                {procedureResults.map((result) => (
                  <div key={result.resultId} className="border-l-4 border-orange-500 pl-4 py-3 bg-orange-50 rounded">
                    <p className="font-normal" style={{ fontSize: '18px', color: '#b45309' }}>{result.procedure?.procedureType || 'Procedure'}</p>
                    <p className="font-normal text-sm text-gray-700 mt-2">Outcome: {result.resultDescription}</p>
                    <p className="font-normal text-sm text-gray-600">Result Date: {result.resultDate}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {(!record.diagnoses || record.diagnoses.length === 0) &&
            (!record.symptoms || record.symptoms.length === 0) &&
            (!record.allergies || record.allergies.length === 0) &&
            (!record.reports || record.reports.length === 0) &&
            (!labResults || labResults.length === 0) &&
            (!procedureResults || procedureResults.length === 0) && (
              <div className="bg-blue-50 rounded-lg p-6 text-center">
                <p className="text-gray-600">No medical data recorded yet</p>
              </div>
            )}
        </div>
      )}

      {/* Record Diagnosis Tab */}
      {activeTab === 'diagnosis' && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 style={{ fontSize: '20px', fontWeight: 'normal', marginBottom: '16px' }}>Record New Diagnosis</h3>
          <form onSubmit={handleAddDiagnosis} className="space-y-4 max-w-2xl">
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Doctor *</label>
              {isDoctor ? (
                <p className="px-4 py-2 bg-gray-100 rounded-lg text-gray-700">
                  Dr. {user.firstName} {user.lastName}
                </p>
              ) : (
                <select
                  value={selectedDoctorId}
                  onChange={(e) => setSelectedDoctorId(e.target.value)}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select doctor</option>
                  {doctors.map((doc) => (
                    <option key={doc.doctorId} value={doc.doctorId}>
                      Dr. {doc.firstName} {doc.lastName}
                    </option>
                  ))}
                </select>
              )}
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Diagnosis *</label>
              <select
                value={diagnosisForm.diagnosisId}
                onChange={(e) => setDiagnosisForm({ ...diagnosisForm, diagnosisId: e.target.value })}
                className="w-full px-4 py-2 border rounded-lg"
                required
              >
                <option value="">{diagnoses.length === 0 ? 'Loading diagnoses...' : 'Select diagnosis'}</option>
                {diagnoses && diagnoses.length > 0 && diagnoses.map((diagnosis) => (
                  <option key={diagnosis.id} value={diagnosis.id}>
                    {diagnosis.name}
                  </option>
                ))}
              </select>
              {diagnoses.length === 0 && (
                <p className="text-sm text-gray-500 mt-1">No diagnoses available. Please wait...</p>
              )}
            </div>
            <button
              type="submit"
              style={{background: '#9333ea', color: 'white', padding: '6px 14px', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '600'}} onMouseEnter={(e) => e.currentTarget.style.background = '#7e22ce'} onMouseLeave={(e) => e.currentTarget.style.background = '#9333ea'}
            >
              Record Diagnosis
            </button>
          </form>
        </div>
      )}

      {/* Record Symptoms Tab */}
      {activeTab === 'symptoms' && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 style={{ fontSize: '20px', fontWeight: 'normal', marginBottom: '16px' }}>Record Patient Symptoms</h3>
          <form onSubmit={handleAddSymptom} className="space-y-4 max-w-2xl">
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Doctor *</label>
              {isDoctor ? (
                <p className="px-4 py-2 bg-gray-100 rounded-lg text-gray-700">
                  Dr. {user.firstName} {user.lastName}
                </p>
              ) : (
                <select
                  value={selectedDoctorId}
                  onChange={(e) => setSelectedDoctorId(e.target.value)}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select doctor</option>
                  {doctors.map((doc) => (
                    <option key={doc.doctorId} value={doc.doctorId}>
                      Dr. {doc.firstName} {doc.lastName}
                    </option>
                  ))}
                </select>
              )}
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Symptom *</label>
              <select
                value={symptomsForm.symptomId}
                onChange={(e) => setSymptomsForm({ ...symptomsForm, symptomId: e.target.value })}
                className="w-full px-4 py-2 border rounded-lg"
                required
              >
                <option value="">Select symptom</option>
                {symptoms.filter(s => !record.symptoms?.some(rs => rs.symptomId === s.id)).map((symptom) => (
                  <option key={symptom.id} value={symptom.id}>
                    {symptom.name}
                  </option>
                ))}
              </select>
            </div>
            <button
              type="submit"
              style={{background: '#9333ea', color: 'white', padding: '6px 14px', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '600'}} onMouseEnter={(e) => e.currentTarget.style.background = '#7e22ce'} onMouseLeave={(e) => e.currentTarget.style.background = '#9333ea'}
            >
              Record Symptom
            </button>
          </form>
        </div>
      )}

      {/* Record Allergies Tab */}
      {activeTab === 'allergies' && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 style={{ fontSize: '20px', fontWeight: 'normal', marginBottom: '16px' }}>Record Patient Allergies</h3>
          <form onSubmit={handleAddAllergy} className="space-y-4 max-w-2xl">
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Doctor *</label>
              {isDoctor ? (
                <p className="px-4 py-2 bg-gray-100 rounded-lg text-gray-700">
                  Dr. {user.firstName} {user.lastName}
                </p>
              ) : (
                <select
                  value={selectedDoctorId}
                  onChange={(e) => setSelectedDoctorId(e.target.value)}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select doctor</option>
                  {doctors.map((doc) => (
                    <option key={doc.doctorId} value={doc.doctorId}>
                      Dr. {doc.firstName} {doc.lastName}
                    </option>
                  ))}
                </select>
              )}
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Allergy *</label>
              <select
                value={allergyForm.allergyId}
                onChange={(e) => setAllergyForm({ ...allergyForm, allergyId: e.target.value })}
                className="w-full px-4 py-2 border rounded-lg"
                required
              >
                <option value="">Select allergy</option>
                {allergies.filter(a => !record.allergies?.some(ra => ra.allergyId === a.id)).map((allergy) => (
                  <option key={allergy.id} value={allergy.id}>
                    {allergy.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Severity *</label>
              <select
                value={allergyForm.severity}
                onChange={(e) => setAllergyForm({ ...allergyForm, severity: e.target.value })}
                className="w-full px-4 py-2 border rounded-lg"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Reaction</label>
              <textarea
                value={allergyForm.reaction}
                onChange={(e) => setAllergyForm({ ...allergyForm, reaction: e.target.value })}
                placeholder="Describe the allergic reaction"
                className="w-full px-4 py-2 border rounded-lg"
                rows="3"
              />
            </div>
            <button
              type="submit"
              style={{background: '#9333ea', color: 'white', padding: '6px 14px', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '600'}} onMouseEnter={(e) => e.currentTarget.style.background = '#7e22ce'} onMouseLeave={(e) => e.currentTarget.style.background = '#9333ea'}
            >
              Record Allergy
            </button>
          </form>
        </div>
      )}

      {/* Record Prescription Tab */}
      {activeTab === 'prescription' && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 style={{ fontSize: '20px', fontWeight: 'normal', marginBottom: '16px' }}>Record Prescription</h3>
          <form onSubmit={handleAddPrescription} className="space-y-4 max-w-2xl">
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Doctor *</label>
              {isDoctor ? (
                <p className="px-4 py-2 bg-gray-100 rounded-lg text-gray-700">
                  Dr. {user.firstName} {user.lastName}
                </p>
              ) : (
                <select
                  value={selectedDoctorId}
                  onChange={(e) => setSelectedDoctorId(e.target.value)}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select doctor</option>
                  {doctors.map((doc) => (
                    <option key={doc.doctorId} value={doc.doctorId}>
                      Dr. {doc.firstName} {doc.lastName}
                    </option>
                  ))}
                </select>
              )}
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Medication *</label>
              <select
                value={prescriptionForm.prescriptionId}
                onChange={(e) =>
                  setPrescriptionForm({ ...prescriptionForm, prescriptionId: e.target.value })
                }
                className="w-full px-4 py-2 border rounded-lg"
                required
              >
                <option value="">Select medication</option>
                {prescriptions.map((prescription) => (
                  <option key={prescription.id} value={prescription.id}>
                    {prescription.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Dosage *</label>
                <input
                  type="text"
                  value={prescriptionForm.dosage}
                  onChange={(e) =>
                    setPrescriptionForm({ ...prescriptionForm, dosage: e.target.value })
                  }
                  placeholder="e.g., 500mg"
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                />
              </div>
              <div>
                <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Frequency *</label>
                <input
                  type="text"
                  value={prescriptionForm.frequency}
                  onChange={(e) =>
                    setPrescriptionForm({ ...prescriptionForm, frequency: e.target.value })
                  }
                  placeholder="e.g., Twice daily"
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                />
              </div>
              <div>
                <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Duration *</label>
                <input
                  type="text"
                  value={prescriptionForm.duration}
                  onChange={(e) =>
                    setPrescriptionForm({ ...prescriptionForm, duration: e.target.value })
                  }
                  placeholder="e.g., 7 days"
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                />
              </div>
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Reason for Prescription</label>
              <textarea
                value={prescriptionForm.reason}
                onChange={(e) => setPrescriptionForm({ ...prescriptionForm, reason: e.target.value })}
                placeholder="Describe why this medication is prescribed"
                className="w-full px-4 py-2 border rounded-lg"
                rows="3"
              />
            </div>
            <button
              type="submit"
              style={{background: '#9333ea', color: 'white', padding: '6px 14px', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '600'}} onMouseEnter={(e) => e.currentTarget.style.background = '#7e22ce'} onMouseLeave={(e) => e.currentTarget.style.background = '#9333ea'}
            >
              Record Prescription
            </button>
          </form>
        </div>
      )}

      {/* Procedure Request Tab */}
      {activeTab === 'procedures' && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 style={{ fontSize: '20px', fontWeight: 'normal', marginBottom: '16px' }}>Request Procedure</h3>
          <form onSubmit={async (e) => {
            e.preventDefault();
            setError(null);

            if (!procedureForm.procedureId) {
              setError('Please select a procedure');
              return;
            }

            try {
              setLoading(true);
              const doctorId = localStorage.getItem('doctorId') || 1;

              const request = {
                patientId: record.patientId,
                doctorId: parseInt(doctorId),
                procedureId: parseInt(procedureForm.procedureId),
                procedureDate: procedureForm.procedureDate,
                notes: procedureForm.notes,
                diagnosisId: procedureForm.diagnosisId ? parseInt(procedureForm.diagnosisId) : null,
              };

              await procedureService.requestProcedure(request);

              setSuccess('Procedure requested successfully');
              setProcedureForm({
                procedureId: '',
                procedureDate: new Date().toISOString().split('T')[0],
                notes: '',
                diagnosisId: '',
              });

              setTimeout(() => setSuccess(null), 3000);
            } catch (err) {
              setError('Failed to request procedure: ' + (err.response?.data?.error || err.message));
            } finally {
              setLoading(false);
            }
          }} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Procedure *</label>
                <select
                  value={procedureForm.procedureId}
                  onChange={(e) => setProcedureForm({ ...procedureForm, procedureId: e.target.value })}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select a procedure</option>
                  {procedures.map((proc) => (
                    <option key={proc.procedureId} value={proc.procedureId}>
                      {proc.procedureType} (${proc.cost})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Procedure Date *</label>
                <input
                  type="date"
                  value={procedureForm.procedureDate}
                  onChange={(e) => setProcedureForm({ ...procedureForm, procedureDate: e.target.value })}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                />
              </div>
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Related Diagnosis (Optional)</label>
              <select
                value={procedureForm.diagnosisId}
                onChange={(e) => setProcedureForm({ ...procedureForm, diagnosisId: e.target.value })}
                className="w-full px-4 py-2 border rounded-lg"
              >
                <option value="">Select diagnosis (optional)</option>
                {diagnoses.map((diagnosis) => (
                  <option key={diagnosis.id} value={diagnosis.id}>
                    {diagnosis.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Notes</label>
              <textarea
                value={procedureForm.notes}
                onChange={(e) => setProcedureForm({ ...procedureForm, notes: e.target.value })}
                placeholder="Pre-procedure instructions or special notes"
                className="w-full px-4 py-2 border rounded-lg"
                rows="3"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              style={{background: loading ? '#9ca3af' : '#9333ea', color: 'white', padding: '6px 14px', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '600'}} onMouseEnter={(e) => !loading && (e.currentTarget.style.background = '#7e22ce')} onMouseLeave={(e) => !loading && (e.currentTarget.style.background = '#9333ea')}
            >
              {loading ? 'Requesting...' : 'Request Procedure'}
            </button>
          </form>
        </div>
      )}

      {/* Lab Test Tab */}
      {activeTab === 'lab-tests' && (
        <div className="bg-white rounded-lg shadow p-6">
          <h3 style={{ fontSize: '20px', fontWeight: 'normal', marginBottom: '16px' }}>Request Lab Test</h3>
          <form onSubmit={async (e) => {
            e.preventDefault();
            setError(null);

            if (!labTestForm.testId) {
              setError('Please select a test');
              return;
            }

            try {
              setLoading(true);
              const doctorId = localStorage.getItem('doctorId') || 1;

              const request = {
                patientId: record.patientId,
                medicalRecordId: record.recordId,
                doctorId: parseInt(doctorId),
                testId: parseInt(labTestForm.testId),
                testDate: labTestForm.testDate,
                notes: labTestForm.notes,
              };

              await labService.requestLabTest(request);

              setSuccess('Lab test requested successfully');
              setLabTestForm({
                testId: '',
                testDate: new Date().toISOString().split('T')[0],
                notes: '',
              });

              setTimeout(() => setSuccess(null), 3000);
            } catch (err) {
              setError('Failed to request lab test: ' + (err.response?.data?.error || err.message));
            } finally {
              setLoading(false);
            }
          }} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Test</label>
                <select
                  value={labTestForm.testId}
                  onChange={(e) => setLabTestForm({ ...labTestForm, testId: e.target.value })}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select a test</option>
                  {labTests.map((test) => (
                    <option key={test.testId} value={test.testId}>
                      {test.testName} (${test.cost})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Test Date</label>
                <input
                  type="date"
                  value={labTestForm.testDate}
                  onChange={(e) => setLabTestForm({ ...labTestForm, testDate: e.target.value })}
                  className="w-full px-4 py-2 border rounded-lg"
                />
              </div>
            </div>
            <div>
              <label style={{ fontWeight: 'normal' }} className="block text-sm mb-2">Notes</label>
              <textarea
                value={labTestForm.notes}
                onChange={(e) => setLabTestForm({ ...labTestForm, notes: e.target.value })}
                placeholder="Additional notes for the lab technician"
                className="w-full px-4 py-2 border rounded-lg"
                rows="3"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              style={{background: loading ? '#9ca3af' : '#9333ea', color: 'white', padding: '6px 14px', borderRadius: '6px', border: 'none', cursor: 'pointer', fontSize: '14px', fontWeight: '600'}} onMouseEnter={(e) => !loading && (e.currentTarget.style.background = '#7e22ce')} onMouseLeave={(e) => !loading && (e.currentTarget.style.background = '#9333ea')}
            >
              {loading ? 'Requesting...' : 'Request Test'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default MedicalRecordDetail;
