import React, { useState, useEffect } from 'react';
import { medicalReportService } from '../../services/medicalReportService';
import { patientService } from '../../services/patientService';
import { doctorService } from '../../services/doctorService';
import { medicalRecordService } from '../../services/medicalRecordService';
import { medicalItemsService } from '../../services/medicalItemsService';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';

function MedicalReportList() {
  const [reports, setReports] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [selectedReport, setSelectedReport] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [searchEmbg, setSearchEmbg] = useState('');
  const [searchedPatient, setSearchedPatient] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [loading, setLoading] = useState(false);

  const [availableDiagnoses, setAvailableDiagnoses] = useState([]);
  const [availablePrescriptions, setAvailablePrescriptions] = useState([]);
  const [availableAllergies, setAvailableAllergies] = useState([]);
  const [availableSymptoms, setAvailableSymptoms] = useState([]);

  const [selectedDiagnosisIds, setSelectedDiagnosisIds] = useState(new Set());
  const [selectedPrescriptionIds, setSelectedPrescriptionIds] = useState(new Set());
  const [selectedAllergyIds, setSelectedAllergyIds] = useState(new Set());
  const [selectedSymptomIds, setSelectedSymptomIds] = useState(new Set());

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isDoctor = user.role === 'DOCTOR';

  const [formData, setFormData] = useState({
    doctorId: isDoctor ? user.doctorId : '',
    description: '',
    reportDate: new Date().toISOString().split('T')[0],
  });

  useEffect(() => {
    if (!isDoctor) {
      loadDoctors();
    }
  }, [isDoctor]);

  const loadDoctors = async () => {
    try {
      const res = await doctorService.getAllDoctors();
      setDoctors(res.data);
    } catch (err) {
      console.error('Error loading doctors:', err);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (!searchEmbg.trim()) {
        setError('Please enter a patient EMBG');
        setLoading(false);
        return;
      }

      const patientRes = await patientService.getPatientByEmbg(searchEmbg);
      setSearchedPatient(patientRes.data);

      const medicalRecordRes = await medicalRecordService.getMedicalRecordByPatientId(patientRes.data.patientId);
      const medicalRecordId = medicalRecordRes.data.recordId;
      const patientId = patientRes.data.patientId;

      const reportsRes = await medicalReportService.getReportsForMedicalRecord(medicalRecordId);
      setReports(Array.isArray(reportsRes.data) ? reportsRes.data : [reportsRes.data]);

      // Fetch available medical items
      try {
        const diagnosesRes = await medicalItemsService.getDiagnosesForPatient(patientId);
        setAvailableDiagnoses(diagnosesRes.data || []);
      } catch (err) {
        console.error('Error loading diagnoses:', err);
        setAvailableDiagnoses([]);
      }

      try {
        const prescriptionsRes = await medicalItemsService.getPrescriptionsForMedicalRecord(medicalRecordId);
        setAvailablePrescriptions(prescriptionsRes.data || []);
      } catch (err) {
        console.error('Error loading prescriptions:', err);
        setAvailablePrescriptions([]);
      }

      try {
        const allergiesRes = await medicalItemsService.getAllergiesForMedicalRecord(medicalRecordId);
        setAvailableAllergies(allergiesRes.data || []);
      } catch (err) {
        console.error('Error loading allergies:', err);
        setAvailableAllergies([]);
      }

      try {
        const symptomsRes = await medicalItemsService.getSymptomsForMedicalRecord(medicalRecordId);
        setAvailableSymptoms(symptomsRes.data || []);
      } catch (err) {
        console.error('Error loading symptoms:', err);
        setAvailableSymptoms([]);
      }

      setSelectedReport(null);
      setSelectedDiagnosisIds(new Set());
      setSelectedPrescriptionIds(new Set());
      setSelectedAllergyIds(new Set());
      setSelectedSymptomIds(new Set());
    } catch (err) {
      setError('Patient not found or no reports available');
      setReports([]);
      setSearchedPatient(null);
      setAvailableDiagnoses([]);
      setAvailablePrescriptions([]);
      setAvailableAllergies([]);
      setAvailableSymptoms([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateReport = async (e) => {
    e.preventDefault();
    setError(null);

    try {
      if (!formData.doctorId || !formData.description || !formData.reportDate) {
        setError('Please fill in all required fields');
        return;
      }

      if (!searchedPatient) {
        setError('Please search for a patient first');
        return;
      }

      const medicalRecordRes = await medicalRecordService.getMedicalRecordByPatientId(searchedPatient.patientId);

      const reportData = {
        doctorId: parseInt(formData.doctorId),
        medicalRecordId: medicalRecordRes.data.recordId,
        description: formData.description,
        reportDate: formData.reportDate,
        selectedDiagnosisIds: Array.from(selectedDiagnosisIds),
        selectedPrescriptionIds: Array.from(selectedPrescriptionIds),
        selectedAllergyIds: Array.from(selectedAllergyIds),
        selectedSymptomIds: Array.from(selectedSymptomIds),
      };

      const newReport = await medicalReportService.createReport(reportData);
      setSuccess('Medical report created successfully!');
      setReports([...reports, newReport.data]);
      setFormData({
        doctorId: '',
        description: '',
        reportDate: new Date().toISOString().split('T')[0],
      });
      setShowCreateForm(false);
      setSelectedReport(newReport.data);
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create medical report');
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const toggleDiagnosisSelection = (diagnosisId) => {
    setSelectedDiagnosisIds(prev => {
      const newSet = new Set(prev);
      if (newSet.has(diagnosisId)) {
        newSet.delete(diagnosisId);
      } else {
        newSet.add(diagnosisId);
      }
      return newSet;
    });
  };

  const togglePrescriptionSelection = (prescriptionId) => {
    setSelectedPrescriptionIds(prev => {
      const newSet = new Set(prev);
      if (newSet.has(prescriptionId)) {
        newSet.delete(prescriptionId);
      } else {
        newSet.add(prescriptionId);
      }
      return newSet;
    });
  };

  const toggleAllergySelection = (allergyId) => {
    setSelectedAllergyIds(prev => {
      const newSet = new Set(prev);
      if (newSet.has(allergyId)) {
        newSet.delete(allergyId);
      } else {
        newSet.add(allergyId);
      }
      return newSet;
    });
  };

  const toggleSymptomSelection = (symptomId) => {
    setSelectedSymptomIds(prev => {
      const newSet = new Set(prev);
      if (newSet.has(symptomId)) {
        newSet.delete(symptomId);
      } else {
        newSet.add(symptomId);
      }
      return newSet;
    });
  };

  return (
    <div>
      <h1 className="text-3xl font-bold mb-6" style={{ color: '#7c3aed' }}>Medical Reports</h1>

      {error && <ErrorAlert message={error} onClose={() => setError(null)} />}
      {success && <SuccessAlert message={success} onClose={() => setSuccess(null)} />}

      {/* Search Patient Form */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-bold mb-4">Search Patient by EMBG</h2>
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
            className="bg-purple-600 text-white px-8 py-2 rounded-full hover:bg-purple-700 disabled:bg-gray-400"
          >
            {loading ? 'Searching...' : 'Search'}
          </button>
        </form>
      </div>

      {/* Create Report Button */}
      {searchedPatient && (
        <div className="mb-6">
          <button
            onClick={() => setShowCreateForm(!showCreateForm)}
            className="bg-green-600 text-white px-6 py-2 rounded hover:bg-green-700"
          >
            {showCreateForm ? 'Cancel' : 'Create New Report'}
          </button>
        </div>
      )}

      {/* Create Report Form */}
      {showCreateForm && searchedPatient && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-xl font-bold mb-4">Create Medical Report</h2>
          <p className="mb-4 text-gray-600">
            Patient: {searchedPatient.firstName} {searchedPatient.lastName} ({searchedPatient.embg})
          </p>
          <form onSubmit={handleCreateReport} className="space-y-4">
            {isDoctor ? (
              <div>
                <label className="block text-sm font-semibold mb-2">Doctor</label>
                <p className="px-4 py-2 bg-gray-100 rounded-lg text-gray-700">
                  Dr. {user.firstName} {user.lastName}
                </p>
              </div>
            ) : (
              <div>
                <label className="block text-sm font-semibold mb-2">Doctor *</label>
                <select
                  name="doctorId"
                  value={formData.doctorId}
                  onChange={handleFormChange}
                  className="w-full px-4 py-2 border rounded-lg"
                  required
                >
                  <option value="">Select doctor</option>
                  {doctors.map(doctor => (
                    <option key={doctor.doctorId} value={doctor.doctorId}>
                      Dr. {doctor.firstName} {doctor.lastName}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div>
              <label className="block text-sm font-semibold mb-2">Report Date *</label>
              <input
                type="date"
                name="reportDate"
                value={formData.reportDate}
                onChange={handleFormChange}
                className="w-full px-4 py-2 border rounded-lg"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-semibold mb-2">Report Description *</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleFormChange}
                placeholder="Describe the patient's visit, findings, and recommendations..."
                className="w-full px-4 py-2 border rounded-lg"
                rows="6"
                required
              />
            </div>

            <button
              type="submit"
              className="bg-purple-600 text-white px-6 py-2 rounded hover:bg-purple-700"
            >
              Create Report
            </button>
          </form>
        </div>
      )}

      {/* Reports List */}
      {reports.length > 0 && (
        <div className="space-y-4">
          <h2 className="text-2xl font-bold">Reports for {searchedPatient?.firstName} {searchedPatient?.lastName}</h2>
          {reports.map(report => (
            <div
              key={report.reportId}
              className="bg-white rounded-lg shadow p-6 cursor-pointer hover:shadow-lg transition"
              onClick={() => setSelectedReport(selectedReport?.reportId === report.reportId ? null : report)}
            >
              <div className="flex justify-between items-start">
                <div>
                  <p className="text-sm text-gray-600">Date: {report.reportDate}</p>
                  <p className="text-sm text-gray-600">Doctor: {report.doctorName}</p>
                  <p className="mt-2 line-clamp-2">{report.reportDescription}</p>
                </div>
                <button
                  className="text-purple-600 hover:text-purple-800 text-sm font-normal"
                >
                  {selectedReport?.reportId === report.reportId ? 'Hide' : 'View'} Details
                </button>
              </div>

              {selectedReport?.reportId === report.reportId && (
                <div className="mt-6 pt-6 border-t space-y-6">
                  {/* Report Description */}
                  <div>
                    <h4 className="font-bold mb-2">Report Description</h4>
                    <p className="text-gray-700 whitespace-pre-wrap">{report.reportDescription}</p>
                  </div>

                  {/* Diagnoses */}
                  {report.diagnoses && report.diagnoses.length > 0 && (
                    <div>
                      <h4 className="font-bold mb-2">Diagnoses</h4>
                      <ul className="space-y-2">
                        {report.diagnoses.map((diagnosis, idx) => (
                          <li key={idx} className="bg-blue-50 p-3 rounded">
                            <p className="font-semibold">{diagnosis.name}</p>
                            <p className="text-sm text-gray-600">{diagnosis.description}</p>
                            <p className="text-xs text-gray-500">By: {diagnosis.doctorName}</p>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {/* Prescriptions */}
                  {report.prescriptions && report.prescriptions.length > 0 && (
                    <div>
                      <h4 className="font-bold mb-2">Prescriptions</h4>
                      <ul className="space-y-2">
                        {report.prescriptions.map((prescription, idx) => (
                          <li key={idx} className="bg-green-50 p-3 rounded">
                            <p className="font-semibold">{prescription.medicationName}</p>
                            <p className="text-sm text-gray-600">
                              {prescription.dosage} - {prescription.frequency} for {prescription.duration}
                            </p>
                            {prescription.notes && (
                              <p className="text-sm text-gray-600">Notes: {prescription.notes}</p>
                            )}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {/* Allergies */}
                  {report.allergies && report.allergies.length > 0 && (
                    <div>
                      <h4 className="font-bold mb-2">Allergies</h4>
                      <ul className="space-y-2">
                        {report.allergies.map((allergy, idx) => (
                          <li key={idx} className="bg-red-50 p-3 rounded">
                            <p className="font-semibold">{allergy.allergyName}</p>
                            <p className="text-sm text-gray-600">Reaction: {allergy.reaction}</p>
                            <p className="text-xs text-red-600">Severity: {allergy.severity}</p>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {/* Symptoms */}
                  {report.symptoms && report.symptoms.length > 0 && (
                    <div>
                      <h4 className="font-bold mb-2">Symptoms</h4>
                      <ul className="space-y-2">
                        {report.symptoms.map((symptom, idx) => (
                          <li key={idx} className="bg-yellow-50 p-3 rounded">
                            <p className="font-semibold">{symptom.symptomName}</p>
                            <p className="text-sm text-gray-600">{symptom.description}</p>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* No Reports Message */}
      {searchedPatient && reports.length === 0 && (
        <div className="bg-blue-50 rounded-lg p-6 text-center">
          <p className="text-gray-600">No medical reports found for this patient</p>
        </div>
      )}
    </div>
  );
}

export default MedicalReportList;
