package medora.repository;

import medora.models.domain.Departments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Departments, Long> {

    // UC023 – View Departments
    @Query("""
        SELECT d FROM Departments d
        WHERE d.departmentName = :departmentName
    """)
    Optional<Departments> findByDepartmentName(String departmentName);
}

