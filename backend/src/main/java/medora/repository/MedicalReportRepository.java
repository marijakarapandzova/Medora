package medora.repository;


import medora.models.domain.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {

    // UC018 – Create Medical Report
    // Use save() method from JpaRepository instead of raw SQL INSERT
    // This properly manages entity lifecycle and relationships

    // UC005 – Retrieve medical reports for a medical record
    List<MedicalReport> findByMedicalRecordRecordId(Long recordId);

    // Retrieve reports created by a specific doctor
    List<MedicalReport> findByDoctorDoctorId(Long doctorId);

    // Helper: Get reports for a medical record ordered by date
    @Query("""
        SELECT mr FROM MedicalReport mr
         WHERE mr.medicalRecord.recordId = :recordId
        ORDER BY mr.reportDate DESC
    """)
    List<MedicalReport> getReportsForRecord(@Param("recordId") Long recordId);
}