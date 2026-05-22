package medora.repository;


import medora.models.domain.MedicalRecordProcedures;
import medora.models.domain.id.MedicalRecordProcedureId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicalRecordProcedureRepository
        extends JpaRepository<MedicalRecordProcedures, MedicalRecordProcedureId> {

    @Modifying
    @Query(value = """
        INSERT INTO medical_record_procedures (record_id, procedure_id)
        VALUES (:recordId, :procedureId)
    """, nativeQuery = true)
    void linkProcedure(
            @Param("recordId") Long recordId,
            @Param("procedureId") Long procedureId
    );

    // Find all procedures linked to a medical record
    List<MedicalRecordProcedures> findByMedicalRecordRecordId(Long recordId);

    // Check if a procedure is already linked to a medical record
    boolean existsByMedicalRecordRecordIdAndProcedureProcedureId(Long recordId, Long procedureId);
}
