package medora.controller;

import medora.dto.CreateDiagnosisRequest;
import medora.dto.DiagnosisDTO;
import medora.models.domain.Diagnosis;
import medora.service.DiagnosisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diagnoses")
public class DiagnosisController {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosisController.class);

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping
    public ResponseEntity<?> recordDiagnosis(@RequestBody CreateDiagnosisRequest request) {
        try {
            if (request.getPatientId() == null || request.getPatientId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid patient ID is required"));
            }
            if (request.getDoctorId() == null || request.getDoctorId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid doctor ID is required"));
            }
            if (request.getName() == null || request.getName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Diagnosis name is required"));
            }

            Diagnosis diagnosis = diagnosisService.recordDiagnosis(
                    request.getPatientId(),
                    request.getDoctorId(),
                    request.getName(),
                    request.getDescription()
            );
            DiagnosisDTO dto = convertToDTO(diagnosis);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error recording diagnosis: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error recording diagnosis: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to record diagnosis: " + e.getMessage()));
        }
    }

    @GetMapping("/{diagnosisId}")
    public ResponseEntity<?> getDiagnosisById(@PathVariable Long diagnosisId) {
        try {
            logger.info("Fetching diagnosis with ID: {}", diagnosisId);
            return diagnosisService.getDiagnosisById(diagnosisId)
                    .map(d -> ResponseEntity.ok(convertToDTO(d)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching diagnosis: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching diagnosis: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch diagnosis: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getDiagnosesForPatient(@PathVariable Long patientId) {
        try {
            logger.info("Fetching diagnoses for patient ID: {}", patientId);
            List<Diagnosis> diagnoses = diagnosisService.getDiagnosesForPatient(patientId);
            List<DiagnosisDTO> dtos = diagnoses.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching diagnoses: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching diagnoses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch diagnoses: " + e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getDiagnosesForDoctor(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching diagnoses for doctor ID: {}", doctorId);
            List<Diagnosis> diagnoses = diagnosisService.getDiagnosesByDoctor(doctorId);
            List<DiagnosisDTO> dtos = diagnoses.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching diagnoses: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching diagnoses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch diagnoses: " + e.getMessage()));
        }
    }

    private DiagnosisDTO convertToDTO(Diagnosis diagnosis) {
        String patientName = "";
        Long patientId = null;
        if (diagnosis.getPatient() != null) {
            patientName = diagnosis.getPatient().getFirstName() + " " + diagnosis.getPatient().getLastName();
            patientId = diagnosis.getPatient().getPatientId();
        }

        String doctorName = "";
        Long doctorId = null;
        if (diagnosis.getDoctor() != null) {
            doctorName = diagnosis.getDoctor().getFirstName() + " " + diagnosis.getDoctor().getLastName();
            doctorId = diagnosis.getDoctor().getDoctorId();
        }

        return new DiagnosisDTO(
                diagnosis.getDiagnosisId(),
                patientId,
                patientName,
                doctorId,
                doctorName,
                diagnosis.getName(),
                diagnosis.getDescription()
        );
    }
}
