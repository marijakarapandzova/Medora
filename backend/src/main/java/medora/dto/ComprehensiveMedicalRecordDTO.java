package medora.dto;

import java.util.List;

public class ComprehensiveMedicalRecordDTO {

    public static class DiagnosisDTO {
        private Long diagnosisId;
        private String name;
        private String description;
        private String doctorName;

        public DiagnosisDTO(Long diagnosisId, String name, String description, String doctorName) {
            this.diagnosisId = diagnosisId;
            this.name = name;
            this.description = description;
            this.doctorName = doctorName;
        }

        public Long getDiagnosisId() { return diagnosisId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getDoctorName() { return doctorName; }
    }
    private Long recordId;
    private Long patientId;
    private String patientName;
    private String embg;
    private List<DiagnosisDTO> diagnoses;
    private List<SymptomDTO> symptoms;
    private List<AllergyDTO> allergies;
    private List<PrescriptionDTO> prescriptions;
    private List<MedicalReportDTO> reports;

    public ComprehensiveMedicalRecordDTO(Long recordId, Long patientId, String patientName, String embg,
                                        List<DiagnosisDTO> diagnoses, List<SymptomDTO> symptoms,
                                        List<AllergyDTO> allergies, List<PrescriptionDTO> prescriptions,
                                        List<MedicalReportDTO> reports) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.embg = embg;
        this.diagnoses = diagnoses;
        this.symptoms = symptoms;
        this.allergies = allergies;
        this.prescriptions = prescriptions;
        this.reports = reports;
    }

    public Long getRecordId() { return recordId; }
    public Long getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getEmbg() { return embg; }
    public List<DiagnosisDTO> getDiagnoses() { return diagnoses; }
    public List<SymptomDTO> getSymptoms() { return symptoms; }
    public List<AllergyDTO> getAllergies() { return allergies; }
    public List<PrescriptionDTO> getPrescriptions() { return prescriptions; }
    public List<MedicalReportDTO> getReports() { return reports; }

    public static class SymptomDTO {
        private Long symptomId;
        private String symptomName;

        public SymptomDTO(Long symptomId, String symptomName) {
            this.symptomId = symptomId;
            this.symptomName = symptomName;
        }

        public Long getSymptomId() { return symptomId; }
        public String getSymptomName() { return symptomName; }
    }

    public static class AllergyDTO {
        private Long allergyId;
        private String allergyName;
        private String severity;
        private String reaction;

        public AllergyDTO(Long allergyId, String allergyName, String severity, String reaction) {
            this.allergyId = allergyId;
            this.allergyName = allergyName;
            this.severity = severity;
            this.reaction = reaction;
        }

        public Long getAllergyId() { return allergyId; }
        public String getAllergyName() { return allergyName; }
        public String getSeverity() { return severity; }
        public String getReaction() { return reaction; }
    }

    public static class PrescriptionDTO {
        private Long prescriptionId;
        private String medicationName;
        private String dosage;
        private String frequency;
        private String duration;

        public PrescriptionDTO(Long prescriptionId, String medicationName, String dosage, String frequency, String duration) {
            this.prescriptionId = prescriptionId;
            this.medicationName = medicationName;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
        }

        public Long getPrescriptionId() { return prescriptionId; }
        public String getMedicationName() { return medicationName; }
        public String getDosage() { return dosage; }
        public String getFrequency() { return frequency; }
        public String getDuration() { return duration; }
    }

    public static class MedicalReportDTO {
        private Long reportId;
        private String doctorName;
        private String description;
        private String reportDate;

        public MedicalReportDTO(Long reportId, String doctorName, String description, String reportDate) {
            this.reportId = reportId;
            this.doctorName = doctorName;
            this.description = description;
            this.reportDate = reportDate;
        }

        public Long getReportId() { return reportId; }
        public String getDoctorName() { return doctorName; }
        public String getDescription() { return description; }
        public String getReportDate() { return reportDate; }
    }
}
