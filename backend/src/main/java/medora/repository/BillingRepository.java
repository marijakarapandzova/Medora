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

    // Helper: Get billing records by payment status
    @Query("""
        SELECT b FROM Billing b
        WHERE b.paymentStatus = :status
        ORDER BY b.paymentDate DESC
    """)
    List<Billing> findByPaymentStatus(@Param("status") String status);

    // Helper: Get unpaid bills for a patient
    @Query("""
        SELECT b FROM Billing b
        WHERE b.medicalRecord.patient.patientId = :patientId
        AND b.paymentStatus != 'PAID'
        ORDER BY b.paymentDate DESC
    """)
    List<Billing> findUnpaidBillsForPatient(@Param("patientId") Long patientId);

    // Helper: Get all bills for a patient sorted by date
    @Query("""
        SELECT b FROM Billing b
        WHERE b.medicalRecord.patient.patientId = :patientId
        ORDER BY b.paymentDate DESC
    """)
    List<Billing> findBillingHistoryForPatient(@Param("patientId") Long patientId);
}
