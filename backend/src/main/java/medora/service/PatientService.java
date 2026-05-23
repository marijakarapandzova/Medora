package medora.service;


import medora.models.domain.MedicalRecord;
import medora.models.domain.Patient;
import medora.repository.MedicalRecordRepository;
import medora.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * PatientService handles patient profile operations.
 * UC004 – View Patient Profile
 * UC026 – Search Medical Records (helper)
 */
@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public PatientService(PatientRepository patientRepository,
                         MedicalRecordRepository medicalRecordRepository) {
        this.patientRepository = patientRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /**
     * UC004 – View Patient Profile
     * Get patient by ID
     */
    @Transactional(readOnly = true)
    public Optional<Patient> getPatientById(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be valid");
        }
        logger.info("Fetching patient with ID: {}", patientId);
        return patientRepository.findById(patientId);
    }

    /**
     * UC004 – View Patient Profile
     * Get patient by EMBG (unique identifier)
     */
    @Transactional(readOnly = true)
    public Optional<Patient> getPatientByEmbg(String embg) {
        if (embg == null || embg.isBlank()) {
            throw new IllegalArgumentException("EMBG cannot be null or empty");
        }
        logger.info("Fetching patient with EMBG: {}", embg);
        return patientRepository.findByEmbg(embg);
    }

    /**
     * UC004 – View Patient Profile
     * Get patient by email
     */
    @Transactional(readOnly = true)
    public Optional<Patient> getPatientByEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        logger.info("Fetching patient with email: {}", emailAddress);
        return patientRepository.findByEmailAddress(emailAddress);
    }

    /**
     * Get all patients
     */
    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        logger.info("Fetching all patients");
        return patientRepository.findAll();
    }

    /**
     * Create a new patient and automatically create their medical record
     */
    @Transactional
    public Patient createPatient(Patient patient) {
        if (patient == null || patient.getEmbg() == null || patient.getEmbg().isBlank()) {
            throw new IllegalArgumentException("Patient EMBG is required");
        }
        if (patient.getFirstName() == null || patient.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Patient first name is required");
        }
        if (patient.getLastName() == null || patient.getLastName().isBlank()) {
            throw new IllegalArgumentException("Patient last name is required");
        }

        logger.info("Creating new patient with EMBG: {}", patient.getEmbg());
        Patient savedPatient = patientRepository.save(patient);

        // Automatically create a medical record for the patient
        try {
            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setPatient(savedPatient);
            medicalRecordRepository.save(medicalRecord);
            logger.info("Created medical record for patient ID: {}", savedPatient.getPatientId());
        } catch (Exception e) {
            logger.error("Failed to create medical record for patient: {}", e.getMessage());
        }

        return savedPatient;
    }

    /**
     * Update patient information
     */
    @Transactional
    public Patient updatePatient(Long patientId, Patient patientDetails) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be valid");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));

        if (patientDetails.getFirstName() != null) {
            patient.setFirstName(patientDetails.getFirstName());
        }
        if (patientDetails.getLastName() != null) {
            patient.setLastName(patientDetails.getLastName());
        }
        if (patientDetails.getPhoneNumber() != null) {
            patient.setPhoneNumber(patientDetails.getPhoneNumber());
        }
        if (patientDetails.getBloodType() != null) {
            patient.setBloodType(patientDetails.getBloodType());
        }

        logger.info("Updating patient with ID: {}", patientId);
        return patientRepository.save(patient);
    }

    /**
     * Create medical records for all patients that don't have one
     * This is a utility method for backfilling missing medical records
     */
    @Transactional
    public int createMissingMedicalRecords() {
        List<Patient> allPatients = patientRepository.findAll();
        int createdCount = 0;

        for (Patient patient : allPatients) {
            try {
                // Check if patient already has a medical record
                boolean hasRecord = patient.getMedicalRecords() != null && !patient.getMedicalRecords().isEmpty();
                if (!hasRecord) {
                    MedicalRecord medicalRecord = new MedicalRecord();
                    medicalRecord.setPatient(patient);
                    medicalRecordRepository.save(medicalRecord);
                    createdCount++;
                    logger.info("Created medical record for patient ID: {}", patient.getPatientId());
                }
            } catch (Exception e) {
                logger.warn("Failed to create medical record for patient ID {}: {}", patient.getPatientId(), e.getMessage());
            }
        }

        logger.info("Backfill complete: created {} medical records", createdCount);
        return createdCount;
    }
}
