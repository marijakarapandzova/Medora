package medora.repository;



import medora.models.domain.Prescriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescriptions, Long> {

    // UC012 – Record Prescription
    // A doctor prescribes medication linked to the medical record.
    // Use save() method from JpaRepository

    // Search prescriptions by medication name
    List<Prescriptions> findByMedicationNameContainingIgnoreCase(String name);

    // Helper: Get all prescriptions for a specific medical record
    @Query("""
        SELECT p FROM Prescriptions p
        WHERE p IN (
            SELECT pmr.prescription FROM PrescriptionMedicalRecord pmr
            WHERE pmr.medicalRecord.recordId = :recordId
        )
    """)
    List<Prescriptions> findByMedicalRecordId(@Param("recordId") Long recordId);

    // Helper: Get all prescriptions for a patient
    @Query("""
        SELECT p FROM Prescriptions p
        WHERE p IN (
            SELECT pmr.prescription FROM PrescriptionMedicalRecord pmr
            WHERE pmr.medicalRecord.patient.patientId = :patientId
        )
    """)
    List<Prescriptions> findByPatientId(@Param("patientId") Long patientId);

    // Helper: Get active prescriptions for a patient (by duration)
    @Query("""
        SELECT p FROM Prescriptions p
        WHERE p IN (
            SELECT pmr.prescription FROM PrescriptionMedicalRecord pmr
            WHERE pmr.medicalRecord.patient.patientId = :patientId
            AND pmr.duration IS NOT NULL
        )
    """)
    List<Prescriptions> findActivePrescrriptionsForPatient(@Param("patientId") Long patientId);
}