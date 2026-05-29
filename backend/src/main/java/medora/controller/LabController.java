package medora.controller;

import medora.dto.*;
import medora.models.domain.*;
import medora.service.LabService;
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
@RequestMapping("/api/lab-tests")
public class LabController {

    private static final Logger logger = LoggerFactory.getLogger(LabController.class);

    private final LabService labService;
    private final SecurityUtil securityUtil;

    public LabController(LabService labService, SecurityUtil securityUtil) {
        this.labService = labService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public ResponseEntity<?> createLabTest(@RequestBody CreateLabTestRequest request, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only ADMIN can create lab tests
            if (!role.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can create lab tests"));
            }

            if (request.getTestName() == null || request.getTestName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Test name is required"));
            }
            if (request.getCost() == null || request.getCost().signum() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid cost is required"));
            }

            LabTests labTest = labService.requestLabTest(
                    request.getTestName(),
                    request.getDescription(),
                    request.getCost()
            );
            LabTestDTO dto = convertToDTO(labTest);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error creating lab test: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating lab test: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create lab test: " + e.getMessage()));
        }
    }

    @GetMapping("/{testId}")
    public ResponseEntity<?> getLabTestById(@PathVariable Long testId) {
        try {
            logger.info("Fetching lab test with ID: {}", testId);
            return labService.getLabTestById(testId)
                    .map(t -> ResponseEntity.ok(convertToDTO(t)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching lab test: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching lab test: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch lab test: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLabTests(HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // BILLING_ADMIN cannot access lab tests
            if (role.equals("BILLING_ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You do not have permission to access lab tests"));
            }

            logger.info("Fetching all lab tests");
            List<LabTests> tests = labService.getAllLabTests();
            List<LabTestDTO> dtos = tests.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching lab tests: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching lab tests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch lab tests: " + e.getMessage()));
        }
    }

    @PutMapping("/{testId}")
    public ResponseEntity<?> updateLabTest(@PathVariable Long testId,
                                           @RequestBody CreateLabTestRequest request) {
        try {
            logger.info("Updating lab test with ID: {}", testId);
            LabTests updatedTest = labService.updateLabTest(
                    testId,
                    request.getTestName(),
                    request.getDescription(),
                    request.getCost()
            );
            LabTestDTO dto = convertToDTO(updatedTest);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error updating lab test: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating lab test: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update lab test: " + e.getMessage()));
        }
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestLabTest(@RequestBody RequestLabTestRequest request, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Patients cannot request lab tests
            if (role.equals("PATIENT")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Patients cannot request lab tests"));
            }

            logger.info("Requesting lab test {} for patient {}", request.getTestId(), request.getPatientId());

            PerformedLabTests performedTest = labService.requestLabTestForPatient(
                    request.getPatientId(),
                    request.getDoctorId(),
                    request.getTestId(),
                    request.getTestDate(),
                    request.getNotes()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertPerformedTestToDTO(performedTest));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error requesting lab test: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error requesting lab test: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error requesting lab test: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to request lab test: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/patient/{patientId}")
    public ResponseEntity<?> getLabTestRequestsForPatient(@PathVariable Long patientId) {
        try {
            logger.info("Fetching lab test requests for patient {}", patientId);
            List<PerformedLabTests> requests = labService.getLabTestRequestsForPatient(patientId);
            List<LabTestRequestDTO> dtos = requests.stream()
                    .map(this::convertPerformedTestToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching lab test requests: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching lab test requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch lab test requests: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/doctor/{doctorId}")
    public ResponseEntity<?> getLabTestRequestsByDoctor(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching lab test requests by doctor {}", doctorId);
            List<PerformedLabTests> requests = labService.getLabTestRequestsByDoctor(doctorId);
            List<LabTestRequestDTO> dtos = requests.stream()
                    .map(this::convertPerformedTestToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching doctor lab test requests: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching doctor lab test requests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch lab test requests: " + e.getMessage()));
        }
    }

    @PostMapping("/results")
    public ResponseEntity<?> submitLabResult(@RequestBody SubmitLabResultRequest request, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only LAB_TECHNICIAN can submit lab results (not DOCTOR)
            if (!role.equals("LAB_TECHNICIAN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only lab technicians can submit lab results"));
            }

            logger.info("Submitting lab result for medical record {}", request.getMedicalRecordId());

            MedicalRecordLabResults result = labService.storeLabResult(
                    request.getMedicalRecordId(),
                    request.getTestId(),
                    request.getResults(),
                    request.getResultDate()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertResultToDTO(result));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error submitting lab result: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error submitting lab result: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error submitting lab result: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to submit lab result: " + e.getMessage()));
        }
    }

    @GetMapping("/results/medical-record/{medicalRecordId}")
    public ResponseEntity<?> getLabResultsForMedicalRecord(@PathVariable Long medicalRecordId) {
        try {
            logger.info("Fetching lab results for medical record {}", medicalRecordId);
            List<MedicalRecordLabResults> results = labService.getLabResultsForMedicalRecord(medicalRecordId);
            List<LabResultDTO> dtos = results.stream()
                    .map(this::convertResultToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching lab results: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching lab results: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch lab results: " + e.getMessage()));
        }
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<?> getPendingLabTests(HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }


            if (!role.equals("LAB_TECHNICIAN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only lab technicians can view pending tests"));
            }

            logger.info("Fetching pending lab tests");
            List<PerformedLabTests> pendingTests = labService.getPendingLabTests();
            List<LabTestRequestDTO> dtos = pendingTests.stream()
                    .map(this::convertPerformedTestToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching pending lab tests: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching pending lab tests: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch pending lab tests: " + e.getMessage()));
        }
    }

    @GetMapping("/results")
    public ResponseEntity<?> getAllSubmittedLabResults(HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            if (!role.equals("LAB_TECHNICIAN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only lab technicians can view submitted results"));
            }

            logger.info("Fetching all submitted lab results");
            List<MedicalRecordLabResults> results = labService.getAllSubmittedLabResults();
            List<Map<String, Object>> dtos = results.stream()
                    .map(recordResult -> {
                        LabResults labResult = recordResult.getLabResult();
                        MedicalRecord medicalRecord = recordResult.getMedicalRecord();
                        String patientName = "N/A";
                        if (medicalRecord != null && medicalRecord.getPatient() != null) {
                            Patient patient = medicalRecord.getPatient();
                            patientName = patient.getFirstName() + " " + patient.getLastName();
                        }
                        Map<String, Object> resultMap = new java.util.HashMap<>();
                        resultMap.put("resultId", labResult.getResultId());
                        resultMap.put("testId", labResult.getLabTest().getTestId());
                        resultMap.put("testName", labResult.getLabTest().getTestName());
                        resultMap.put("patientName", patientName);
                        resultMap.put("doctorName", "");
                        resultMap.put("testDate", labResult.getResultDate());
                        resultMap.put("resultDate", labResult.getResultDate());
                        resultMap.put("createdDate", labResult.getResultDate());
                        resultMap.put("results", labResult.getResults());
                        resultMap.put("description", labResult.getLabTest().getDescription() != null ? labResult.getLabTest().getDescription() : "");
                        return resultMap;
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching submitted lab results: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching submitted lab results: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch submitted lab results: " + e.getMessage()));
        }
    }

    private LabTestDTO convertToDTO(LabTests labTest) {
        return new LabTestDTO(
                labTest.getTestId(),
                labTest.getTestName(),
                labTest.getDescription(),
                labTest.getCost()
        );
    }

    private LabTestRequestDTO convertPerformedTestToDTO(PerformedLabTests performedTest) {
        return new LabTestRequestDTO(
                performedTest.getLabTest().getTestId(),
                performedTest.getLabTest().getTestName(),
                performedTest.getPatient().getPatientId(),
                performedTest.getPatient().getFirstName() + " " + performedTest.getPatient().getLastName(),
                performedTest.getDoctor().getDoctorId(),
                performedTest.getDoctor().getFirstName() + " " + performedTest.getDoctor().getLastName(),
                performedTest.getCreatedAt() != null ? performedTest.getCreatedAt().toLocalDate() : performedTest.getTestDate(),
                performedTest.getTestDate(),
                performedTest.getNotes()
        );
    }

    private LabResultDTO convertResultToDTO(MedicalRecordLabResults recordLabResult) {
        LabResults labResult = recordLabResult.getLabResult();
        return new LabResultDTO(
                labResult.getResultId(),
                labResult.getLabTest().getTestId(),
                labResult.getLabTest().getTestName(),
                recordLabResult.getMedicalRecord().getRecordId(),
                labResult.getResults(),
                labResult.getResultDate()
        );
    }
}
