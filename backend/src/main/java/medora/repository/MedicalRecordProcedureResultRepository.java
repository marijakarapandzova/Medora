package medora.repository;

import medora.models.domain.MedicalRecordProcedureResults;
import medora.models.domain.id.MedicalRecordProcedureResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicalRecordProcedureResultRepository extends JpaRepository<MedicalRecordProcedureResults, MedicalRecordProcedureResultId> {

    // Find all procedure results for a medical record
    List<MedicalRecordProcedureResults> findByMedicalRecordRecordId(Long recordId);

    // Check if a procedure result is already linked to a medical record
    @Query("""
        SELECT CASE WHEN COUNT(mrpr) > 0 THEN true ELSE false END
        FROM MedicalRecordProcedureResults mrpr
        WHERE mrpr.medicalRecord.recordId = :recordId
        AND mrpr.procedureResult.resultId = :resultId
    """)
    boolean existsByMedicalRecordAndProcedureResult(@Param("recordId") Long recordId, @Param("resultId") Long resultId);
}
