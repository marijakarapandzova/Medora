package medora.repository;

import medora.models.domain.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // UC005 – View Medical Record (Full history)
    // UC008 – Access Medical Record
    Optional<MedicalRecord> findByPatientPatientId(Long patientId);

    // UC026 – Search Medical Records (with filters: name, EMBG, diagnosis, date range)
    @Query("""
        SELECT DISTINCT mr
        FROM MedicalRecord mr
        JOIN mr.patient p
        LEFT JOIN Diagnosis d ON d.patient.patientId = p.patientId
        WHERE
            (:name IS NULL OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                           OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:embg IS NULL OR p.embg = :embg)
        AND (:diagnosis IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :diagnosis, '%')))
        AND (:fromDate IS NULL OR mr.recordId >= :fromDate)
        AND (:toDate IS NULL OR mr.recordId <= :toDate)
    """)
    List<MedicalRecord> searchMedicalRecords(
            @Param("name") String name,
            @Param("embg") String embg,
            @Param("diagnosis") String diagnosis,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

}