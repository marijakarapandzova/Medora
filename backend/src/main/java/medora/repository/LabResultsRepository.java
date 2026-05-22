package medora.repository;

import medora.models.domain.LabResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LabResultsRepository extends JpaRepository<LabResults, Long> {

    // UC014 – Store Lab Results
    // Use save() method from JpaRepository instead of raw SQL INSERT
    // This properly manages entity lifecycle and relationships

    // UC015 – Link Medical Data - Find lab results for a specific test
    List<LabResults> findByLabTestTestId(Long testId);

    // Helper: Find all results for a specific medical record
    @Query("""
        SELECT lr FROM LabResults lr
        WHERE lr IN (
            SELECT mrlr.labResult FROM MedicalRecordLabResults mrlr
            WHERE mrlr.medicalRecord.recordId = :recordId
        )
    """)
    List<LabResults> findByMedicalRecordId(@Param("recordId") Long recordId);

    // Helper: Find results by lab test ordered by date
    @Query("""
        SELECT lr FROM LabResults lr
        WHERE lr.labTest.testId = :testId
        ORDER BY lr.resultDate DESC
    """)
    List<LabResults> findLatestResultsByTest(@Param("testId") Long testId);
}