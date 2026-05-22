package medora.repository;


import medora.models.domain.Symptoms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SymptomRepository extends JpaRepository<Symptoms, Long> {
//UC010 – Record Symptoms
  //  A doctor records patient symptoms in the medical record.
  //  Use save() method from JpaRepository

    // Search symptoms by name
            List<Symptoms> findByNameContainingIgnoreCase(String name);

    // Helper: Get all symptoms for a patient's medical record
    @Query("""
        SELECT s FROM Symptoms s
        WHERE s IN (
            SELECT mrs.symptom FROM MedicalRecordSymptoms mrs
            WHERE mrs.medicalRecord.recordId = :recordId
        )
    """)
    List<Symptoms> findByMedicalRecordId(@Param("recordId") Long recordId);

    // Helper: Get all symptoms for a patient
    @Query("""
        SELECT s FROM Symptoms s
        WHERE s IN (
            SELECT mrs.symptom FROM MedicalRecordSymptoms mrs
            WHERE mrs.medicalRecord.patient.patientId = :patientId
        )
    """)
    List<Symptoms> findByPatientId(@Param("patientId") Long patientId);
}