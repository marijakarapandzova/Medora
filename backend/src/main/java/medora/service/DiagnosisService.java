package medora.service;

import medora.models.domain.Diagnosis;
import medora.models.domain.Patient;
import medora.models.domain.Doctors;
import medora.repository.DiagnosisRepository;
import medora.repository.PatientRepository;
import medora.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * DiagnosisService handles diagnosis operations.
 * UC009 – Record Diagnosis
 * UC015 – Link Medical Data to Medical Record
 */
@Service
public class DiagnosisService {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosisService.class);

    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public DiagnosisService(DiagnosisRepository diagnosisRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository) {
        this.diagnosisRepository = diagnosisRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * UC009 – Record Diagnosis
     */
    @Transactional
    public Diagnosis recordDiagnosis(Long patientId, Long doctorId,
                                     String diagnosisName, String description) {

        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Patient ID must be valid");

        if (doctorId == null || doctorId <= 0)
            throw new IllegalArgumentException("Doctor ID must be valid");

        if (diagnosisName == null || diagnosisName.isBlank())
            throw new IllegalArgumentException("Diagnosis name is required");

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setName(diagnosisName);
        diagnosis.setDescription(description);
        diagnosis.setPatient(patient);
        diagnosis.setDoctor(doctor);

        logger.info("Recording diagnosis '{}' for patient {}", diagnosisName, patientId);
        return diagnosisRepository.save(diagnosis);
    }

    /**
     * Get diagnosis by ID
     */
    @Transactional(readOnly = true)
    public Optional<Diagnosis> getDiagnosisById(Long diagnosisId) {

        if (diagnosisId == null || diagnosisId <= 0)
            throw new IllegalArgumentException("Diagnosis ID must be valid");

        return diagnosisRepository.findById(diagnosisId);
    }

    /**
     * Get diagnoses for patient
     */
    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesForPatient(Long patientId) {

        if (patientId == null || patientId <= 0)
            throw new IllegalArgumentException("Patient ID must be valid");

        if (!patientRepository.existsById(patientId))
            throw new RuntimeException("Patient not found");

        return diagnosisRepository.findByPatientPatientId(patientId);
    }

    /**
     * Get diagnoses by doctor
     */
    @Transactional(readOnly = true)
    public List<Diagnosis> getDiagnosesByDoctor(Long doctorId) {

        if (doctorId == null || doctorId <= 0)
            throw new IllegalArgumentException("Doctor ID must be valid");

        if (!doctorRepository.existsById(doctorId))
            throw new RuntimeException("Doctor not found");

        return diagnosisRepository.findByDoctorDoctorId(doctorId);
    }


}