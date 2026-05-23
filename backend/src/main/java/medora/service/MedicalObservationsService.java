package medora.service;


import medora.models.domain.*;
import medora.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MedicalObservationService handles medical observations (symptoms and allergies).
 * UC010 – Record Symptoms
 * UC011 – Record Allergies
 * UC015 – Link Medical Data to Medical Record
 */
@Service
public class MedicalObservationsService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalObservationsService.class);

    private final SymptomRepository symptomRepository;
    private final AllergyRepository allergyRepository;
    private final MedicalRecordSymptomRepository medicalRecordSymptomRepository;
    private final MedicalRecordAllergyRepository medicalRecordAllergyRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalObservationsService(SymptomRepository symptomRepository,
                                     AllergyRepository allergyRepository,
                                     MedicalRecordSymptomRepository medicalRecordSymptomRepository,
                                     MedicalRecordAllergyRepository medicalRecordAllergyRepository,
                                     MedicalRecordRepository medicalRecordRepository) {
        this.symptomRepository = symptomRepository;
        this.allergyRepository = allergyRepository;
        this.medicalRecordSymptomRepository = medicalRecordSymptomRepository;
        this.medicalRecordAllergyRepository = medicalRecordAllergyRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    // ================= SYMPTOMS OPERATIONS =================

    /**
     * UC010 – Record Symptoms
     * Record a symptom for a patient's medical record
     */
    @Transactional
    public MedicalRecordSymptoms recordSymptom(Long medicalRecordId, Long symptomId) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }
        if (symptomId == null || symptomId <= 0) {
            throw new IllegalArgumentException("Symptom ID must be valid");
        }

        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + medicalRecordId));

        Symptoms symptom = symptomRepository.findById(symptomId)
                .orElseThrow(() -> new RuntimeException("Symptom not found with ID: " + symptomId));


        if (medicalRecordSymptomRepository.existsByMedicalRecordRecordIdAndSymptomSymptomId(
                medicalRecordId, symptomId)) {
            throw new RuntimeException("Symptom already recorded for this medical record");
        }

        MedicalRecordSymptoms recordSymptom = new MedicalRecordSymptoms();
        recordSymptom.setMedicalRecord(medicalRecord);
        recordSymptom.setSymptom(symptom);

        logger.info("Recording symptom ID: {} for medical record ID: {}", symptomId, medicalRecordId);
        return medicalRecordSymptomRepository.save(recordSymptom);
    }

    /**
     * Get all symptoms for a patient's medical record
     */
    @Transactional(readOnly = true)
    public List<MedicalRecordSymptoms> getSymptomsForMedicalRecord(Long medicalRecordId) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }

        if (!medicalRecordRepository.existsById(medicalRecordId)) {
            throw new RuntimeException("Medical record not found with ID: " + medicalRecordId);
        }

        logger.info("Fetching symptoms for medical record ID: {}", medicalRecordId);
        return medicalRecordSymptomRepository.findByMedicalRecordRecordId(medicalRecordId);
    }

   
    // For ALLERGIES

    /**
     * UC011 – Record Allergies
     * Record an allergy for a patient's medical record
     */
    @Transactional
    public MedicalRecordAllergies recordAllergy(Long medicalRecordId, Long allergyId) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }
        if (allergyId == null || allergyId <= 0) {
            throw new IllegalArgumentException("Allergy ID must be valid");
        }

        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + medicalRecordId));

        Allergies allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new RuntimeException("Allergy not found with ID: " + allergyId));

        // Check if allergy is already recorded
        if (medicalRecordAllergyRepository.existsByMedicalRecordRecordIdAndAllergyAllergyId(
                medicalRecordId, allergyId)) {
            throw new RuntimeException("Allergy already recorded for this medical record");
        }

        MedicalRecordAllergies recordAllergy = new MedicalRecordAllergies();
        recordAllergy.setMedicalRecord(medicalRecord);
        recordAllergy.setAllergy(allergy);

        logger.info("Recording allergy ID: {} for medical record ID: {}", allergyId, medicalRecordId);
        return medicalRecordAllergyRepository.save(recordAllergy);
    }


    @Transactional(readOnly = true)
    public List<MedicalRecordAllergies> getAllergiesForMedicalRecord(Long medicalRecordId) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }

        if (!medicalRecordRepository.existsById(medicalRecordId)) {
            throw new RuntimeException("Medical record not found with ID: " + medicalRecordId);
        }

        logger.info("Fetching allergies for medical record ID: {}", medicalRecordId);
        return medicalRecordAllergyRepository.findByMedicalRecordRecordId(medicalRecordId);
    }


    @Transactional(readOnly = true)
    public List<Symptoms> getAllSymptoms() {
        logger.info("Fetching all symptoms");
        return symptomRepository.findAll();
    }


    @Transactional(readOnly = true)
    public List<Allergies> getAllAllergies() {
        logger.info("Fetching all allergies");
        return allergyRepository.findAll();
    }
}
