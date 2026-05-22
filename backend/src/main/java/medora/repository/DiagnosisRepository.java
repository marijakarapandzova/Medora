package medora.repository;


import medora.models.domain.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
//UC009 – Record Diagnosis
   // A doctor adds a diagnosis to a patient’s medical record.
   // Use save() method from JpaRepository to create new diagnoses

    // Find all diagnoses for a specific patient
    List<Diagnosis> findByPatientPatientId(Long patientId);

    // Find all diagnoses recorded by a specific doctor
    List<Diagnosis> findByDoctorDoctorId(Long doctorId);

    // Helper: Search diagnoses by name (for UC026)
    @Query("""
        SELECT d FROM Diagnosis d
        WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Diagnosis> findByNameContainingIgnoreCase(@Param("name") String name);

    // Helper: Find diagnoses for a specific patient by name
    @Query("""
        SELECT d FROM Diagnosis d
        WHERE d.patient.patientId = :patientId
        AND LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Diagnosis> findByPatientAndNameContaining(@Param("patientId") Long patientId, @Param("name") String name);
}