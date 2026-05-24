package medora.repository;


import medora.models.domain.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, Long> {

    // UC020 – Generate Billing Record
    // UC021 – Record Payment Status
    // UC022 – View Billing History

    // UC022 – View Billing History (Patient view - through medical record)
    @Query("""
        SELECT b FROM Billing b
        WHERE b.medicalRecord.patient.patientId = :patientId
        ORDER BY b.paymentDate DESC
    """)
    List<Billing> findByPatientPatientId(Long patientId);

    // UC022 – View Billing History (Admin view)
    List<Billing> findByAdminAdminId(Long adminId);

    // UC021 – Record Payment Status
    @Transactional
    @Modifying
    @Query("""
        UPDATE Billing b
        SET b.paymentStatus = :status
        WHERE b.billId = :billId
    """)
    void updatePaymentStatus(
            @Param("billId") Long billId,
            @Param("status") String status
    );

    // UC020 – Helper to calculate total cost for a medical record
    @Query(value = """
        SELECT COALESCE(SUM(p.cost), 0) + COALESCE(SUM(l.cost), 0) AS total_cost
        FROM medical_records mr
        LEFT JOIN medical_record_procedures mrp ON mr.record_id = mrp.record_id
        LEFT JOIN procedures p ON mrp.procedure_id = p.procedure_id
        LEFT JOIN medical_record_lab_results mrl ON mr.record_id = mrl.record_id
        LEFT JOIN lab_results lr ON mrl.result_id = lr.result_id
        LEFT JOIN lab_tests l ON lr.test_id = l.test_id
        WHERE mr.record_id = :recordId
    """, nativeQuery = true)
    BigDecimal calculateTotalCostForMedicalRecord(@Param("recordId") Long recordId);

   //Get billing records by payment status
    @Query("""
        SELECT b FROM Billing b
        WHERE b.paymentStatus = :status
        ORDER BY b.paymentDate DESC
    """)
    List<Billing> findByPaymentStatus(@Param("status") String status);

    // Get unpaid bills for a patient
    @Query("""
        SELECT b FROM Billing b
        WHERE b.medicalRecord.patient.patientId = :patientId
        AND b.paymentStatus != 'PAID'
        ORDER BY b.paymentDate DESC
    """)
    List<Billing> findUnpaidBillsForPatient(@Param("patientId") Long patientId);

    //Get all bills for a patient sorted by date
    @Query("""
        SELECT b FROM Billing b
        WHERE b.medicalRecord.patient.patientId = :patientId
        ORDER BY b.paymentDate DESC
    """)
    List<Billing> findBillingHistoryForPatient(@Param("patientId") Long patientId);

    // UC020 – Auto billing: Check if billing exists for patient on a specific date
    @Query(value = """
        SELECT COUNT(b.bill_id) > 0
        FROM billing b
        JOIN medical_records mr ON b.record_id = mr.record_id
        WHERE mr.patient_id = :patientId
        AND CAST(b.payment_date AS DATE) = :billDate
    """, nativeQuery = true)
    boolean existsBillingForPatientOnDate(@Param("patientId") Long patientId, @Param("billDate") java.time.LocalDate billDate);

    // UC020 – Auto billing: Get billing for patient on a specific date
    @Query(value = """
        SELECT b.* FROM billing b
        JOIN medical_records mr ON b.record_id = mr.record_id
        WHERE mr.patient_id = :patientId
        AND CAST(b.payment_date AS DATE) = :billDate
        LIMIT 1
    """, nativeQuery = true)
    Billing findBillingForPatientOnDate(@Param("patientId") Long patientId, @Param("billDate") java.time.LocalDate billDate);

    // UC020 – Auto billing: Calculate total cost for patient on a specific date (using view)
    @Query(value = """
        SELECT COALESCE(total_cost, 0::decimal)
        FROM daily_patient_billing_totals
        WHERE patient_id = :patientId
        AND service_date = :serviceDate
    """, nativeQuery = true)
    BigDecimal calculateDailyTotalCostForPatient(@Param("patientId") Long patientId, @Param("serviceDate") java.time.LocalDate serviceDate);

    // UC020 – Get procedures for a billing record
    @Query(value = """
        SELECT p.procedure_id, p.procedure_type, p.cost
        FROM billing_procedures bp
        JOIN procedures p ON bp.procedure_id = p.procedure_id
        WHERE bp.bill_id = :billId
    """, nativeQuery = true)
    java.util.List<Object[]> findProceduresForBilling(@Param("billId") Long billId);

    // UC020 – Get lab tests for a billing record
    @Query(value = """
        SELECT l.test_id, l.test_name, l.cost
        FROM billing_lab_tests blt
        JOIN lab_tests l ON blt.test_id = l.test_id
        WHERE blt.bill_id = :billId
    """, nativeQuery = true)
    java.util.List<Object[]> findLabTestsForBilling(@Param("billId") Long billId);
}
