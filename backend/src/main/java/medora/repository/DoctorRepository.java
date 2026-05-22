package medora.repository;


import medora.models.domain.Doctors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctors, Long> {

    // UC023 – View Departments (see DepartmentRepository)
    // UC024 – View Doctors by Department
    // UC025 – View Doctor Profile

    // UC024 – Find all doctors assigned to a specific department
    List<Doctors> findByDepartmentDepartmentId(Long departmentId);

    // UC025 – View Doctor Profile with full details
    @Query("""
        SELECT d FROM Doctors d
        LEFT JOIN FETCH d.specialization
        LEFT JOIN FETCH d.level
        LEFT JOIN FETCH d.department
        WHERE d.doctorId = :doctorId
    """)
    Optional<Doctors> getDoctorProfile(@Param("doctorId") Long doctorId);

    // Helper: Find doctors by specialization
    @Query("""
        SELECT d FROM Doctors d
        WHERE d.specialization.specializationId = :specializationId
    """)
    List<Doctors> findBySpecializationId(@Param("specializationId") Long specializationId);

    // Helper: Find doctors by level
    @Query("""
        SELECT d FROM Doctors d
        WHERE d.level.levelId = :levelId
    """)
    List<Doctors> findByLevelId(@Param("levelId") Long levelId);

    // Helper: Find doctors by name
    @Query("""
        SELECT d FROM Doctors d
        WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
           OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Doctors> findByNameContainingIgnoreCase(@Param("name") String name);

    // Helper: Find doctors in a department with a specific specialization
    @Query("""
        SELECT d FROM Doctors d
        WHERE d.department.departmentId = :departmentId
        AND d.specialization.specializationId = :specializationId
    """)
    List<Doctors> findByDepartmentAndSpecialization(
            @Param("departmentId") Long departmentId,
            @Param("specializationId") Long specializationId
    );

    // Helper: Get doctor by email (for login/verification)
    Optional<Doctors> findByEmailAddressIgnoreCase(String emailAddress);
    // UC025 – View Doctor Profile
    Optional<Doctors> findByEmailAddress(String emailAddress);




    // filter by specialization
    List<Doctors> findBySpecializationSpecializationId(Long specializationId);

    // filter by level
    List<Doctors> findByLevelLevelId(Long levelId);
}

