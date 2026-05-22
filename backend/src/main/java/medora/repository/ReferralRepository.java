package medora.repository;

import medora.models.domain.Referrals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReferralRepository extends JpaRepository<Referrals, Long> {

    // UC019 – Create Referral Record
    // Use save() method from JpaRepository instead of raw SQL INSERT
    // This properly manages entity lifecycle and relationships

    // Find referrals associated with a medical record
    List<Referrals> findByMedicalRecordRecordId(Long recordId);

    // Find referrals created by a specific doctor (referrals from)
    List<Referrals> findByFromDoctorDoctorId(Long doctorId);

    // Find referrals sent to a specific doctor (referrals to)
    List<Referrals> findByToDoctorDoctorId(Long doctorId);

    // Helper: Get all referrals for a patient
    @Query("""
        SELECT r FROM Referrals r
        WHERE r.medicalRecord.patient.patientId = :patientId
        ORDER BY r.referralDate DESC
    """)
    List<Referrals> findReferralsForPatient(@Param("patientId") Long patientId);

    // Helper: Get incoming referrals for a doctor
    @Query("""
        SELECT r FROM Referrals r
        WHERE r.toDoctor.doctorId = :doctorId
        ORDER BY r.referralDate DESC
    """)
    List<Referrals> findIncomingReferralsForDoctor(@Param("doctorId") Long doctorId);
}