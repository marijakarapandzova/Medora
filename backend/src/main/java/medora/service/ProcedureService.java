package medora.service;

import medora.dto.SimpleProcedureDTO;
import medora.models.domain.*;
import medora.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProcedureService {

    private static final Logger logger = LoggerFactory.getLogger(ProcedureService.class);

    private final PerformedProcedureRepository performedProcedureRepository;
    private final ProcedureRepository procedureRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordProcedureRepository medicalRecordProcedureRepository;
    private final ProcedureResultRepository procedureResultRepository;
    private final MedicalRecordProcedureResultRepository medicalRecordProcedureResultRepository;
    private final EntityManager entityManager;

    public ProcedureService(PerformedProcedureRepository performedProcedureRepository,
                            ProcedureRepository procedureRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            DiagnosisRepository diagnosisRepository,
                            MedicalRecordRepository medicalRecordRepository,
                            MedicalRecordProcedureRepository medicalRecordProcedureRepository,
                            ProcedureResultRepository procedureResultRepository,
                            MedicalRecordProcedureResultRepository medicalRecordProcedureResultRepository,
                            EntityManager entityManager) {

        this.performedProcedureRepository = performedProcedureRepository;
        this.procedureRepository = procedureRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.medicalRecordProcedureRepository = medicalRecordProcedureRepository;
        this.procedureResultRepository = procedureResultRepository;
        this.medicalRecordProcedureResultRepository = medicalRecordProcedureResultRepository;
        this.entityManager = entityManager;
    }

    // ================= REQUEST PROCEDURE (NEW) =================
    @Transactional
    public PerformedProcedures requestProcedureForPatient(Long patientId,
                                                          Long doctorId,
                                                          Long procedureId,
                                                          Long diagnosisId,
                                                          LocalDate procedureDate,
                                                          String notes) {

        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Patient ID must be valid");

        if (doctorId == null || doctorId <= 0)
            throw new IllegalArgumentException("Doctor ID must be valid");

        if (procedureId == null || procedureId <= 0)
            throw new IllegalArgumentException("Procedure ID must be valid");

        if (procedureDate == null)
            throw new IllegalArgumentException("Procedure date is required");

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new RuntimeException("Procedure not found"));

        PerformedProcedures performed = new PerformedProcedures();
        performed.setProcedure(procedure);
        performed.setDoctor(doctor);
        performed.setPatient(patient);
        performed.setProcedureDate(procedureDate);
        performed.setNotes(notes);

        if (diagnosisId != null && diagnosisId > 0) {
            Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                    .orElseThrow(() -> new RuntimeException("Diagnosis not found"));
            performed.setDiagnosis(diagnosis);
        }

        logger.info("Requested procedure {} for patient {} by doctor {}", procedureId, patientId, doctorId);
        return performedProcedureRepository.saveAndFlush(performed);
    }

    // ================= UC016 =================
    @Transactional
    public PerformedProcedures recordProcedure(Long procedureId,
                                               Long doctorId,
                                               Long patientId,
                                               Long diagnosisId,
                                               LocalDate procedureDate) {

        if (procedureId == null || procedureId <= 0)
            throw new IllegalArgumentException("Procedure ID must be valid");

        if (doctorId == null || doctorId <= 0)
            throw new IllegalArgumentException("Doctor ID must be valid");

        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Patient ID must be valid");

        if (procedureDate == null)
            throw new IllegalArgumentException("Procedure date is required");

        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new RuntimeException("Procedure not found"));

        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        PerformedProcedures performed = new PerformedProcedures();
        performed.setProcedure(procedure);
        performed.setDoctor(doctor);
        performed.setPatient(patient);
        performed.setProcedureDate(procedureDate);

        if (diagnosisId != null && diagnosisId > 0) {
            Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                    .orElseThrow(() -> new RuntimeException("Diagnosis not found"));
            performed.setDiagnosis(diagnosis);
        }

        logger.info("Recorded procedure {} for patient {}", procedureId, patientId);
        return performedProcedureRepository.saveAndFlush(performed);
    }

    // ================= UC017 =================
    @Transactional
    public PerformedProcedures recordProcedureOutcome(Long performedProcedureId, String notes) {

        if (performedProcedureId == null || performedProcedureId <= 0)
            throw new IllegalArgumentException("Performed procedure ID must be valid");

        PerformedProcedures performed = performedProcedureRepository.findById(performedProcedureId)
                .orElseThrow(() -> new RuntimeException("Performed procedure not found"));

        if (notes != null && !notes.isBlank()) {
            performed.setNotes(notes);
        }

        logger.info("Updated procedure outcome {}", performedProcedureId);
        return performedProcedureRepository.save(performed);
    }

    // ================= LINK TO MEDICAL RECORD =================
    @Transactional
    public MedicalRecordProcedures linkProcedureToMedicalRecord(Long medicalRecordId,
                                                                Long procedureId) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Medical record ID must be valid");

        if (procedureId == null || procedureId <= 0)
            throw new IllegalArgumentException("Procedure ID must be valid");

        MedicalRecord record = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new RuntimeException("Procedure not found"));

        // Prevent duplicates
        if (medicalRecordProcedureRepository
                .existsByMedicalRecordRecordIdAndProcedureProcedureId(medicalRecordId, procedureId)) {
            throw new RuntimeException("Procedure already linked to this medical record");
        }

        MedicalRecordProcedures link = new MedicalRecordProcedures(record, procedure);

        logger.info("Linked procedure {} to record {}", procedureId, medicalRecordId);
        return medicalRecordProcedureRepository.save(link);
    }

    // ================= READ METHODS =================
    @Transactional(readOnly = true)
    public List<PerformedProcedures> getProcedureRequestsForPatient(Long patientId) {

        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Patient ID must be valid");

        if (!patientRepository.existsById(patientId))
            throw new RuntimeException("Patient not found");

        return performedProcedureRepository.findByPatientPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<PerformedProcedures> getProcedureRequestsByDoctor(Long doctorId) {

        if (doctorId == null || doctorId <= 0)
            throw new IllegalArgumentException("Doctor ID must be valid");

        if (!doctorRepository.existsById(doctorId))
            throw new RuntimeException("Doctor not found");

        return performedProcedureRepository.findByDoctorDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<PerformedProcedures> getProceduresForPatient(Long patientId) {

        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Patient ID must be valid");

        if (!patientRepository.existsById(patientId))
            throw new RuntimeException("Patient not found");

        return performedProcedureRepository.findByPatientPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordProcedures> getProceduresForMedicalRecord(Long medicalRecordId) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Medical record ID must be valid");

        if (!medicalRecordRepository.existsById(medicalRecordId))
            throw new RuntimeException("Medical record not found");

        return medicalRecordProcedureRepository.findByMedicalRecordRecordId(medicalRecordId);
    }

    @Transactional(readOnly = true)
    public List<SimpleProcedureDTO> getAllProcedures() {
        return procedureRepository.findAll().stream()
                .map(proc -> new SimpleProcedureDTO(
                        proc.getProcedureId(),
                        proc.getProcedureType(),
                        proc.getDescription(),
                        proc.getCost()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PerformedProcedures> getPerformedProcedureById(Long id) {

        if (id == null || id <= 0)
            throw new IllegalArgumentException("ID must be valid");

        return performedProcedureRepository.findById(id);
    }

    // ================= STORE PROCEDURE RESULT (UC017 Enhanced) =================
    @Transactional
    public ProcedureResults storeProcedureResult(Long medicalRecordId,
                                                 Long procedureId,
                                                 String resultDescription,
                                                 LocalDate resultDate) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Medical record ID must be valid");

        if (procedureId == null || procedureId <= 0)
            throw new IllegalArgumentException("Procedure ID must be valid");

        if (resultDate == null)
            throw new IllegalArgumentException("Result date is required");

        MedicalRecord record = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new RuntimeException("Procedure not found"));

        ProcedureResults result = new ProcedureResults();
        result.setProcedure(procedure);
        result.setResultDescription(resultDescription);
        result.setResultDate(resultDate);

        ProcedureResults savedResult = procedureResultRepository.save(result);

        // Link to medical record
        MedicalRecordProcedureResults link = new MedicalRecordProcedureResults(record, savedResult);
        medicalRecordProcedureResultRepository.save(link);

        logger.info("Stored procedure result {} for medical record {}", savedResult.getResultId(), medicalRecordId);
        return savedResult;
    }

    @Transactional(readOnly = true)
    public List<ProcedureResults> getProcedureResultsForMedicalRecord(Long medicalRecordId) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Medical record ID must be valid");

        if (!medicalRecordRepository.existsById(medicalRecordId))
            throw new RuntimeException("Medical record not found");

        return procedureResultRepository.findByMedicalRecordId(medicalRecordId);
    }
}