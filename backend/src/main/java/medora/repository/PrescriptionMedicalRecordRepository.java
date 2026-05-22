package medora.repository;

import medora.models.domain.PrescriptionMedicalRecord;
import medora.models.domain.id.PrescriptionMedicalRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionMedicalRecordRepository 
        extends JpaRepository<PrescriptionMedicalRecord, PrescriptionMedicalRecordId> {

    // UC012 – Record Prescription
    // Find all prescriptions linked to a medical record
    @Query("""
        SELECT pmr FROM PrescriptionMedicalRecord pmr
        WHERE pmr.medicalRecord.recordId = :recordId
    """)
    List<PrescriptionMedicalRecord> findByMedicalRecordRecordId(@Param("recordId") Long recordId);

    // Find a specific prescription for a medical record
    @Query("""
        SELECT pmr FROM PrescriptionMedicalRecord pmr
        WHERE pmr.medicalRecord.recordId = :recordId
        AND pmr.prescription.prescriptionId = :prescriptionId
    """)
    PrescriptionMedicalRecord findByMedicalRecordAndPrescription(
            @Param("recordId") Long recordId,
            @Param("prescriptionId") Long prescriptionId
    );

    // Find all prescriptions for a medical record by prescription ID
    @Query("""
        SELECT pmr FROM PrescriptionMedicalRecord pmr
        WHERE pmr.medicalRecord.recordId = :recordId
        AND pmr.prescription.prescriptionId = :prescriptionId
    """)
    List<PrescriptionMedicalRecord> findByMedicalRecordRecordIdAndPrescriptionPrescriptionId(
            @Param("recordId") Long recordId,
            @Param("prescriptionId") Long prescriptionId
    );
}

