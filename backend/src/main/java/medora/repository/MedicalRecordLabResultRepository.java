package medora.repository;



import medora.models.domain.MedicalRecordLabResults;
import medora.models.domain.id.MedicalRecordLabResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicalRecordLabResultRepository
        extends JpaRepository<MedicalRecordLabResults, MedicalRecordLabResultId> {

    // UC015 – Link lab result to medical record
    @Modifying
    @Query(value = """
        INSERT INTO medical_record_lab_results (record_id, result_id)
        VALUES (:recordId, :resultId)
    """, nativeQuery = true)
    void linkLabResult(
            @Param("recordId") Long recordId,
            @Param("resultId") Long resultId
    );

    // Find all lab results linked to a medical record
    List<MedicalRecordLabResults> findByMedicalRecordRecordId(Long recordId);
}


