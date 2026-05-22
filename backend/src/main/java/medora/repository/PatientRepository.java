package medora.repository;

import medora.models.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // UC004 – View Patient Profile
    // Find patient by EMBG (unique identifier)
    Optional<Patient> findByEmbg(String embg);

    // Find patient by email for login/contact purposes
    Optional<Patient> findByEmailAddress(String emailAddress);

    // UC026 – Helper for medical record search by patient name
    @Query("""
        SELECT p FROM Patient p
        WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
           OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Patient> findByNameContainingIgnoreCase(@Param("name") String name);
}