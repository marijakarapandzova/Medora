package medora.repository;



import medora.models.domain.PerformedProcedures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PerformedProcedureRepository extends JpaRepository<PerformedProcedures, Long> {

    // UC016 – Record Procedure Entry
    // Use save() method from JpaRepository instead of raw SQL INSERT

    // UC017 – Record Procedure Outcome
    @Transactional
    @Modifying
    @Query("""
        UPDATE PerformedProcedures pp
        SET pp.notes = :notes
        WHERE pp.performedId = :id
    """)
    void updateProcedureOutcome(
            @Param("id") Long id,
            @Param("notes") String notes
    );

    // Helper: Get all procedures performed on a patient
    List<PerformedProcedures> findByPatientPatientId(Long patientId);

    // Helper: Get all procedures performed by a doctor
    List<PerformedProcedures> findByDoctorDoctorId(Long doctorId);

    // Helper: Get procedures for a specific diagnosis
    List<PerformedProcedures> findByDiagnosisDiagnosisId(Long diagnosisId);

    // UC015 – Link Medical Data - Get procedures linked to a medical record
    @Query("""
        SELECT mrp.procedure FROM MedicalRecordProcedures mrp
        WHERE mrp.medicalRecord.recordId = :recordId
    """)
    List<PerformedProcedures> findByMedicalRecordId(@Param("recordId") Long recordId);
}
