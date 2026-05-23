package medora.repository;

import medora.models.domain.ProcedureResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcedureResultRepository extends JpaRepository<ProcedureResults, Long> {

    // UC017 – Record Procedure Outcome
    List<ProcedureResults> findByProcedureProcedureId(Long procedureId);

    // Find results for a specific medical record
    @Query("""
        SELECT pr FROM ProcedureResults pr
        WHERE pr IN (
            SELECT mrpr.procedureResult FROM MedicalRecordProcedureResults mrpr
            WHERE mrpr.medicalRecord.recordId = :recordId
        )
    """)
    List<ProcedureResults> findByMedicalRecordId(@Param("recordId") Long recordId);

    // Find latest results for a procedure
    @Query("""
        SELECT pr FROM ProcedureResults pr
        WHERE pr.procedure.procedureId = :procedureId
        ORDER BY pr.resultDate DESC
    """)
    List<ProcedureResults> findLatestResultsByProcedure(@Param("procedureId") Long procedureId);
}
