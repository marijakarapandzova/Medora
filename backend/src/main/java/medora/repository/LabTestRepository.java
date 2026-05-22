package medora.repository;

import medora.models.domain.LabTests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LabTestRepository extends JpaRepository<LabTests, Long> {

    // UC013 – Record Lab Test Request
    // Use save() method from JpaRepository

    // Helper: Find lab test by name
    Optional<LabTests> findByTestNameIgnoreCase(String testName);

    // Helper: Search lab tests by name
    @Query("""
        SELECT lt FROM LabTests lt
        WHERE LOWER(lt.testName) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<LabTests> findByNameContainingIgnoreCase(@Param("name") String name);

    // Helper: Get all available lab tests
    @Query("""
        SELECT lt FROM LabTests lt
        ORDER BY lt.testName ASC
    """)
    List<LabTests> findAllLabTests();
}