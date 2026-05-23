package medora.service;

import medora.models.domain.*;
import medora.models.enums.PaymentStatus;
import medora.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * BillingService handles billing operations.
 * UC020 – Generate Billing Record
 * UC021 – Record Payment Status
 * UC022 – View Billing History
 */
@Service
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private final BillingRepository billingRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AdminRepository adminRepository;
    private final BillingLabTestsRepository billingLabTestsRepository;
    private final BillingProceduresRepository billingProceduresRepository;

    public BillingService(BillingRepository billingRepository,
                          MedicalRecordRepository medicalRecordRepository,
                          AdminRepository adminRepository,
                          BillingLabTestsRepository billingLabTestsRepository,
                          BillingProceduresRepository billingProceduresRepository) {
        this.billingRepository = billingRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.adminRepository = adminRepository;
        this.billingLabTestsRepository = billingLabTestsRepository;
        this.billingProceduresRepository = billingProceduresRepository;
    }

    /**
     * UC020 – Generate Billing Record
     * Create a billing record based on procedures and lab tests
     */
    @Transactional
    public Billing generateBillingRecord(Long medicalRecordId, Long adminId, BigDecimal totalCost) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("Admin ID must be valid");
        }
        if (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total cost must be valid");
        }

        MedicalRecord medicalRecord = medicalRecordRepository.findById(medicalRecordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + medicalRecordId));

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + adminId));

        Billing billing = new Billing();
        billing.setMedicalRecord(medicalRecord);
        billing.setAdmin(admin);
        billing.setTotalCost(totalCost);
        billing.setPaymentStatus(PaymentStatus.PENDING);

        logger.info("Generating billing record for medical record ID: {} with total cost: {}",
                medicalRecordId, totalCost);
        return billingRepository.save(billing);
    }

    /**
     * UC021 – Record Payment Status
     * Update billing payment status
     */
    @Transactional
    public Billing updatePaymentStatus(Long billId, PaymentStatus paymentStatus, LocalDate paymentDate) {
        if (billId == null || billId <= 0) {
            throw new IllegalArgumentException("Bill ID must be valid");
        }
        if (paymentStatus == null) {
            throw new IllegalArgumentException("Payment status is required");
        }

        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Billing record not found with ID: " + billId));

        billing.setPaymentStatus(paymentStatus);
        if (paymentDate != null && paymentStatus == PaymentStatus.PAID) {
            billing.setPaymentDate(paymentDate);
        }

        logger.info("Updating payment status for bill ID: {} to {}", billId, paymentStatus);
        return billingRepository.save(billing);
    }

    /**
     * UC022 – View Billing History
     * Get all billing records for a patient (via medical record)
     */
    @Transactional(readOnly = true)
    public List<Billing> getBillingHistoryForPatient(Long patientId) {
        if (patientId == null || patientId <= 0) {
            throw new IllegalArgumentException("Patient ID must be valid");
        }

        logger.info("Fetching billing history for patient ID: {}", patientId);
        return billingRepository.findBillingHistoryForPatient(patientId);
    }

    /**
     * UC022 – View Billing History
     * Get billing record by ID
     */
    @Transactional(readOnly = true)
    public Optional<Billing> getBillingById(Long billId) {
        if (billId == null || billId <= 0) {
            throw new IllegalArgumentException("Bill ID must be valid");
        }
        logger.info("Fetching billing record with ID: {}", billId);
        return billingRepository.findById(billId);
    }

    /**
     * Get all billing records
     */
    @Transactional(readOnly = true)
    public List<Billing> getAllBillingRecords() {
        logger.info("Fetching all billing records");
        return billingRepository.findAll();
    }

    /**
     * Get billing records by payment status
     */
    @Transactional(readOnly = true)
    public List<Billing> getBillingByPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new IllegalArgumentException("Payment status is required");
        }

        logger.info("Fetching billing records with payment status: {}", paymentStatus);
        return billingRepository.findByPaymentStatus(paymentStatus.toString());
    }

    /**
     * Get billing record for a medical record
     */
    @Transactional(readOnly = true)
    public Optional<Billing> getBillingForMedicalRecord(Long medicalRecordId) {
        if (medicalRecordId == null || medicalRecordId <= 0) {
            throw new IllegalArgumentException("Medical record ID must be valid");
        }

        if (!medicalRecordRepository.existsById(medicalRecordId)) {
            throw new RuntimeException("Medical record not found with ID: " + medicalRecordId);
        }

        logger.info("Fetching billing record for medical record ID: {}", medicalRecordId);
        // Get all bills and filter by medical record
        return billingRepository.findAll()
                .stream()
                .filter(b -> b.getMedicalRecord().getRecordId().equals(medicalRecordId))
                .findFirst();
    }

    /**
     * Calculate total cost from procedures and lab tests for a medical record
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalCostForBilling(Long billId) {
        if (billId == null || billId <= 0) {
            throw new IllegalArgumentException("Bill ID must be valid");
        }

        if (!billingRepository.existsById(billId)) {
            throw new RuntimeException("Billing record not found with ID: " + billId);
        }

        // Calculate cost from procedures
        BigDecimal procedureCost = billingProceduresRepository.calculateTotalCostForBilling(billId);
        if (procedureCost == null) {
            procedureCost = BigDecimal.ZERO;
        }

        // Calculate cost from lab tests
        BigDecimal labTestCost = billingLabTestsRepository.calculateTotalCostForBilling(billId);
        if (labTestCost == null) {
            labTestCost = BigDecimal.ZERO;
        }

        logger.info("Total billing cost for bill ID {}: procedures={}, lab tests={}", 
                billId, procedureCost, labTestCost);
        return procedureCost.add(labTestCost);
    }

    /**
     * Add a procedure to a billing record
     */
    @Transactional
    public BillingProcedures addProcedureToBilling(Long billId, Long procedureId) {
        if (billId == null || billId <= 0) {
            throw new IllegalArgumentException("Bill ID must be valid");
        }
        if (procedureId == null || procedureId <= 0) {
            throw new IllegalArgumentException("Procedure ID must be valid");
        }

        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Billing record not found with ID: " + billId));

        // Note: You'll need to inject ProcedureRepository to get the procedure
        // This is a placeholder - adjust based on your actual Procedure entity
        logger.info("Adding procedure {} to billing record {}", procedureId, billId);
        
        return null; // Will be implemented with ProcedureRepository injection
    }

    /**
     * Add a lab test to a billing record
     */
    @Transactional
    public BillingLabTests addLabTestToBilling(Long billId, Long testId) {
        if (billId == null || billId <= 0) {
            throw new IllegalArgumentException("Bill ID must be valid");
        }
        if (testId == null || testId <= 0) {
            throw new IllegalArgumentException("Test ID must be valid");
        }

        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Billing record not found with ID: " + billId));

        // Note: You'll need to inject LabTestRepository to get the test
        // This is a placeholder - adjust based on your actual LabTests entity
        logger.info("Adding lab test {} to billing record {}", testId, billId);
        
        return null; // Will be implemented with LabTestRepository injection
    }
}
