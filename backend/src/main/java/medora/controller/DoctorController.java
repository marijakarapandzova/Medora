package medora.controller;

import medora.dto.DoctorDTO;
import medora.dto.CreateDoctorRequest;
import medora.dto.DoctorLevelDTO;
import medora.dto.DoctorSpecializationDTO;
import medora.dto.DepartmentDTO;
import medora.models.domain.Doctors;
import medora.models.domain.DoctorLevel;
import medora.models.domain.DoctorSpecialization;
import medora.models.domain.Departments;
import medora.service.DoctorService;
import medora.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private static final Logger logger = LoggerFactory.getLogger(DoctorController.class);

    private final DoctorService doctorService;
    private final SecurityUtil securityUtil;

    public DoctorController(DoctorService doctorService, SecurityUtil securityUtil) {
        this.doctorService = doctorService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public ResponseEntity<?> createDoctor(@RequestBody CreateDoctorRequest request, HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only ADMIN can create doctors
            if (!role.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can create doctors"));
            }
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "First name is required"));
            }
            if (request.getLastName() == null || request.getLastName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Last name is required"));
            }
            if (request.getEmailAddress() == null || request.getEmailAddress().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email is required"));
            }

            Doctors doctor = new Doctors();
            doctor.setFirstName(request.getFirstName());
            doctor.setLastName(request.getLastName());
            doctor.setEmailAddress(request.getEmailAddress());

            DoctorLevel level = new DoctorLevel();
            level.setLevelId(request.getLevelId());
            doctor.setLevel(level);

            DoctorSpecialization specialization = new DoctorSpecialization();
            specialization.setSpecializationId(request.getSpecializationId());
            doctor.setSpecialization(specialization);

            Departments department = new Departments();
            department.setDepartmentId(request.getDepartmentId());
            doctor.setDepartment(department);

            Doctors createdDoctor = doctorService.createDoctor(doctor);
            DoctorDTO dto = convertToDTO(createdDoctor);

            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException e) {
            logger.error("Error creating doctor: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating doctor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create doctor: " + e.getMessage()));
        }
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching doctor with ID: {}", doctorId);
            Doctors doctor = doctorService.getDoctorById(doctorId);
            DoctorDTO dto = convertToDTO(doctor);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error fetching doctor: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error fetching doctor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch doctor: " + e.getMessage()));
        }
    }

    @GetMapping("/email/{emailAddress}")
    public ResponseEntity<?> getDoctorByEmail(@PathVariable String emailAddress) {
        try {
            logger.info("Fetching doctor with email: {}", emailAddress);
            Doctors doctor = doctorService.getDoctorByEmail(emailAddress);
            DoctorDTO dto = convertToDTO(doctor);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error fetching doctor: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error fetching doctor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch doctor: " + e.getMessage()));
        }
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getDoctorsByDepartment(@PathVariable Long departmentId) {
        try {
            logger.info("Fetching doctors for department ID: {}", departmentId);
            List<Doctors> doctors = doctorService.getDoctorsByDepartment(departmentId);
            List<DoctorDTO> dtos = doctors.stream()
                    .map(this::convertToDTO)
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

    @GetMapping("/specialization/{specializationId}")
    public ResponseEntity<?> getDoctorsBySpecialization(@PathVariable Long specializationId) {
        try {
            logger.info("Fetching doctors with specialization ID: {}", specializationId);
            List<Doctors> doctors = doctorService.getDoctorsBySpecialization(specializationId);
            List<DoctorDTO> dtos = doctors.stream()
                    .map(this::convertToDTO)
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

    @GetMapping("/level/{levelId}")
    public ResponseEntity<?> getDoctorsByLevel(@PathVariable Long levelId) {
        try {
            logger.info("Fetching doctors with level ID: {}", levelId);
            List<Doctors> doctors = doctorService.getDoctorsByLevel(levelId);
            List<DoctorDTO> dtos = doctors.stream()
                    .map(this::convertToDTO)
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

    @GetMapping
    public ResponseEntity<?> getAllDoctors() {
        try {
            logger.info("Fetching all doctors");
            List<Doctors> doctors = doctorService.getAllDoctors();
            List<DoctorDTO> dtos = doctors.stream()
                    .map(this::convertToDTO)
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

    @PutMapping("/{doctorId}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long doctorId,
                                          @RequestBody CreateDoctorRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            String role = securityUtil.getRoleFromRequest(httpRequest);
            if (role == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }

            // Only ADMIN can update doctors
            if (!role.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only administrators can update doctors"));
            }

            logger.info("Updating doctor with ID: {}", doctorId);

            Doctors doctorDetails = new Doctors();
            doctorDetails.setFirstName(request.getFirstName());
            doctorDetails.setLastName(request.getLastName());

            if (request.getLevelId() != null) {
                DoctorLevel level = new DoctorLevel();
                level.setLevelId(request.getLevelId());
                doctorDetails.setLevel(level);
            }

            if (request.getSpecializationId() != null) {
                DoctorSpecialization specialization = new DoctorSpecialization();
                specialization.setSpecializationId(request.getSpecializationId());
                doctorDetails.setSpecialization(specialization);
            }

            if (request.getDepartmentId() != null) {
                Departments department = new Departments();
                department.setDepartmentId(request.getDepartmentId());
                doctorDetails.setDepartment(department);
            }

            Doctors updatedDoctor = doctorService.updateDoctor(doctorId, doctorDetails);
            DoctorDTO dto = convertToDTO(updatedDoctor);

            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            logger.error("Error updating doctor: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating doctor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update doctor: " + e.getMessage()));
        }
    }

    private DoctorDTO convertToDTO(Doctors doctor) {
        DoctorLevelDTO levelDTO = null;
        if (doctor.getLevel() != null) {
            levelDTO = new DoctorLevelDTO(doctor.getLevel().getLevelId(), doctor.getLevel().getLevel());
        }

        DoctorSpecializationDTO specializationDTO = null;
        if (doctor.getSpecialization() != null) {
            specializationDTO = new DoctorSpecializationDTO(
                    doctor.getSpecialization().getSpecializationId(),
                    doctor.getSpecialization().getSpecializationName()
            );
        }

        DepartmentDTO departmentDTO = null;
        if (doctor.getDepartment() != null) {
            departmentDTO = new DepartmentDTO(
                    doctor.getDepartment().getDepartmentId(),
                    doctor.getDepartment().getDepartmentName()
            );
        }

        return new DoctorDTO(
                doctor.getDoctorId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getEmailAddress(),
                levelDTO,
                specializationDTO,
                departmentDTO
        );
    }
}
