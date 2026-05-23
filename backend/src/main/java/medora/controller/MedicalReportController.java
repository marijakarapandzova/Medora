package medora.controller;

import medora.dto.ComprehensiveMedicalReportDTO;
import medora.dto.CreateMedicalReportRequest;
import medora.models.domain.MedicalReport;
import medora.service.MedicalReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medical-reports")
public class MedicalReportController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalReportController.class);

    private final MedicalReportService medicalReportService;

    public MedicalReportController(MedicalReportService medicalReportService) {
        this.medicalReportService = medicalReportService;
    }

    @PostMapping
    public ResponseEntity<?> createMedicalReport(@RequestBody CreateMedicalReportRequest request) {
        try {
            if (request.getDoctorId() == null || request.getDoctorId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid doctor ID is required"));
            }
            if (request.getMedicalRecordId() == null || request.getMedicalRecordId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid medical record ID is required"));
            }
            if (request.getDescription() == null || request.getDescription().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Report description is required"));
            }
            if (request.getReportDate() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Report date is required"));
            }

            MedicalReport report = medicalReportService.createMedicalReportWithSelectedItems(
                    request.getDoctorId(),
                    request.getMedicalRecordId(),
                    request.getDescription(),
                    request.getReportDate(),
                    request.getSelectedDiagnosisIds(),
                    request.getSelectedPrescriptionIds(),
                    request.getSelectedAllergyIds(),
                    request.getSelectedSymptomIds()
            );

            ComprehensiveMedicalReportDTO dto = medicalReportService.getComprehensiveReport(report.getReportId());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error creating medical report: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating medical report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create medical report: " + e.getMessage()));
        }
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<?> getMedicalReportById(@PathVariable Long reportId) {
        try {
            logger.info("Fetching medical report with ID: {}", reportId);
            ComprehensiveMedicalReportDTO report = medicalReportService.getComprehensiveReport(reportId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            logger.error("Error fetching medical report: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching medical report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch medical report: " + e.getMessage()));
        }
    }

    @GetMapping("/record/{medicalRecordId}")
    public ResponseEntity<?> getReportsForMedicalRecord(@PathVariable Long medicalRecordId) {
        try {
            logger.info("Fetching reports for medical record ID: {}", medicalRecordId);
            List<ComprehensiveMedicalReportDTO> reports = medicalReportService.getComprehensiveReportsForMedicalRecord(medicalRecordId);
            return ResponseEntity.ok(reports);
        } catch (RuntimeException e) {
            logger.error("Error fetching medical reports: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching medical reports: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch medical reports: " + e.getMessage()));
        }
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateMedicalReport(@PathVariable Long reportId,
                                                 @RequestBody Map<String, String> request) {
        try {
            String description = request.get("description");
            if (description == null || description.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Description is required"));
            }

            logger.info("Updating medical report with ID: {}", reportId);
            MedicalReport updated = medicalReportService.updateMedicalReport(reportId, description);
            ComprehensiveMedicalReportDTO dto = medicalReportService.getComprehensiveReport(updated.getReportId());
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error updating medical report: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating medical report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update medical report: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteMedicalReport(@PathVariable Long reportId) {
        try {
            logger.info("Deleting medical report with ID: {}", reportId);
            medicalReportService.deleteMedicalReport(reportId);
            return ResponseEntity.ok(Map.of("message", "Medical report deleted successfully"));
        } catch (RuntimeException e) {
            logger.error("Error deleting medical report: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error deleting medical report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete medical report: " + e.getMessage()));
        }
    }
}
