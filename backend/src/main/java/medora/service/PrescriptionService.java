package medora.service;

import medora.models.domain.MedicalRecord;
import medora.models.domain.PrescriptionMedicalRecord;
import medora.models.domain.Prescriptions;
import medora.repository.MedicalRecordRepository;
import medora.repository.PrescriptionMedicalRecordRepository;
import medora.repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * PrescriptionService handles prescription operations.
 * UC012 – Record Prescription
 * UC015 – Link Medical Data to Medical Record
 */
@Service
public class PrescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMedicalRecordRepository prescriptionMedicalRecordRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PrescriptionMedicalRecordRepository prescriptionMedicalRecordRepository,
                               MedicalRecordRepository medicalRecordRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionMedicalRecordRepository = prescriptionMedicalRecordRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /**
     * UC012 – Record Prescription
     */
    @Transactional
    public PrescriptionMedicalRecord prescribeMedication(Long medicalRecordId,
                                                         String medicationName,
                                                         String dosage,
                                                         String frequency,
                                                         String duration,
                                                         String notes) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Medical record ID must be valid");

        if (medicationName == null || medicationName.isBlank())
            throw new IllegalArgumentException("Medication name is required");

        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        Prescriptions prescription = new Prescriptions();
        prescription.setMedicationName(medicationName);

        Prescriptions savedPrescription = prescriptionRepository.save(prescription);

        PrescriptionMedicalRecord record = new PrescriptionMedicalRecord();
        record.setPrescription(savedPrescription);
        record.setMedicalRecord(medicalRecord);
        record.setDosage(dosage);
        record.setFrequency(frequency);
        record.setDuration(duration);
        record.setNotes(notes);

        logger.info("Prescription '{}' added to medical record {}", medicationName, medicalRecordId);
        return prescriptionMedicalRecordRepository.save(record);
    }

    /**
     * Get prescriptions for medical record
     */
    @Transactional(readOnly = true)
    public List<PrescriptionMedicalRecord> getPrescriptionsForMedicalRecord(Long medicalRecordId) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Medical record ID must be valid");

        if (!medicalRecordRepository.existsById(medicalRecordId))
            throw new RuntimeException("Medical record not found");

        return prescriptionMedicalRecordRepository.findByMedicalRecordRecordId(medicalRecordId);
    }

    /**
     * Get prescription by medical record and prescription IDs
     */
    @Transactional(readOnly = true)
    public Optional<PrescriptionMedicalRecord> getPrescriptionByRecordAndId(Long medicalRecordId, Long prescriptionId) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Medical record ID must be valid");

        if (prescriptionId == null || prescriptionId <= 0)
            throw new IllegalArgumentException("Prescription ID must be valid");

        return Optional.ofNullable(
                prescriptionMedicalRecordRepository.findByMedicalRecordAndPrescription(medicalRecordId, prescriptionId)
        );
    }

    /**
     * Update prescription details
     */
    @Transactional
    public PrescriptionMedicalRecord updatePrescription(Long medicalRecordId,
                                                        Long prescriptionId,
                                                        String dosage,
                                                        String frequency,
                                                        String duration,
                                                        String notes) {

        if (medicalRecordId == null || medicalRecordId <= 0)
            throw new IllegalArgumentException("Medical record ID must be valid");

        if (prescriptionId == null || prescriptionId <= 0)
            throw new IllegalArgumentException("Prescription ID must be valid");

        PrescriptionMedicalRecord record = prescriptionMedicalRecordRepository
                .findByMedicalRecordAndPrescription(medicalRecordId, prescriptionId);

        if (record == null)
            throw new RuntimeException("Prescription not found for the given medical record");

        if (dosage != null && !dosage.isBlank())
            record.setDosage(dosage);

        if (frequency != null && !frequency.isBlank())
            record.setFrequency(frequency);

        if (duration != null && !duration.isBlank())
            record.setDuration(duration);

        if (notes != null && !notes.isBlank())
            record.setNotes(notes);

        logger.info("Updated prescription {} for medical record {}", prescriptionId, medicalRecordId);
        return prescriptionMedicalRecordRepository.save(record);
    }
}