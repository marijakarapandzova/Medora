package medora.controller;

import medora.dto.PatientDTO;
import medora.dto.CreatePatientRequest;
import medora.models.domain.Patient;
import medora.service.PatientService;
import medora.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger logger = LoggerFactory.getLogger(PatientController.class);

    private final PatientService patientService;
    private final SecurityUtil securityUtil;

    public PatientController(PatientService patientService, SecurityUtil securityUtil) {
        this.patientService = patientService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody CreatePatientRequest request, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only ADMIN can create patients
            if (!role.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can create patients"));
            }
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "First name is required"));
            }
            if (request.getLastName() == null || request.getLastName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Last name is required"));
            }
            if (request.getEmbg() == null || request.getEmbg().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "EMBG is required"));
            }

            Patient patient = new Patient();
            patient.setFirstName(request.getFirstName());
            patient.setLastName(request.getLastName());
            patient.setEmailAddress(request.getEmailAddress());
            patient.setDateOfBirth(request.getDateOfBirth());
            patient.setBloodType(request.getBloodType());
            patient.setGender(request.getGender());
            patient.setPhoneNumber(request.getPhoneNumber());
            patient.setEmbg(request.getEmbg());

            Patient createdPatient = patientService.createPatient(patient);
            PatientDTO dto = convertToDTO(createdPatient);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error creating patient: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating patient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create patient: " + e.getMessage()));
        }
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<?> getPatientById(@PathVariable Long patientId) {
        try {
            logger.info("Fetching patient with ID: {}", patientId);
            return patientService.getPatientById(patientId)
                    .map(p -> ResponseEntity.ok(convertToDTO(p)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching patient: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching patient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch patient: " + e.getMessage()));
        }
    }

    @GetMapping("/embg/{embg}")
    public ResponseEntity<?> getPatientByEmbg(@PathVariable String embg) {
        try {
            logger.info("Fetching patient with EMBG: {}", embg);
            return patientService.getPatientByEmbg(embg)
                    .map(p -> ResponseEntity.ok(convertToDTO(p)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching patient: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching patient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch patient: " + e.getMessage()));
        }
    }

    @GetMapping("/email/{emailAddress}")
    public ResponseEntity<?> getPatientByEmail(@PathVariable String emailAddress) {
        try {
            logger.info("Fetching patient with email: {}", emailAddress);
            return patientService.getPatientByEmail(emailAddress)
                    .map(p -> ResponseEntity.ok(convertToDTO(p)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching patient: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching patient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch patient: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPatients() {
        try {
            logger.info("Fetching all patients");
            List<Patient> patients = patientService.getAllPatients();
            List<PatientDTO> patientDTOs = patients.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(patientDTOs);
        } catch (RuntimeException e) {
            logger.error("Error fetching patients: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching patients: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch patients: " + e.getMessage()));
        }
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<?> updatePatient(@PathVariable Long patientId,
                                           @RequestBody CreatePatientRequest request,
                                           HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only ADMIN can update patients
            if (!role.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can update patients"));
            }

            logger.info("Updating patient with ID: {}", patientId);

            Patient patientDetails = new Patient();
            patientDetails.setFirstName(request.getFirstName());
            patientDetails.setLastName(request.getLastName());
            patientDetails.setPhoneNumber(request.getPhoneNumber());
            patientDetails.setBloodType(request.getBloodType());

            Patient updatedPatient = patientService.updatePatient(patientId, patientDetails);
            PatientDTO dto = convertToDTO(updatedPatient);

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error updating patient: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating patient: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update patient: " + e.getMessage()));
        }
    }

    @PostMapping("/backfill/medical-records")
    public ResponseEntity<?> backfillMissingMedicalRecords() {
        try {
            logger.info("Starting backfill of missing medical records");
            int createdCount = patientService.createMissingMedicalRecords();
            return ResponseEntity.ok(Map.of("message", "Backfill complete", "recordsCreated", createdCount));
        } catch (Exception e) {
            logger.error("Error during backfill: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Backfill failed: " + e.getMessage()));
        }
    }

    private PatientDTO convertToDTO(Patient patient) {
        return new PatientDTO(
                patient.getPatientId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmailAddress(),
                patient.getDateOfBirth(),
                patient.getBloodType(),
                patient.getGender(),
                patient.getPhoneNumber(),
                patient.getEmbg()
        );
    }
}
