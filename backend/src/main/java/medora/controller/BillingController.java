package medora.controller;

import medora.dto.BillingDTO;
import medora.dto.BillingDetailDTO;
import medora.dto.CreateBillingRequest;
import medora.dto.UpdateBillingRequest;
import medora.models.domain.Billing;
import medora.models.enums.PaymentStatus;
import medora.service.BillingService;
import medora.util.BillingPDFGenerator;
import medora.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);

    private final BillingService billingService;
    private final SecurityUtil securityUtil;

    public BillingController(BillingService billingService, SecurityUtil securityUtil) {
        this.billingService = billingService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public ResponseEntity<?> generateBillingRecord(@RequestBody CreateBillingRequest request, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only ADMIN can generate billing records (doctors cannot access billing)
            if (!role.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can generate billing records"));
            }

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
    public ResponseEntity<?> getBillingById(@PathVariable Long billId, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Doctors cannot access billing
            if (role.equals("DOCTOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Doctors cannot access billing records"));
            }

            logger.info("Fetching billing record with ID: {}", billId);
            var billing = billingService.getBillingById(billId);
            if (billing.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Patients can only view their own billing records
            if (role.equals("PATIENT")) {
                Long patientIdFromToken = securityUtil.getPatientIdFromRequest(httpRequest);
                Long billPatientId = billing.get().getMedicalRecord() != null && billing.get().getMedicalRecord().getPatient() != null
                        ? billing.get().getMedicalRecord().getPatient().getPatientId()
                        : null;
                if (patientIdFromToken == null || !patientIdFromToken.equals(billPatientId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only view your own billing records"));
                }
            }

            return ResponseEntity.ok(convertToDTO(billing.get()));
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

    @GetMapping("/{billId}/detail")
    public ResponseEntity<?> getBillingDetail(@PathVariable Long billId, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Doctors cannot access billing
            if (role.equals("DOCTOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Doctors cannot access billing records"));
            }

            logger.info("Fetching detailed billing information for bill ID: {}", billId);
            BillingDetailDTO detail = billingService.getBillingDetail(billId);

            // Patients can only view their own billing details
            if (role.equals("PATIENT")) {
                Long patientIdFromToken = securityUtil.getPatientIdFromRequest(httpRequest);
                if (patientIdFromToken == null || detail == null || !patientIdFromToken.equals(detail.getPatientId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only view your own billing records"));
                }
            }

            return ResponseEntity.ok(detail);
        } catch (RuntimeException e) {
            logger.error("Error fetching billing detail: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching billing detail: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch billing detail: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllBillings(HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Doctors and Patients cannot view all billing records
            if (role.equals("PATIENT") || role.equals("DOCTOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You cannot view all billing records"));
            }

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
    public ResponseEntity<?> getBillingHistoryForPatient(@PathVariable Long patientId, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Doctors cannot access billing
            if (role.equals("DOCTOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Doctors cannot access billing records"));
            }

            // Patients can only view their own billing history
            if (role.equals("PATIENT")) {
                Long patientIdFromToken = securityUtil.getPatientIdFromRequest(httpRequest);
                if (patientIdFromToken == null || !patientIdFromToken.equals(patientId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only view your own billing records"));
                }
            }

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
                                                 @RequestBody UpdateBillingRequest request,
                                                 HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only ADMIN can update payment status
            if (!role.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can update payment status"));
            }

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

    @GetMapping("/{billId}/invoice-pdf")
    public ResponseEntity<?> downloadInvoicePDF(@PathVariable Long billId, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Doctors cannot access billing
            if (role.equals("DOCTOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Doctors cannot access billing records"));
            }

            logger.info("Generating PDF invoice for bill ID: {}", billId);
            BillingDetailDTO billingDetail = billingService.getBillingDetail(billId);

            // Patients can only download their own invoices
            if (role.equals("PATIENT")) {
                Long patientIdFromToken = securityUtil.getPatientIdFromRequest(httpRequest);
                if (patientIdFromToken == null || !patientIdFromToken.equals(billingDetail.getPatientId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You can only download your own invoices"));
                }
            }

            byte[] pdfContent = BillingPDFGenerator.generateInvoicePDF(billingDetail);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice-" + billId + ".pdf");
            headers.setContentLength(pdfContent.length);

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error generating PDF invoice: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error generating PDF invoice: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate invoice: " + e.getMessage()));
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
