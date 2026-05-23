package medora.service;

import medora.models.domain.*;
import medora.models.domain.id.MedicalRecordAllergyId;
import medora.models.domain.id.MedicalRecordLabResultId;
import medora.models.domain.id.MedicalRecordProcedureId;
import medora.models.domain.id.MedicalRecordSymptomId;
import medora.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalRecordService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordService.class);

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final MedicalRecordProcedureRepository medicalRecordProcedureRepository;
    private final MedicalRecordLabResultRepository medicalRecordLabResultRepository;
    private final MedicalRecordAllergyRepository medicalRecordAllergyRepository;
    private final MedicalRecordSymptomRepository medicalRecordSymptomRepository;
    private final SymptomRepository symptomRepository;
    private final AllergyRepository allergyRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PatientRepository patientRepository,
                                MedicalRecordProcedureRepository medicalRecordProcedureRepository,
                                MedicalRecordLabResultRepository medicalRecordLabResultRepository,
                                MedicalRecordAllergyRepository medicalRecordAllergyRepository,
                                MedicalRecordSymptomRepository medicalRecordSymptomRepository,
                                SymptomRepository symptomRepository,
                                AllergyRepository allergyRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.medicalRecordProcedureRepository = medicalRecordProcedureRepository;
        this.medicalRecordLabResultRepository = medicalRecordLabResultRepository;
        this.medicalRecordAllergyRepository = medicalRecordAllergyRepository;
        this.medicalRecordSymptomRepository = medicalRecordSymptomRepository;
        this.symptomRepository = symptomRepository;
        this.allergyRepository = allergyRepository;
    }

    // UC008 – Access Medical Record
    @Transactional(readOnly = true)
    public Optional<MedicalRecord> getMedicalRecordById(Long recordId) {
        if (recordId == null || recordId <= 0) {
            throw new IllegalArgumentException("Record ID must be valid");
        }

        logger.info("Fetching medical record ID: {}", recordId);
        return medicalRecordRepository.findById(recordId);
    }

    // UC005 – View Medical Record
    @Transactional(readOnly = true)
    public Optional<MedicalRecord> getMedicalRecordByPatientId(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be valid");
        }

        if (!patientRepository.existsById(patientId)) {
            throw new RuntimeException("Patient not found");
        }

        logger.info("Fetching medical record for patient ID: {}", patientId);
        return medicalRecordRepository.findByPatientPatientId(patientId);
    }

    // UC026 – Search Medical Records
    @Transactional(readOnly = true)
    public List<MedicalRecord> searchMedicalRecords(
            String patientName,
            String embg,
            String diagnosisName,
            LocalDate startDate,
            LocalDate endDate
    ) {
        logger.info("Searching medical records with filters");

        return medicalRecordRepository.searchMedicalRecords(
                patientName,
                embg,
                diagnosisName,
                startDate,
                endDate
        );
    }

    // UC015 – Link Medical Data to Medical Record
    @Transactional
    public MedicalRecord linkMedicalData(Long recordId, MedicalRecord updatedData) {

        if (recordId == null || recordId <= 0) {
            throw new IllegalArgumentException("Record ID must be valid");
        }

        if (updatedData == null) {
            throw new IllegalArgumentException("Updated data cannot be null");
        }

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));


        try {
            // Procedures
            try {
                java.lang.reflect.Method getProcs = updatedData.getClass().getMethod("getProcedures");
                Object procsObj = getProcs.invoke(updatedData);
                if (procsObj instanceof Iterable<?> procs) {
                    for (Object p : procs) {
                        Long procId = extractId(p, "getProcedureId", "getId");
                        if (procId == null) continue;
                        MedicalRecordProcedureId mrpId = new MedicalRecordProcedureId(recordId, procId);
                        if (medicalRecordProcedureRepository.findById(mrpId).isEmpty()) {
                            medicalRecordProcedureRepository.linkProcedure(recordId, procId);
                        }
                    }
                }
            } catch (NoSuchMethodException ignored) {

            }

            // LabResults
            try {
                java.lang.reflect.Method getLabs = updatedData.getClass().getMethod("getLabResults");
                Object labsObj = getLabs.invoke(updatedData);
                if (labsObj instanceof Iterable<?> labs) {
                    for (Object lr : labs) {
                        Long lrId = extractId(lr, "getResultId", "getId");
                        if (lrId == null) continue;
                        MedicalRecordLabResultId mrlId = new MedicalRecordLabResultId(recordId, lrId);
                        if (medicalRecordLabResultRepository.findById(mrlId).isEmpty()) {
                            medicalRecordLabResultRepository.linkLabResult(recordId, lrId);
                        }
                    }
                }
            } catch (NoSuchMethodException ignored) {

            }

            // Allergies
            try {
                java.lang.reflect.Method getAllergies = updatedData.getClass().getMethod("getAllergies");
                Object allergiesObj = getAllergies.invoke(updatedData);
                if (allergiesObj instanceof Iterable<?> allergies) {
                    for (Object allergy : allergies) {
                        Long allergyId = extractId(allergy, "getAllergyId", "getId");
                        if (allergyId == null) continue;
                        MedicalRecordAllergyId mraId = new MedicalRecordAllergyId(recordId, allergyId);
                        if (medicalRecordAllergyRepository.findById(mraId).isEmpty()) {

                            MedicalRecordAllergies allergyJoin =
                                new MedicalRecordAllergies();
                            allergyJoin.setMedicalRecord(record);

                            logger.debug("Allergy {} linking deferred (missing allergyRepo)", allergyId);
                        }
                    }
                }
            } catch (NoSuchMethodException ignored) {

            }

            // Symptoms
            try {
                java.lang.reflect.Method getSymptoms = updatedData.getClass().getMethod("getSymptoms");
                Object symptomsObj = getSymptoms.invoke(updatedData);
                if (symptomsObj instanceof Iterable<?> symptoms) {
                    for (Object symptom : symptoms) {
                        Long symptomId = extractId(symptom, "getSymptomId", "getId");
                        if (symptomId == null) continue;
                        MedicalRecordSymptomId mrsId = new MedicalRecordSymptomId(recordId, symptomId);
                        if (medicalRecordSymptomRepository.findById(mrsId).isEmpty()) {
                            logger.debug("Symptom {} linking deferred (missing symptomRepo)", symptomId);
                        }
                    }
                }
            } catch (NoSuchMethodException ignored) {

            }
        } catch (ReflectiveOperationException e) {

            throw new RuntimeException("Failed to link medical data via reflection", e);
        }

        logger.info("Linked medical data to record ID: {}", recordId);
        return medicalRecordRepository.save(record);
    }

    @Transactional
    public MedicalRecord updateMedicalRecord(Long recordId, MedicalRecord recordDetails) {

        if (recordId == null || recordId <= 0) {
            throw new IllegalArgumentException("Record ID must be valid");
        }

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        if (recordDetails != null && recordDetails.getPatient() != null) {
            record.setPatient(recordDetails.getPatient());
        }

        logger.info("Updating medical record ID: {}", recordId);
        return medicalRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    @Transactional
    public MedicalRecord recordSymptom(Long medicalRecordId, Long symptomId, String severity) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }
        if (symptomId == null || symptomId <= 0) {
            throw new IllegalArgumentException("Symptom ID must be valid");
        }

        MedicalRecord record = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        Symptoms symptom = symptomRepository.findById(symptomId)
                .orElseThrow(() -> new RuntimeException("Symptom not found"));

        if (medicalRecordSymptomRepository.existsByMedicalRecordRecordIdAndSymptomSymptomId(medicalRecordId, symptomId)) {
            throw new RuntimeException("Symptom already recorded for this medical record");
        }

        logger.info("Recording symptom {} for medical record {}", symptomId, medicalRecordId);

        MedicalRecordSymptoms mrs = new MedicalRecordSymptoms(record, symptom, severity);
        medicalRecordSymptomRepository.save(mrs);

        return record;
    }

    @Transactional
    public MedicalRecord recordAllergy(Long medicalRecordId, Long allergyId, String reaction, String severity) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }
        if (allergyId == null || allergyId <= 0) {
            throw new IllegalArgumentException("Allergy ID must be valid");
        }

        MedicalRecord record = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found"));

        Allergies allergy = allergyRepository.findById(allergyId)
                .orElseThrow(() -> new RuntimeException("Allergy not found"));

        if (medicalRecordAllergyRepository.existsByMedicalRecordRecordIdAndAllergyAllergyId(medicalRecordId, allergyId)) {
            throw new RuntimeException("Allergy already recorded for this medical record");
        }

        logger.info("Recording allergy {} for medical record {}", allergyId, medicalRecordId);

        MedicalRecordAllergies mra = new MedicalRecordAllergies(record, allergy, reaction, severity);
        medicalRecordAllergyRepository.save(mra);

        return record;
    }


    private Long extractId(Object obj, String... candidateGetters) {
        if (obj == null) return null;
        for (String getter : candidateGetters) {
            try {
                java.lang.reflect.Method m = obj.getClass().getMethod(getter);
                Object val = m.invoke(obj);
                if (val instanceof Number) return ((Number) val).longValue();
            } catch (ReflectiveOperationException ignored) {

            }
        }
        return null;
    }
}