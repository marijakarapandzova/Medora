package medora.service;

import medora.models.domain.Departments;
import medora.models.domain.Doctors;
import medora.repository.DepartmentRepository;
import medora.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * DepartmentService handles department operations.
 * UC023 – View Departments
 * UC024 – View Doctors by Department
 */
@Service
public class DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             DoctorRepository doctorRepository) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
    }


    @Transactional(readOnly = true)
    public List<Departments> getAllDepartments() {
        logger.info("Fetching all departments");
        return departmentRepository.findAll();
    }


    @Transactional(readOnly = true)
    public Optional<Departments> getDepartmentById(Long departmentId) {
        if (departmentId == null || departmentId <= 0) {
            throw new IllegalArgumentException("Department ID must be valid");
        }
        logger.info("Fetching department with ID: {}", departmentId);
        return departmentRepository.findById(departmentId);
    }


    @Transactional(readOnly = true)
    public Optional<Departments> getDepartmentByName(String departmentName) {
        if (departmentName == null || departmentName.isBlank()) {
            throw new IllegalArgumentException("Department name cannot be null or empty");
        }
        logger.info("Fetching department with name: {}", departmentName);
        return departmentRepository.findByDepartmentName(departmentName);
    }


    @Transactional(readOnly = true)
    public List<Doctors> getDoctorsByDepartment(Long departmentId) {
        if (departmentId == null || departmentId <= 0) {
            throw new IllegalArgumentException("Department ID must be valid");
        }

        // Verify department exists
        if (!departmentRepository.existsById(departmentId)) {
            throw new RuntimeException("Department not found with ID: " + departmentId);
        }

        logger.info("Fetching doctors for department ID: {}", departmentId);
        return doctorRepository.findByDepartmentDepartmentId(departmentId);
    }


    @Transactional
    public Departments createDepartment(Departments department) {
        if (department == null || department.getDepartmentName() == null ||
                department.getDepartmentName().isBlank()) {
            throw new IllegalArgumentException("Department name is required");
        }

        logger.info("Creating new department: {}", department.getDepartmentName());
        return departmentRepository.save(department);
    }


    @Transactional
    public Departments updateDepartment(Long departmentId, Departments departmentDetails) {
        if (departmentId == null || departmentId <= 0) {
            throw new IllegalArgumentException("Department ID must be valid");
        }

        Departments department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));

        if (departmentDetails.getDepartmentName() != null && !departmentDetails.getDepartmentName().isBlank()) {
            department.setDepartmentName(departmentDetails.getDepartmentName());
        }

        logger.info("Updating department with ID: {}", departmentId);
        return departmentRepository.save(department);
    }
}
