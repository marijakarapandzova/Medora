package medora.controller;

import medora.dto.BillingDTO;
import medora.dto.CreateBillingRequest;
import medora.dto.UpdateBillingRequest;
import medora.models.domain.Billing;
import medora.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    public ResponseEntity<?> generateBillingRecord(@RequestBody CreateBillingRequest request) {
        try {
            if (request.getMedicalRecordId() == null || request.getMedicalRecordId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid medical record ID is required"));
            }
            if (request.getAdminId() == null || request.getAdminId() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid admin ID is required"));
            }
            if (request.getTotalCost() == null || request.getTotalCost().signum() < 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Valid total cost is required"));
            }

            Billing billing = billingService.generateBillingRecord(
                    request.getMedicalRecordId(),
                    request.getAdminId(),
                    request.getTotalCost()
            );
            BillingDTO dto = convertToDTO(billing);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error creating billing record: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating billing record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create billing record: " + e.getMessage()));
        }
    }

    @GetMapping("/{billId}")
    public ResponseEntity<?> getBillingById(@PathVariable Long billId) {
        try {
            logger.info("Fetching billing record with ID: {}", billId);
            return billingService.getBillingById(billId)
                    .map(b -> ResponseEntity.ok(convertToDTO(b)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching billing record: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching billing record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch billing record: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllBillings() {
        try {
            logger.info("Fetching all billing records");
            List<Billing> billings = billingService.getAllBillingRecords();
            List<BillingDTO> dtos = billings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching billing records: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching billing records: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch billing records: " + e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getBillingHistoryForPatient(@PathVariable Long patientId) {
        try {
            logger.info("Fetching billing history for patient ID: {}", patientId);
            List<Billing> billings = billingService.getBillingHistoryForPatient(patientId);
            List<BillingDTO> dtos = billings.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching billing history: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching billing history: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch billing history: " + e.getMessage()));
        }
    }

    @PatchMapping("/{billId}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long billId,
                                                @RequestBody UpdateBillingRequest request) {
        try {
            if (request.getPaymentStatus() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Payment status is required"));
            }

            LocalDate paymentDate = request.getPaymentDate();
            Billing billing = billingService.updatePaymentStatus(billId, request.getPaymentStatus(), paymentDate);
            BillingDTO dto = convertToDTO(billing);

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error updating payment status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating payment status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update payment status: " + e.getMessage()));
        }
    }

    private BillingDTO convertToDTO(Billing billing) {
        String patientName = "";
        if (billing.getMedicalRecord() != null && billing.getMedicalRecord().getPatient() != null) {
            patientName = billing.getMedicalRecord().getPatient().getFirstName() + " " +
                         billing.getMedicalRecord().getPatient().getLastName();
        }
        return new BillingDTO(
                billing.getBillId(),
                billing.getMedicalRecord() != null ? billing.getMedicalRecord().getRecordId() : null,
                patientName,
                billing.getTotalCost(),
                billing.getPaymentStatus(),
                billing.getPaymentDate()
        );
    }
}
