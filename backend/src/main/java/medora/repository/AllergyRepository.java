package medora.repository;

import medora.models.domain.Allergies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AllergyRepository extends JpaRepository<Allergies, Long> {
//UC011 – Record Allergies
//A doctor records patient allergies in the medical record.
// Use save() method from JpaRepository

// Find allergies by name
    Optional<Allergies> findByNameIgnoreCase(String name);

    // Helper: Get all allergies for a patient
    @Query("""
        SELECT a FROM Allergies a
        WHERE a IN (
            SELECT pa.allergy FROM PatientAllergy pa
            WHERE pa.patient.patientId = :patientId
        )
    """)
    List<Allergies> findByPatientId(@Param("patientId") Long patientId);

    // Helper: Get all allergies recorded in a medical record
    @Query("""
        SELECT a FROM Allergies a
        WHERE a IN (
            SELECT mra.allergy FROM MedicalRecordAllergies mra
             WHERE mra.medicalRecord.recordId = :recordId
        )
    """)
    List<Allergies> findByMedicalRecordId(@Param("recordId") Long recordId);

    // Helper: Search allergies by name (case-insensitive)
    @Query("""
        SELECT a FROM Allergies a
        WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Allergies> findByNameContainingIgnoreCase(@Param("name") String name);
}