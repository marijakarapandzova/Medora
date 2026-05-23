package medora.service;

import medora.dto.*;
import medora.models.domain.*;
import medora.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MedicalReportService handles medical report operations.
 * UC018 – Create Medical Report
 * OPTIONAL - Use only if needed
 */
@Service
public class MedicalReportService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalReportService.class);

    private final MedicalReportRepository medicalReportRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final PrescriptionMedicalRecordRepository prescriptionMedicalRecordRepository;
    private final MedicalRecordAllergyRepository medicalRecordAllergyRepository;
    private final MedicalRecordSymptomRepository symptomRepository;
    private final ReportDiagnosisRepository reportDiagnosisRepository;
    private final ReportPrescriptionRepository reportPrescriptionRepository;
    private final ReportAllergyRepository reportAllergyRepository;
    private final ReportSymptomRepository reportSymptomRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AllergyRepository allergyRepository;
    private final SymptomRepository symptomDbRepository;

    public MedicalReportService(MedicalReportRepository medicalReportRepository,
                                DoctorRepository doctorRepository,
                                MedicalRecordRepository medicalRecordRepository,
                                DiagnosisRepository diagnosisRepository,
                                PrescriptionMedicalRecordRepository prescriptionMedicalRecordRepository,
                                MedicalRecordAllergyRepository medicalRecordAllergyRepository,
                                MedicalRecordSymptomRepository symptomRepository,
                                ReportDiagnosisRepository reportDiagnosisRepository,
                                ReportPrescriptionRepository reportPrescriptionRepository,
                                ReportAllergyRepository reportAllergyRepository,
                                ReportSymptomRepository reportSymptomRepository,
                                PrescriptionRepository prescriptionRepository,
                                AllergyRepository allergyRepository,
                                SymptomRepository symptomDbRepository) {
        this.medicalReportRepository = medicalReportRepository;
        this.doctorRepository = doctorRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.prescriptionMedicalRecordRepository = prescriptionMedicalRecordRepository;
        this.medicalRecordAllergyRepository = medicalRecordAllergyRepository;
        this.symptomRepository = symptomRepository;
        this.reportDiagnosisRepository = reportDiagnosisRepository;
        this.reportPrescriptionRepository = reportPrescriptionRepository;
        this.reportAllergyRepository = reportAllergyRepository;
        this.reportSymptomRepository = reportSymptomRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.allergyRepository = allergyRepository;
        this.symptomDbRepository = symptomDbRepository;
    }

    /**
     * UC018 – Create Medical Report
     * Create a medical report describing a patient's visit and condition
     */
    @Transactional
    public MedicalReport createMedicalReport(Long doctorId, Long medicalRecordId, String description,
                                             LocalDate reportDate) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Report description is required");
        }
        if (reportDate == null) {
            throw new IllegalArgumentException("Report date is required");
        }

        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + medicalRecordId));

        MedicalReport report = new MedicalReport();
        // Let JPA auto-generate the ID using the sequence
        report.setReportId(null);
        report.setDoctor(doctor);
        report.setMedicalRecord(medicalRecord);
        report.setDescription(description);
        report.setReportDate(reportDate);

        logger.info("Creating medical report for medical record ID: {} by doctor ID: {}",
                medicalRecordId, doctorId);
        return medicalReportRepository.save(report);
    }

    /**
     * Create a medical report with selected diagnoses, prescriptions, allergies, and symptoms
     */
    @Transactional
    public MedicalReport createMedicalReportWithSelectedItems(Long doctorId, Long medicalRecordId,
            String description, LocalDate reportDate,
            List<Long> selectedDiagnosisIds, List<Long> selectedPrescriptionIds,
            List<Long> selectedAllergyIds, List<Long> selectedSymptomIds) {

        // Create the base report
        MedicalReport report = createMedicalReport(doctorId, medicalRecordId, description, reportDate);

        // Store selected diagnoses
        if (selectedDiagnosisIds != null) {
            for (Long diagnosisId : selectedDiagnosisIds) {
                Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                        .orElseThrow(() -> new RuntimeException("Diagnosis not found with ID: " + diagnosisId));
                ReportDiagnosis reportDiagnosis = new ReportDiagnosis(report, diagnosis);
                reportDiagnosisRepository.save(reportDiagnosis);
            }
        }

        // Store selected prescriptions
        if (selectedPrescriptionIds != null) {
            for (Long prescriptionId : selectedPrescriptionIds) {
                Prescriptions prescription = prescriptionRepository.findById(prescriptionId)
                        .orElseThrow(() -> new RuntimeException("Prescription not found with ID: " + prescriptionId));
                ReportPrescription reportPrescription = new ReportPrescription(report, prescription);
                reportPrescriptionRepository.save(reportPrescription);
            }
        }

        // Store selected allergies
        if (selectedAllergyIds != null) {
            for (Long allergyId : selectedAllergyIds) {
                Allergies allergy = allergyRepository.findById(allergyId)
                        .orElseThrow(() -> new RuntimeException("Allergy not found with ID: " + allergyId));
                ReportAllergy reportAllergy = new ReportAllergy(report, allergy);
                reportAllergyRepository.save(reportAllergy);
            }
        }

        // Store selected symptoms
        if (selectedSymptomIds != null) {
            for (Long symptomId : selectedSymptomIds) {
                Symptoms symptom = symptomDbRepository.findById(symptomId)
                        .orElseThrow(() -> new RuntimeException("Symptom not found with ID: " + symptomId));
                ReportSymptom reportSymptom = new ReportSymptom(report, symptom);
                reportSymptomRepository.save(reportSymptom);
            }
        }

        logger.info("Created medical report with selected items for record ID: {}", medicalRecordId);
        return report;
    }

    /**
     * Get medical report by ID
     */
    @Transactional(readOnly = true)
    public Optional<MedicalReport> getMedicalReportById(Long reportId) {
        if (reportId == null || reportId <= 0) {
            throw new IllegalArgumentException("Report ID must be valid");
        }
        logger.info("Fetching medical report with ID: {}", reportId);
        return medicalReportRepository.findById(reportId);
    }

    /**
     * Get all reports for a medical record
     */
    @Transactional(readOnly = true)
    public List<MedicalReport> getReportsForMedicalRecord(Long medicalRecordId) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }

        if (!medicalRecordRepository.existsById(medicalRecordId)) {
            throw new RuntimeException("Medical record not found with ID: " + medicalRecordId);
        }

        logger.info("Fetching reports for medical record ID: {}", medicalRecordId);
        return medicalReportRepository.findByMedicalRecordRecordId(medicalRecordId);
    }

    /**
     * Get all reports created by a doctor
     */
    @Transactional(readOnly = true)
    public List<MedicalReport> getReportsByDoctor(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }

        if (!doctorRepository.existsById(doctorId)) {
            throw new RuntimeException("Doctor not found with ID: " + doctorId);
        }

        logger.info("Fetching reports created by doctor ID: {}", doctorId);
        return medicalReportRepository.findByDoctorDoctorId(doctorId);
    }

    /**
     * Update medical report
     */
    @Transactional
    public MedicalReport updateMedicalReport(Long reportId, String description) {
        if (reportId == null || reportId <= 0) {
            throw new IllegalArgumentException("Report ID must be valid");
        }

        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Medical report not found with ID: " + reportId));

        if (description != null && !description.isBlank()) {
            report.setDescription(description);
        }

        logger.info("Updating medical report with ID: {}", reportId);
        return medicalReportRepository.save(report);
    }

    /**
     * Delete medical report
     */
    @Transactional
    public void deleteMedicalReport(Long reportId) {
        if (reportId == null || reportId <= 0) {
            throw new IllegalArgumentException("Report ID must be valid");
        }

        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Medical report not found with ID: " + reportId));

        logger.info("Deleting medical report with ID: {}", reportId);
        medicalReportRepository.delete(report);
    }

    /**
     * Get comprehensive medical report with all patient medical data
     */
    @Transactional(readOnly = true)
    public ComprehensiveMedicalReportDTO getComprehensiveReport(Long reportId) {
        if (reportId == null || reportId <= 0) {
            throw new IllegalArgumentException("Report ID must be valid");
        }

        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Medical report not found with ID: " + reportId));

        return buildComprehensiveReport(report);
    }

    /**
     * Get all comprehensive reports for a medical record
     */
    @Transactional(readOnly = true)
    public List<ComprehensiveMedicalReportDTO> getComprehensiveReportsForMedicalRecord(Long medicalRecordId) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }

        List<MedicalReport> reports = medicalReportRepository.findByMedicalRecordRecordId(medicalRecordId);
        return reports.stream()
                .map(this::buildComprehensiveReport)
                .collect(Collectors.toList());
    }

    /**
     * Build a comprehensive report DTO with all medical data
     */
    private ComprehensiveMedicalReportDTO buildComprehensiveReport(MedicalReport report) {
        Long medicalRecordId = report.getMedicalRecord().getRecordId();
        Patient patient = report.getMedicalRecord().getPatient();

        // Get selected diagnoses from linking table
        List<ReportDiagnosis> reportDiagnoses = reportDiagnosisRepository.findByReportReportId(report.getReportId());
        List<Diagnosis> diagnoses = reportDiagnoses.stream()
                .map(ReportDiagnosis::getDiagnosis)
                .collect(Collectors.toList());

        List<DiagnosisDTO> diagnosisDTOs = diagnoses.stream()
                .map(d -> new DiagnosisDTO(
                        d.getDiagnosisId(),
                        patient.getPatientId(),
                        patient.getFirstName() + " " + patient.getLastName(),
                        d.getDoctor().getDoctorId(),
                        d.getDoctor().getFirstName() + " " + d.getDoctor().getLastName(),
                        d.getName(),
                        d.getDescription()
                ))
                .collect(Collectors.toList());

        // Get selected prescriptions from linking table
        List<ReportPrescription> reportPrescriptions = reportPrescriptionRepository.findByReportReportId(report.getReportId());
        List<PrescriptionMedicalRecord> prescriptions = new java.util.ArrayList<>();
        for (ReportPrescription rp : reportPrescriptions) {
            // Find the corresponding PrescriptionMedicalRecord entry
            List<PrescriptionMedicalRecord> pmr = prescriptionMedicalRecordRepository.findByMedicalRecordRecordIdAndPrescriptionPrescriptionId(
                    medicalRecordId, rp.getPrescription().getPrescriptionId());
            prescriptions.addAll(pmr);
        }

        List<PrescriptionDTO> prescriptionDTOs = prescriptions.stream()
                .map(p -> new PrescriptionDTO(
                        p.getPrescription().getPrescriptionId(),
                        medicalRecordId,
                        p.getPrescription().getMedicationName(),
                        p.getDosage(),
                        p.getFrequency(),
                        p.getDuration(),
                        p.getNotes()
                ))
                .collect(Collectors.toList());

        // Get selected allergies from linking table
        List<ReportAllergy> reportAllergies = reportAllergyRepository.findByReportReportId(report.getReportId());
        List<MedicalRecordAllergies> allergies = new java.util.ArrayList<>();
        for (ReportAllergy ra : reportAllergies) {
            // Find the corresponding MedicalRecordAllergies entry
            List<MedicalRecordAllergies> mra = medicalRecordAllergyRepository.findByMedicalRecordRecordIdAndAllergyAllergyId(
                    medicalRecordId, ra.getAllergy().getAllergyId());
            allergies.addAll(mra);
        }

        List<AllergyDTO> allergyDTOs = allergies.stream()
                .map(a -> new AllergyDTO(
                        a.getAllergy().getAllergyId(),
                        a.getAllergy().getName(),
                        a.getReaction(),
                        a.getSeverity()
                ))
                .collect(Collectors.toList());

        // Get selected symptoms from linking table
        List<ReportSymptom> reportSymptoms = reportSymptomRepository.findByReportReportId(report.getReportId());
        List<MedicalRecordSymptoms> symptoms = new java.util.ArrayList<>();
        for (ReportSymptom rs : reportSymptoms) {
            // Find the corresponding MedicalRecordSymptoms entry
            List<MedicalRecordSymptoms> mrs = symptomRepository.findByMedicalRecordRecordIdAndSymptomSymptomId(
                    medicalRecordId, rs.getSymptom().getSymptomId());
            symptoms.addAll(mrs);
        }

        List<SymptomDTO> symptomDTOs = symptoms.stream()
                .map(s -> new SymptomDTO(
                        s.getSymptom().getSymptomId(),
                        s.getSymptom().getName(),
                        s.getSymptom().getDescription()
                ))
                .collect(Collectors.toList());

        return new ComprehensiveMedicalReportDTO(
                report.getReportId(),
                medicalRecordId,
                patient.getPatientId(),
                patient.getFirstName() + " " + patient.getLastName(),
                patient.getEmbg(),
                report.getReportDate(),
                report.getDoctor().getDoctorId(),
                report.getDoctor().getFirstName() + " " + report.getDoctor().getLastName(),
                report.getDescription(),
                diagnosisDTOs,
                prescriptionDTOs,
                allergyDTOs,
                symptomDTOs
        );
    }
}
