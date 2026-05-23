package medora.controller;

import medora.dto.AllergyDTO;
import medora.dto.ComprehensiveMedicalRecordDTO;
import medora.dto.MedicalRecordDTO;
import medora.dto.SymptomDTO;
import medora.models.domain.*;
import medora.repository.*;
import medora.service.MedicalRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalRecordController.class);

    private final MedicalRecordService medicalRecordService;
    private final DiagnosisRepository diagnosisRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final MedicalRecordAllergyRepository allergyRepository;
    private final MedicalRecordSymptomRepository symptomRepository;
    private final SymptomRepository allSymptomRepository;
    private final AllergyRepository allAllergyRepository;
    private final PrescriptionRepository allPrescriptionRepository;
    private final PrescriptionMedicalRecordRepository prescriptionMedicalRecordRepository;

    public MedicalRecordController(MedicalRecordService medicalRecordService,
                                  DiagnosisRepository diagnosisRepository,
                                  MedicalReportRepository medicalReportRepository,
                                  MedicalRecordAllergyRepository allergyRepository,
                                  MedicalRecordSymptomRepository symptomRepository,
                                  SymptomRepository allSymptomRepository,
                                  AllergyRepository allAllergyRepository,
                                  PrescriptionRepository allPrescriptionRepository,
                                  PrescriptionMedicalRecordRepository prescriptionMedicalRecordRepository) {
        this.medicalRecordService = medicalRecordService;
        this.diagnosisRepository = diagnosisRepository;
        this.medicalReportRepository = medicalReportRepository;
        this.allergyRepository = allergyRepository;
        this.symptomRepository = symptomRepository;
        this.allSymptomRepository = allSymptomRepository;
        this.allAllergyRepository = allAllergyRepository;
        this.allPrescriptionRepository = allPrescriptionRepository;
        this.prescriptionMedicalRecordRepository = prescriptionMedicalRecordRepository;
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable Long recordId) {
        try {
            logger.info("Fetching medical record with ID: {}", recordId);
            return medicalRecordService.getMedicalRecordById(recordId)
                    .map(r -> ResponseEntity.ok(convertToDTO(r)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching medical record: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching medical record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch medical record: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getMedicalRecordByPatientId(@PathVariable Long patientId) {
        try {
            logger.info("Fetching comprehensive medical record for patient ID: {}", patientId);
            return medicalRecordService.getMedicalRecordByPatientId(patientId)
                    .map(record -> {
                        List<Diagnosis> diagnoses = diagnosisRepository.findByPatientPatientId(patientId);
                        List<MedicalReport> reports = medicalReportRepository.findByMedicalRecordRecordId(record.getRecordId());
                        return ResponseEntity.ok(convertToComprehensiveDTO(record, diagnoses, reports));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching medical record: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching medical record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch medical record: " + e.getMessage()));
        }
    }

    @GetMapping("/{medicalRecordId}/allergies")
    public ResponseEntity<?> getAllergiesForMedicalRecord(@PathVariable Long medicalRecordId) {
        try {
            logger.info("Fetching allergies for medical record ID: {}", medicalRecordId);
            List<MedicalRecordAllergies> allergies = allergyRepository.findByMedicalRecordRecordId(medicalRecordId);
            List<AllergyDTO> dtos = allergies.stream()
                    .map(a -> new AllergyDTO(
                            a.getAllergy().getAllergyId(),
                            a.getAllergy().getName(),
                            a.getReaction(),
                            a.getSeverity()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching allergies: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching allergies: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch allergies: " + e.getMessage()));
        }
    }

    @GetMapping("/{medicalRecordId}/symptoms")
    public ResponseEntity<?> getSymptomsForMedicalRecord(@PathVariable Long medicalRecordId) {
        try {
            logger.info("Fetching symptoms for medical record ID: {}", medicalRecordId);
            List<MedicalRecordSymptoms> symptoms = symptomRepository.findByMedicalRecordRecordId(medicalRecordId);
            List<SymptomDTO> dtos = symptoms.stream()
                    .map(s -> new SymptomDTO(
                            s.getSymptom().getSymptomId(),
                            s.getSymptom().getName(),
                            s.getSymptom().getDescription()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching symptoms: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching symptoms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch symptoms: " + e.getMessage()));
        }
    }

    @GetMapping("/dropdown/diagnoses")
    public ResponseEntity<?> getAllDiagnoses() {
        try {
            logger.info("Fetching all available diagnoses");
            List<Diagnosis> diagnoses = diagnosisRepository.findAll();
            return ResponseEntity.ok(diagnoses.stream()
                    .map(d -> new java.util.LinkedHashMap<String, Object>() {{
                        put("id", d.getDiagnosisId());
                        put("name", d.getName());
                    }})
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error fetching diagnoses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch diagnoses: " + e.getMessage()));
        }
    }

    @GetMapping("/dropdown/symptoms")
    public ResponseEntity<?> getAllSymptoms() {
        try {
            logger.info("Fetching all available symptoms");
            List<Symptoms> symptoms = allSymptomRepository.findAll();
            return ResponseEntity.ok(symptoms.stream()
                    .map(s -> new java.util.LinkedHashMap<String, Object>() {{
                        put("id", s.getSymptomId());
                        put("name", s.getName());
                    }})
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error fetching symptoms: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch symptoms: " + e.getMessage()));
        }
    }

    @GetMapping("/dropdown/allergies")
    public ResponseEntity<?> getAllAllergies() {
        try {
            logger.info("Fetching all available allergies");
            List<Allergies> allergies = allAllergyRepository.findAll();
            return ResponseEntity.ok(allergies.stream()
                    .map(a -> new java.util.LinkedHashMap<String, Object>() {{
                        put("id", a.getAllergyId());
                        put("name", a.getName());
                    }})
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error fetching allergies: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch allergies: " + e.getMessage()));
        }
    }

    @GetMapping("/dropdown/prescriptions")
    public ResponseEntity<?> getAllPrescriptions() {
        try {
            logger.info("Fetching all available prescriptions");
            List<Prescriptions> prescriptions = allPrescriptionRepository.findAll();
            return ResponseEntity.ok(prescriptions.stream()
                    .map(p -> new java.util.LinkedHashMap<String, Object>() {{
                        put("id", p.getPrescriptionId());
                        put("name", p.getMedicationName());
                    }})
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.error("Error fetching prescriptions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch prescriptions: " + e.getMessage()));
        }
    }

    @PostMapping("/{medicalRecordId}/symptoms")
    public ResponseEntity<?> recordSymptom(@PathVariable Long medicalRecordId, @RequestBody Map<String, Object> request) {
        try {
            if (medicalRecordId == null || medicalRecordId <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid medical record ID is required"));
            }
            Object symptomIdObj = request.get("symptomId");
            if (symptomIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Symptom ID is required"));
            }
            Long symptomId = symptomIdObj instanceof Number ? ((Number) symptomIdObj).longValue() : Long.parseLong(symptomIdObj.toString());
            String severity = (String) request.getOrDefault("severity", "MEDIUM");

            logger.info("Recording symptom {} for medical record {}", symptomId, medicalRecordId);
            medicalRecordService.recordSymptom(medicalRecordId, symptomId, severity);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Symptom recorded successfully"));
        } catch (RuntimeException e) {
            logger.error("Error recording symptom: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error recording symptom: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to record symptom: " + e.getMessage()));
        }
    }

    @PostMapping("/{medicalRecordId}/allergies")
    public ResponseEntity<?> recordAllergy(@PathVariable Long medicalRecordId, @RequestBody Map<String, Object> request) {
        try {
            if (medicalRecordId == null || medicalRecordId <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid medical record ID is required"));
            }
            Object allergyIdObj = request.get("allergyId");
            if (allergyIdObj == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Allergy ID is required"));
            }
            Long allergyId = allergyIdObj instanceof Number ? ((Number) allergyIdObj).longValue() : Long.parseLong(allergyIdObj.toString());
            String severity = (String) request.getOrDefault("severity", "MEDIUM");
            String reaction = (String) request.getOrDefault("reaction", "");

            logger.info("Recording allergy {} for medical record {}", allergyId, medicalRecordId);
            medicalRecordService.recordAllergy(medicalRecordId, allergyId, reaction, severity);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Allergy recorded successfully"));
        } catch (RuntimeException e) {
            logger.error("Error recording allergy: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error recording allergy: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to record allergy: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMedicalRecords(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) String embg,
            @RequestParam(required = false) String diagnosisName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            logger.info("Searching medical records with filters: patientName={}, embg={}, diagnosisName={}, startDate={}, endDate={}",
                    patientName, embg, diagnosisName, startDate, endDate);
            List<MedicalRecord> records = medicalRecordService.searchMedicalRecords(
                    patientName, embg, diagnosisName, startDate, endDate);
            List<MedicalRecordDTO> dtos = records.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error searching medical records: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error searching medical records: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search medical records: " + e.getMessage()));
        }
    }

    private MedicalRecordDTO convertToDTO(MedicalRecord record) {
        String patientName = "";
        String embg = "";
        if (record.getPatient() != null) {
            patientName = record.getPatient().getFirstName() + " " + record.getPatient().getLastName();
            embg = record.getPatient().getEmbg();
        }
        return new MedicalRecordDTO(
                record.getRecordId(),
                record.getPatient() != null ? record.getPatient().getPatientId() : null,
                patientName,
                embg
        );
    }

    private ComprehensiveMedicalRecordDTO convertToComprehensiveDTO(MedicalRecord record,
                                                                     List<Diagnosis> diagnoses,
                                                                     List<MedicalReport> reports) {
        String patientName = "";
        String embg = "";
        if (record.getPatient() != null) {
            patientName = record.getPatient().getFirstName() + " " + record.getPatient().getLastName();
            embg = record.getPatient().getEmbg();
        }

        List<MedicalRecordSymptoms> recordSymptoms = symptomRepository.findByMedicalRecordRecordId(record.getRecordId());
        List<ComprehensiveMedicalRecordDTO.SymptomDTO> symptoms = recordSymptoms.stream()
                .map(s -> new ComprehensiveMedicalRecordDTO.SymptomDTO(
                        s.getSymptom().getSymptomId(),
                        s.getSymptom().getName()
                ))
                .collect(Collectors.toList());

        List<MedicalRecordAllergies> recordAllergies = allergyRepository.findByMedicalRecordRecordId(record.getRecordId());
        List<ComprehensiveMedicalRecordDTO.AllergyDTO> allergies = recordAllergies.stream()
                .map(a -> new ComprehensiveMedicalRecordDTO.AllergyDTO(
                        a.getAllergy().getAllergyId(),
                        a.getAllergy().getName(),
                        a.getSeverity(),
                        a.getReaction()
                ))
                .collect(Collectors.toList());

        List<PrescriptionMedicalRecord> recordPrescriptions = prescriptionMedicalRecordRepository.findByMedicalRecordRecordId(record.getRecordId());
        List<ComprehensiveMedicalRecordDTO.PrescriptionDTO> prescriptions = recordPrescriptions.stream()
                .map(p -> new ComprehensiveMedicalRecordDTO.PrescriptionDTO(
                        p.getPrescription() != null ? p.getPrescription().getPrescriptionId() : null,
                        p.getPrescription() != null ? p.getPrescription().getMedicationName() : "",
                        p.getDosage(),
                        p.getFrequency(),
                        p.getDuration()
                ))
                .collect(Collectors.toList());

        List<ComprehensiveMedicalRecordDTO.MedicalReportDTO> reportDTOs = reports.stream()
                .map(r -> new ComprehensiveMedicalRecordDTO.MedicalReportDTO(
                        r.getReportId(),
                        r.getDoctor() != null ? r.getDoctor().getFirstName() + " " + r.getDoctor().getLastName() : "",
                        r.getDescription(),
                        r.getReportDate().toString()
                ))
                .collect(Collectors.toList());

        List<ComprehensiveMedicalRecordDTO.DiagnosisDTO> diagnosisDTOs = diagnoses.stream()
                .map(d -> new ComprehensiveMedicalRecordDTO.DiagnosisDTO(
                        d.getDiagnosisId(),
                        d.getName(),
                        d.getDescription(),
                        d.getDoctor() != null ? d.getDoctor().getFirstName() + " " + d.getDoctor().getLastName() : ""
                ))
                .collect(Collectors.toList());

        return new ComprehensiveMedicalRecordDTO(
                record.getRecordId(),
                record.getPatient() != null ? record.getPatient().getPatientId() : null,
                patientName,
                embg,
                diagnosisDTOs,
                symptoms,
                allergies,
                prescriptions,
                reportDTOs
        );
    }
}
