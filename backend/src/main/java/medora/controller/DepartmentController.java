package medora.controller;

import medora.models.domain.Departments;
import medora.models.domain.Doctors;
import medora.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    /**
     * UC023 – View Departments
     * Get all departments
     */
    @GetMapping
    public ResponseEntity<?> getAllDepartments() {
        try {
            logger.info("Fetching all departments");
            List<Departments> departments = departmentService.getAllDepartments();
            List<Map<String, Object>> dtos = departments.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error fetching departments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch departments: " + e.getMessage()));
        }
    }

    /**
     * Get department by ID
     */
    @GetMapping("/{departmentId}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long departmentId) {
        try {
            logger.info("Fetching department with ID: {}", departmentId);
            return departmentService.getDepartmentById(departmentId)
                    .map(dept -> ResponseEntity.ok(convertToDTO(dept)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching department: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch department: " + e.getMessage()));
        }
    }


    @GetMapping("/name/{departmentName}")
    public ResponseEntity<?> getDepartmentByName(@PathVariable String departmentName) {
        try {
            logger.info("Fetching department with name: {}", departmentName);
            return departmentService.getDepartmentByName(departmentName)
                    .map(dept -> ResponseEntity.ok(convertToDTO(dept)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            logger.error("Error fetching department: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch department: " + e.getMessage()));
        }
    }

    /**
     * UC024 – View Doctors by Department
     * Get all doctors in a specific department
     */
    @GetMapping("/{departmentId}/doctors")
    public ResponseEntity<?> getDoctorsByDepartment(@PathVariable Long departmentId) {
        try {
            logger.info("Fetching doctors for department ID: {}", departmentId);
            List<Doctors> doctors = departmentService.getDoctorsByDepartment(departmentId);
            List<Map<String, Object>> dtos = doctors.stream()
                    .map(this::convertDoctorToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            logger.error("Error fetching doctors: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error fetching doctors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch doctors: " + e.getMessage()));
        }
    }


    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody Map<String, String> request) {
        try {
            String departmentName = request.get("departmentName");
            if (departmentName == null || departmentName.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Department name is required"));
            }

            Departments department = new Departments();
            department.setDepartmentName(departmentName);

            Departments createdDept = departmentService.createDepartment(department);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(convertToDTO(createdDept));
        } catch (RuntimeException e) {
            logger.error("Error creating department: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create department: " + e.getMessage()));
        }
    }


    @PutMapping("/{departmentId}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long departmentId,
                                              @RequestBody Map<String, String> request) {
        try {
            String departmentName = request.get("departmentName");
            if (departmentName == null || departmentName.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Department name is required"));
            }

            Departments department = new Departments();
            department.setDepartmentName(departmentName);

            Departments updatedDept = departmentService.updateDepartment(departmentId, department);
            return ResponseEntity.ok(convertToDTO(updatedDept));
        } catch (RuntimeException e) {
            logger.error("Error updating department: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update department: " + e.getMessage()));
        }
    }

    private Map<String, Object> convertToDTO(Departments department) {
        return Map.of(
                "departmentId", department.getDepartmentId(),
                "departmentName", department.getDepartmentName()
        );
    }

    private Map<String, Object> convertDoctorToDTO(Doctors doctor) {
        return Map.of(
                "doctorId", doctor.getDoctorId(),
                "firstName", doctor.getFirstName(),
                "lastName", doctor.getLastName(),
                "emailAddress", doctor.getEmailAddress(),
                "departmentId", doctor.getDepartment() != null ? doctor.getDepartment().getDepartmentId() : null,
                "departmentName", doctor.getDepartment() != null ? doctor.getDepartment().getDepartmentName() : "N/A"
        );
    }
}
