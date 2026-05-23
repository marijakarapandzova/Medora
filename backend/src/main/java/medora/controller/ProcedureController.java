package medora.controller;

import medora.dto.ProcedureRequestDTO;
import medora.dto.RequestProcedureRequest;
import medora.dto.SubmitProcedureResultRequest;
import medora.models.domain.PerformedProcedures;
import medora.models.domain.ProcedureResults;
import medora.service.ProcedureService;
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
@RequestMapping("/api/performed-procedures")
public class ProcedureController {

    private static final Logger logger = LoggerFactory.getLogger(ProcedureController.class);

    private final ProcedureService procedureService;

    public ProcedureController(ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableProcedures() {
        try {
            logger.info("Fetching all available procedures");
            return ResponseEntity.ok(procedureService.getAllProcedures());
        } catch (RuntimeException e) {
            logger.error("Error fetching procedures: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching procedures: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch procedures: " + e.getMessage()));
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestProcedure(@RequestBody RequestProcedureRequest request) {
        try {
            logger.info("Requesting procedure {} for patient {}", request.getProcedureId(), request.getPatientId());

            PerformedProcedures performedProcedure = procedureService.requestProcedureForPatient(
                    request.getPatientId(),
                    request.getDoctorId(),
                    request.getProcedureId(),
                    request.getDiagnosisId(),
                    request.getProcedureDate(),
                    request.getNotes()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertPerformedProcedureToDTO(performedProcedure));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error requesting procedure: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error requesting procedure: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error requesting procedure: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to request procedure: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/patient/{patientId}")
    public ResponseEntity<?> getProcedureRequestsForPatient(@PathVariable Long patientId) {
        try {
            logger.info("Fetching procedure requests for patient {}", patientId);
            List<PerformedProcedures> requests = procedureService.getProcedureRequestsForPatient(patientId);
            List<ProcedureRequestDTO> dtos = requests.stream()
                    .map(this::convertPerformedProcedureToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching procedure requests: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching procedure requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch procedure requests: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/doctor/{doctorId}")
    public ResponseEntity<?> getProcedureRequestsByDoctor(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching procedure requests by doctor {}", doctorId);
            List<PerformedProcedures> requests = procedureService.getProcedureRequestsByDoctor(doctorId);
            List<ProcedureRequestDTO> dtos = requests.stream()
                    .map(this::convertPerformedProcedureToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching doctor procedure requests: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching doctor procedure requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch procedure requests: " + e.getMessage()));
        }
    }

    @PostMapping("/record")
    public ResponseEntity<?> recordProcedure(
            @RequestParam Long procedureId,
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam(required = false) Long diagnosisId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate procedureDate) {
        try {
            logger.info("Recording procedure - procedureId: {}, doctorId: {}, patientId: {}, procedureDate: {}",
                    procedureId, doctorId, patientId, procedureDate);

            PerformedProcedures procedure = procedureService.recordProcedure(
                    procedureId, doctorId, patientId, diagnosisId, procedureDate);

            Map<String, Object> response = Map.of(
                    "performedId", procedure.getPerformedId(),
                    "procedureId", procedure.getProcedure().getProcedureId(),
                    "procedureType", procedure.getProcedure().getProcedureType(),
                    "doctorId", procedure.getDoctor().getDoctorId(),
                    "patientId", procedure.getPatient().getPatientId(),
                    "procedureDate", procedure.getProcedureDate(),
                    "notes", procedure.getNotes() != null ? procedure.getNotes() : ""
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Error recording procedure: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error recording procedure: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to record procedure: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getProceduresForPatient(@PathVariable Long patientId) {
        try {
            logger.info("Fetching procedures for patient ID: {}", patientId);
            List<PerformedProcedures> procedures = procedureService.getProceduresForPatient(patientId);
            return ResponseEntity.ok(procedures);
        } catch (RuntimeException e) {
            logger.error("Error fetching procedures: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching procedures: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch procedures: " + e.getMessage()));
        }
    }

    @GetMapping("/medical-record/{medicalRecordId}")
    public ResponseEntity<?> getProceduresForMedicalRecord(@PathVariable Long medicalRecordId) {
        try {
            logger.info("Fetching procedures for medical record ID: {}", medicalRecordId);
            return ResponseEntity.ok(procedureService.getProceduresForMedicalRecord(medicalRecordId));
        } catch (RuntimeException e) {
            logger.error("Error fetching procedures: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching procedures: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch procedures: " + e.getMessage()));
        }
    }

    @GetMapping("/{procedureId}")
    public ResponseEntity<?> getPerformedProcedureById(@PathVariable Long procedureId) {
        try {
            logger.info("Fetching performed procedure with ID: {}", procedureId);
            return procedureService.getPerformedProcedureById(procedureId)
                    .map(p -> ResponseEntity.ok(p))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching procedure: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching procedure: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch procedure: " + e.getMessage()));
        }
    }

    @PatchMapping("/{procedureId}/outcome")
    public ResponseEntity<?> recordProcedureOutcome(
            @PathVariable Long procedureId,
            @RequestParam String notes) {
        try {
            logger.info("Recording procedure outcome for ID: {}", procedureId);
            PerformedProcedures procedure = procedureService.recordProcedureOutcome(procedureId, notes);

            Map<String, Object> response = Map.of(
                    "performedId", procedure.getPerformedId(),
                    "procedureId", procedure.getProcedure().getProcedureId(),
                    "procedureType", procedure.getProcedure().getProcedureType(),
                    "notes", procedure.getNotes() != null ? procedure.getNotes() : "",
                    "procedureDate", procedure.getProcedureDate()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error recording procedure outcome: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error recording procedure outcome: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to record procedure outcome: " + e.getMessage()));
        }
    }

    @PostMapping("/results")
    public ResponseEntity<?> submitProcedureResult(@RequestBody SubmitProcedureResultRequest request) {
        try {
            logger.info("Submitting procedure result for medical record {}", request.getMedicalRecordId());

            ProcedureResults result = procedureService.storeProcedureResult(
                    request.getMedicalRecordId(),
                    request.getProcedureId(),
                    request.getResultDescription(),
                    request.getResultDate()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertResultToDTO(result));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error submitting procedure result: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error submitting procedure result: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error submitting procedure result: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit procedure result: " + e.getMessage()));
        }
    }

    @GetMapping("/results/medical-record/{medicalRecordId}")
    public ResponseEntity<?> getProcedureResultsForMedicalRecord(@PathVariable Long medicalRecordId) {
        try {
            logger.info("Fetching procedure results for medical record {}", medicalRecordId);
            List<ProcedureResults> results = procedureService.getProcedureResultsForMedicalRecord(medicalRecordId);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            logger.error("Error fetching procedure results: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching procedure results: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch procedure results: " + e.getMessage()));
        }
    }

    private ProcedureRequestDTO convertPerformedProcedureToDTO(PerformedProcedures performedProcedure) {
        return new ProcedureRequestDTO(
                performedProcedure.getProcedure().getProcedureId(),
                performedProcedure.getProcedure().getProcedureType(),
                performedProcedure.getPatient().getPatientId(),
                performedProcedure.getPatient().getFirstName() + " " + performedProcedure.getPatient().getLastName(),
                performedProcedure.getDoctor().getDoctorId(),
                performedProcedure.getDoctor().getFirstName() + " " + performedProcedure.getDoctor().getLastName(),
                performedProcedure.getProcedureDate(),
                performedProcedure.getNotes()
        );
    }

    private Map<String, Object> convertResultToDTO(ProcedureResults result) {
        return Map.of(
            "resultId", result.getResultId(),
            "procedureId", result.getProcedure().getProcedureId(),
            "procedureType", result.getProcedure().getProcedureType(),
            "resultDescription", result.getResultDescription() != null ? result.getResultDescription() : "",
            "resultDate", result.getResultDate()
        );
    }
}
