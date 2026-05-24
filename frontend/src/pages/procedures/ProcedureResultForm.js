import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { procedureService } from '../../services/procedureService';
import { patientService } from '../../services/patientService';
import { medicalRecordService } from '../../services/medicalRecordService';
import ErrorAlert from '../../components/ErrorAlert';
import SuccessAlert from '../../components/SuccessAlert';

function ProcedureResultForm() {
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searchEmbg, setSearchEmbg] = useState('');
  const [patient, setPatient] = useState(null);
  const [performedProcedures, setPerformedProcedures] = useState([]);
  const [selectedProcedure, setSelectedProcedure] = useState(null);
  const [medicalRecordId, setMedicalRecordId] = useState(null);

  const [formData, setFormData] = useState({
    medicalRecordId: '',
    procedureId: '',
    resultDescription: '',
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

      // Get performed procedures
      try {
        const proceduresRes = await procedureService.getPerformedProceduresForPatient(patientRes.data.patientId);
        setPerformedProcedures(Array.isArray(proceduresRes.data) ? proceduresRes.data : []);
      } catch (err) {
        console.error('Failed to fetch performed procedures:', err);
        setPerformedProcedures([]);
      }

      setFormData(prev => ({
        ...prev,
        medicalRecordId: medicalRecordRes.data.recordId
      }));
    } catch (err) {
      console.error('Search error:', err);
      setError(`Error: ${err.response?.data?.error || err.message || 'Patient not found'}`);
      setPatient(null);
      setPerformedProcedures([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectProcedure = (procedure) => {
    setSelectedProcedure(procedure);
    setFormData(prev => ({
      ...prev,
      procedureId: procedure.procedure?.procedureId || procedure.procedureId,
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

    if (!formData.medicalRecordId || !formData.procedureId || !formData.resultDescription) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setLoading(true);

      const submitData = {
        medicalRecordId: parseInt(formData.medicalRecordId),
        procedureId: parseInt(formData.procedureId),
        resultDescription: formData.resultDescription,
        resultDate: formData.resultDate,
      };

      await procedureService.submitProcedureResult(submitData);

      setSuccess('Procedure result submitted successfully!');
      setFormData({
        medicalRecordId: '',
        procedureId: '',
        resultDescription: '',
        resultDate: new Date().toISOString().split('T')[0],
      });
      setSelectedProcedure(null);
      setPatient(null);
      setSearchEmbg('');
      setPerformedProcedures([]);

      // Navigate back to procedures page after a short delay
      setTimeout(() => navigate('/procedures'), 2000);
    } catch (err) {
      setError('Failed to submit procedure result: ' + (err.response?.data?.error || err.message));
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">Submit Procedure Result</h1>

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

      {/* Patient Info and Performed Procedures */}
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

          {/* Performed Procedures List */}
          {performedProcedures.length > 0 ? (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-bold mb-4">Performed Procedures</h2>
              <div className="space-y-2">
                {performedProcedures.map((proc) => (
                  <div
                    key={proc.performedId}
                    onClick={() => handleSelectProcedure(proc)}
                    className={`p-4 rounded-lg border-2 cursor-pointer transition ${
                      selectedProcedure?.performedId === proc.performedId
                        ? 'border-purple-600 bg-blue-50'
                        : 'border-gray-200 bg-gray-50 hover:border-blue-400'
                    }`}
                  >
                    <p className="font-semibold text-lg">{proc.procedure?.procedureType || 'Procedure'}</p>
                    <p className="text-sm text-gray-600">Requested by: {proc.doctor?.firstName} {proc.doctor?.lastName}</p>
                    <p className="text-sm text-gray-600">Procedure Date: {proc.procedureDate}</p>
                    {proc.notes && (
                      <p className="text-sm text-gray-600 mt-1">Notes: {proc.notes}</p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="bg-blue-50 rounded-lg p-6 text-center">
              <p className="text-gray-600">No performed procedures for this patient</p>
            </div>
          )}

          {/* Result Submission Form */}
          {selectedProcedure && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-xl font-bold mb-4">Submit Result for {selectedProcedure.procedure?.procedureType}</h2>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold mb-2">Procedure ID</label>
                    <input
                      type="number"
                      value={formData.procedureId}
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
                  <label className="block text-sm font-semibold mb-2">Procedure Outcome *</label>
                  <textarea
                    name="resultDescription"
                    value={formData.resultDescription}
                    onChange={handleChange}
                    placeholder="Describe the procedure outcome and any findings"
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
                    className="flex-1 bg-purple-600 text-white py-2 rounded-lg hover:bg-purple-700 disabled:bg-gray-400"
                  >
                    {loading ? 'Submitting...' : 'Submit Result'}
                  </button>
                  <button
                    type="button"
                    onClick={() => navigate('/procedures')}
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
          <p className="text-gray-600 text-lg">Enter a patient EMBG to view performed procedures</p>
        </div>
      )}
    </div>
  );
}

export default ProcedureResultForm;
