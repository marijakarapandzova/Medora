package medora.controller;

import medora.dto.CreatePrescriptionRequest;
import medora.dto.PrescriptionDTO;
import medora.models.domain.PrescriptionMedicalRecord;
import medora.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    public ResponseEntity<?> prescribeMedication(@RequestBody CreatePrescriptionRequest request) {
        try {
            if (request.getMedicalRecordId() == null || request.getMedicalRecordId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid medical record ID is required"));
            }
            if (request.getMedicationName() == null || request.getMedicationName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Medication name is required"));
            }
            if (request.getDosage() == null || request.getDosage().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Dosage is required"));
            }

            PrescriptionMedicalRecord prescription = prescriptionService.prescribeMedication(
                    request.getMedicalRecordId(),
                    request.getMedicationName(),
                    request.getDosage(),
                    request.getFrequency(),
                    request.getDuration(),
                    request.getNotes()
            );
            PrescriptionDTO dto = convertToDTO(prescription);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error creating prescription: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating prescription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create prescription: " + e.getMessage()));
        }
    }


    @GetMapping("/medical-record/{medicalRecordId}")
    public ResponseEntity<?> getPrescriptionsForMedicalRecord(@PathVariable Long medicalRecordId) {
        try {
            logger.info("Fetching prescriptions for medical record ID: {}", medicalRecordId);
            List<PrescriptionMedicalRecord> prescriptions = prescriptionService.getPrescriptionsForMedicalRecord(medicalRecordId);
            List<PrescriptionDTO> dtos = prescriptions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching prescriptions: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching prescriptions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch prescriptions: " + e.getMessage()));
        }
    }



    private PrescriptionDTO convertToDTO(PrescriptionMedicalRecord prescription) {
        Long prescriptionId = null;
        Long medicalRecordId = null;
        String medicationName = null;

        if (prescription.getPrescription() != null) {
            prescriptionId = prescription.getPrescription().getPrescriptionId();
            medicationName = prescription.getPrescription().getMedicationName();
        }
        if (prescription.getMedicalRecord() != null) {
            medicalRecordId = prescription.getMedicalRecord().getRecordId();
        }

        return new PrescriptionDTO(
                prescriptionId,
                medicalRecordId,
                medicationName,
                prescription.getDosage(),
                prescription.getFrequency(),
                prescription.getDuration(),
                prescription.getNotes()
        );
    }
}
