package medora.service;

import medora.models.domain.Billing;
import medora.models.domain.MedicalRecord;
import medora.models.domain.Admin;
import medora.models.domain.BillingLabTests;
import medora.models.domain.BillingProcedures;
import medora.models.domain.PerformedProcedures;
import medora.models.domain.PerformedLabTests;
import medora.models.enums.PaymentStatus;
import medora.repository.BillingRepository;
import medora.repository.MedicalRecordRepository;
import medora.repository.AdminRepository;
import medora.repository.BillingLabTestsRepository;
import medora.repository.BillingProceduresRepository;
import medora.repository.PerformedProcedureRepository;
import medora.repository.PerformedLabTestRepository;
import medora.repository.PatientRepository;
import medora.dto.BillingDetailDTO;
import medora.dto.BillingItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

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
    private final PerformedProcedureRepository performedProcedureRepository;
    private final PerformedLabTestRepository performedLabTestRepository;
    private final PatientRepository patientRepository;

    public BillingService(BillingRepository billingRepository,
                          MedicalRecordRepository medicalRecordRepository,
                          AdminRepository adminRepository,
                          BillingLabTestsRepository billingLabTestsRepository,
                          BillingProceduresRepository billingProceduresRepository,
                          PerformedProcedureRepository performedProcedureRepository,
                          PerformedLabTestRepository performedLabTestRepository,
                          PatientRepository patientRepository) {
        this.billingRepository = billingRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.adminRepository = adminRepository;
        this.billingLabTestsRepository = billingLabTestsRepository;
        this.billingProceduresRepository = billingProceduresRepository;
        this.performedProcedureRepository = performedProcedureRepository;
        this.performedLabTestRepository = performedLabTestRepository;
        this.patientRepository = patientRepository;
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

    /**
     * UC020 – Auto-generate billing when a procedure or lab test is performed
     * Creates a billing record if one doesn't exist for the patient on that date
     * Calculates total cost from all procedures and lab tests performed that day
     */
    @Transactional
    public void autoGenerateBillingForPatientService(Long patientId, LocalDate serviceDate) {
        try {
            if (patientId == null || patientId <= 0) {
                throw new IllegalArgumentException("Patient ID must be valid");
            }
            if (serviceDate == null) {
                throw new IllegalArgumentException("Service date must be valid");
            }

            logger.info("Starting auto-billing for patient {} on date {}", patientId, serviceDate);

            // Get patient's medical record (or create one if it doesn't exist)
            MedicalRecord medicalRecord = medicalRecordRepository.findByPatientPatientId(patientId)
                    .orElseGet(() -> {
                        logger.info("Creating new medical record for patient {}", patientId);
                        MedicalRecord newRecord = new MedicalRecord();
                        newRecord.setPatient(patientRepository.findById(patientId)
                                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId)));
                        return medicalRecordRepository.save(newRecord);
                    });

            logger.info("Using medical record {} for patient {}", medicalRecord.getRecordId(), patientId);

            // Get all procedures and lab tests for the patient on that date
            List<PerformedProcedures> procedures = performedProcedureRepository.findByPatientAndDate(patientId, serviceDate);
            List<PerformedLabTests> labTests = performedLabTestRepository.findByPatientAndDate(patientId, serviceDate);

            logger.info("Found {} procedures and {} lab tests for patient {} on {}",
                    procedures.size(), labTests.size(), patientId, serviceDate);

            // Only generate billing if there are procedures or lab tests on that date
            if (procedures.isEmpty() && labTests.isEmpty()) {
                logger.info("No procedures or lab tests found for patient {} on {}", patientId, serviceDate);
                return;
            }

            // Calculate total cost
            BigDecimal procedureCost = procedures.stream()
                    .map(p -> {
                        BigDecimal cost = p.getProcedure().getCost();
                        logger.debug("Procedure {} cost: {}", p.getProcedure().getProcedureId(), cost);
                        return cost;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal labTestCost = labTests.stream()
                    .map(lt -> {
                        BigDecimal cost = lt.getLabTest().getCost();
                        logger.debug("Lab test {} cost: {}", lt.getLabTest().getTestId(), cost);
                        return cost;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = procedureCost.add(labTestCost);
            logger.info("Total cost calculation: procedures={}, labTests={}, total={}", procedureCost, labTestCost, totalCost);

            // Check if billing already exists for this patient on this date
            Billing billing = billingRepository.findBillingForPatientOnDate(patientId, serviceDate);

            if (billing != null) {
                logger.info("Billing record {} already exists for patient {} on {}, updating with new total",
                        billing.getBillId(), patientId, serviceDate);
                billing.setTotalCost(totalCost);
            } else {
                // Get default admin (first admin in system)
                Admin admin = adminRepository.findAll()
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No admin found in system"));

                // Create new billing record
                billing = new Billing();
                billing.setMedicalRecord(medicalRecord);
                billing.setAdmin(admin);
                billing.setTotalCost(totalCost);
                billing.setPaymentStatus(PaymentStatus.PENDING);
                billing.setPaymentDate(serviceDate);

                logger.info("Creating new billing record for patient {} on {}", patientId, serviceDate);
            }

            Billing savedBilling = billingRepository.save(billing);
            logger.info("Billing record {} for patient {} on {} with total cost: {}",
                    savedBilling.getBillId(), patientId, serviceDate, totalCost);

            // Link procedures to billing (only if not already linked)
            for (PerformedProcedures procedure : procedures) {
                try {
                    BillingProcedures billingProcedure = new BillingProcedures(savedBilling, procedure.getProcedure());
                    billingProceduresRepository.save(billingProcedure);
                    logger.debug("Linked procedure {} to billing {}", procedure.getProcedure().getProcedureId(), savedBilling.getBillId());
                } catch (Exception e) {
                    logger.debug("Procedure {} already linked to billing {}", procedure.getProcedure().getProcedureId(), savedBilling.getBillId());
                }
            }

            // Link lab tests to billing (only if not already linked)
            for (PerformedLabTests labTest : labTests) {
                try {
                    BillingLabTests billingLabTest = new BillingLabTests(savedBilling, labTest.getLabTest());
                    billingLabTestsRepository.save(billingLabTest);
                    logger.debug("Linked lab test {} to billing {}", labTest.getLabTest().getTestId(), savedBilling.getBillId());
                } catch (Exception e) {
                    logger.debug("Lab test {} already linked to billing {}", labTest.getLabTest().getTestId(), savedBilling.getBillId());
                }
            }

            logger.info("Successfully processed {} procedures and {} lab tests for billing record {}",
                    procedures.size(), labTests.size(), savedBilling.getBillId());

        } catch (Exception e) {
            logger.error("Error in auto-billing for patient {} on date {}: {}", patientId, serviceDate, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get detailed billing information with itemized procedures and lab tests
     */
    @Transactional(readOnly = true)
    public BillingDetailDTO getBillingDetail(Long billId) {
        if (billId == null || billId <= 0) {
            throw new IllegalArgumentException("Bill ID must be valid");
        }

        Billing billing = billingRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Billing record not found with ID: " + billId));

        // Get procedures for this bill
        List<Object[]> procedureResults = billingRepository.findProceduresForBilling(billId);
        List<BillingItemDTO> procedures = new ArrayList<>();
        for (Object[] row : procedureResults) {
            procedures.add(new BillingItemDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (BigDecimal) row[2]
            ));
        }

        // Get lab tests for this bill
        List<Object[]> labTestResults = billingRepository.findLabTestsForBilling(billId);
        List<BillingItemDTO> labTests = new ArrayList<>();
        for (Object[] row : labTestResults) {
            labTests.add(new BillingItemDTO(
                    ((Number) row[0]).longValue(),
                    (String) row[1],
                    (BigDecimal) row[2]
            ));
        }

        // Build the detail DTO
        BillingDetailDTO detail = new BillingDetailDTO();
        detail.setBillId(billing.getBillId());
        detail.setPatientName(billing.getMedicalRecord().getPatient().getFirstName() + " " +
                billing.getMedicalRecord().getPatient().getLastName());
        detail.setPatientEmbg(billing.getMedicalRecord().getPatient().getEmbg());
        detail.setPatientPhone(billing.getMedicalRecord().getPatient().getPhoneNumber());
        detail.setTotalCost(billing.getTotalCost());
        detail.setPaymentStatus(billing.getPaymentStatus().toString());
        detail.setPaymentDate(billing.getPaymentDate());
        detail.setBillDate(billing.getPaymentDate());
        detail.setProcedures(procedures);
        detail.setLabTests(labTests);

        logger.info("Retrieved detailed billing information for bill {}", billId);
        return detail;
    }
}
