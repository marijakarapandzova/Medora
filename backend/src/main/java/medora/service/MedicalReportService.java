package medora.service;

import medora.models.domain.*;
import medora.dto.*;
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
 *
 * NOTE: This service was refactored to match the final database schema, which
 * has no report_diagnosis / report_prescription / report_allergy / report_symptom
 * junction tables. A medical report therefore no longer stores its own
 * per-report subset of diagnoses/prescriptions/allergies/symptoms — instead,
 * a "comprehensive report" simply reflects everything already linked to the
 * report's underlying medical record (via Diagnosis.patient, PrescriptionMedicalRecord,
 * MedicalRecordAllergies, and MedicalRecordSymptoms), the same way the record's
 * data is shown elsewhere in the application.
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

    public MedicalReportService(MedicalReportRepository medicalReportRepository,
                                DoctorRepository doctorRepository,
                                MedicalRecordRepository medicalRecordRepository,
                                DiagnosisRepository diagnosisRepository,
                                PrescriptionMedicalRecordRepository prescriptionMedicalRecordRepository,
                                MedicalRecordAllergyRepository medicalRecordAllergyRepository,
                                MedicalRecordSymptomRepository symptomRepository) {
        this.medicalReportRepository = medicalReportRepository;
        this.doctorRepository = doctorRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.prescriptionMedicalRecordRepository = prescriptionMedicalRecordRepository;
        this.medicalRecordAllergyRepository = medicalRecordAllergyRepository;
        this.symptomRepository = symptomRepository;
    }

    /**
     * UC018 – Create Medical Report
     * Create a medical report describing a patient's visit and condition.
     * report_id is not database-generated, so the next free ID is computed here.
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
        Long nextReportId = medicalReportRepository.findMaxReportId() + 1;
        report.setReportId(nextReportId);
        report.setDoctor(doctor);
        report.setMedicalRecord(medicalRecord);
        report.setDescription(description);
        report.setReportDate(reportDate);

        logger.info("Creating medical report for medical record ID: {} by doctor ID: {}",
                medicalRecordId, doctorId);
        return medicalReportRepository.save(report);
    }

    /**
     * Create a medical report, optionally validating that the given diagnosis,
     * prescription, allergy, and symptom IDs are actually linked to the medical
     * record. The schema has no table to persist a report-specific subset of
     * these items, so they are only used here as a validation step — the
     * resulting comprehensive report will reflect everything on the record,
     * not just the IDs passed in.
     */
    @Transactional
    public MedicalReport createMedicalReportWithSelectedItems(Long doctorId, Long medicalRecordId,
            String description, LocalDate reportDate,
            List<Long> selectedDiagnosisIds, List<Long> selectedPrescriptionIds,
            List<Long> selectedAllergyIds, List<Long> selectedSymptomIds) {

        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + medicalRecordId));

        if (selectedDiagnosisIds != null) {
            for (Long diagnosisId : selectedDiagnosisIds) {
                diagnosisRepository.findById(diagnosisId)
                        .orElseThrow(() -> new RuntimeException("Diagnosis not found with ID: " + diagnosisId));
            }
        }

        if (selectedPrescriptionIds != null) {
            for (Long prescriptionId : selectedPrescriptionIds) {
                PrescriptionMedicalRecord pmr = prescriptionMedicalRecordRepository
                        .findByMedicalRecordAndPrescription(medicalRecordId, prescriptionId);
                if (pmr == null) {
                    throw new RuntimeException("Prescription " + prescriptionId
                            + " is not linked to medical record " + medicalRecordId);
                }
            }
        }

        if (selectedAllergyIds != null) {
            for (Long allergyId : selectedAllergyIds) {
                boolean exists = medicalRecordAllergyRepository
                        .existsByMedicalRecordRecordIdAndAllergyAllergyId(medicalRecordId, allergyId);
                if (!exists) {
                    throw new RuntimeException("Allergy " + allergyId
                            + " is not linked to medical record " + medicalRecordId);
                }
            }
        }

        if (selectedSymptomIds != null) {
            for (Long symptomId : selectedSymptomIds) {
                boolean exists = symptomRepository
                        .existsByMedicalRecordRecordIdAndSymptomSymptomId(medicalRecordId, symptomId);
                if (!exists) {
                    throw new RuntimeException("Symptom " + symptomId
                            + " is not linked to medical record " + medicalRecordId);
                }
            }
        }

        logger.info("Creating medical report for record ID: {} (validated {} diagnoses, {} prescriptions, "
                        + "{} allergies, {} symptoms already on the record)", medicalRecordId,
                selectedDiagnosisIds == null ? 0 : selectedDiagnosisIds.size(),
                selectedPrescriptionIds == null ? 0 : selectedPrescriptionIds.size(),
                selectedAllergyIds == null ? 0 : selectedAllergyIds.size(),
                selectedSymptomIds == null ? 0 : selectedSymptomIds.size());

        return createMedicalReport(doctorId, medicalRecordId, description, reportDate);
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
     * Build a comprehensive report DTO with all medical data linked to the
     * report's underlying medical record (diagnoses by patient, prescriptions,
     * allergies, and symptoms already recorded on that record).
     */
    private ComprehensiveMedicalReportDTO buildComprehensiveReport(MedicalReport report) {
        Long medicalRecordId = report.getMedicalRecord().getRecordId();
        Patient patient = report.getMedicalRecord().getPatient();

        // Diagnoses: diagnosis links directly to a patient in this schema,
        // there is no separate report-specific selection table.
        List<Diagnosis> diagnoses = diagnosisRepository.findByPatientPatientId(patient.getPatientId());

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

        // Prescriptions already linked to this medical record.
        List<PrescriptionMedicalRecord> prescriptions =
                prescriptionMedicalRecordRepository.findByMedicalRecordRecordId(medicalRecordId);

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

        // Allergies already linked to this medical record.
        List<MedicalRecordAllergies> allergies =
                medicalRecordAllergyRepository.findByMedicalRecordRecordId(medicalRecordId);

        List<AllergyDTO> allergyDTOs = allergies.stream()
                .map(a -> new AllergyDTO(
                        a.getAllergy().getAllergyId(),
                        a.getAllergy().getName(),
                        a.getReaction(),
                        a.getSeverity()
                ))
                .collect(Collectors.toList());

        // Symptoms already linked to this medical record.
        List<MedicalRecordSymptoms> symptoms =
                symptomRepository.findByMedicalRecordRecordId(medicalRecordId);

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
