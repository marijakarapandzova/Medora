package medora.service;

import medora.models.domain.Doctors;
import medora.models.domain.Departments;
import medora.models.domain.User;
import medora.repository.DoctorRepository;
import medora.repository.DepartmentRepository;
import medora.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * DoctorService handles doctor profile and specialization operations.
 * UC024 – View Doctors by Department
 * UC025 – View Doctor Profile
 */
@Service
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository,
                         DepartmentRepository departmentRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * UC025 – View Doctor Profile
     */
    @Transactional(readOnly = true)
    public Doctors getDoctorById(Long doctorId) {
        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }

        logger.info("Fetching doctor with ID: {}", doctorId);

        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));
    }

    /**
     * Get doctor by email
     */
    @Transactional(readOnly = true)
    public Doctors getDoctorByEmail(String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        logger.info("Fetching doctor with email: {}", emailAddress);

        return doctorRepository.findByEmailAddress(emailAddress)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + emailAddress));
    }

    /**
     * UC024 – View Doctors by Department
     */
    @Transactional(readOnly = true)
    public List<Doctors> getDoctorsByDepartment(Long departmentId) {
        if (departmentId == null || departmentId <= 0) {
            throw new IllegalArgumentException("Department ID must be valid");
        }

        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + departmentId));

        logger.info("Fetching doctors for department ID: {}", departmentId);

        return doctorRepository.findByDepartmentDepartmentId(departmentId);
    }

    /**
     * Get doctors by specialization
     */
    @Transactional(readOnly = true)
    public List<Doctors> getDoctorsBySpecialization(Long specializationId) {
        if (specializationId == null || specializationId <= 0) {
            throw new IllegalArgumentException("Specialization ID must be valid");
        }

        logger.info("Fetching doctors with specialization ID: {}", specializationId);

        return doctorRepository.findBySpecializationSpecializationId(specializationId);
    }

    /**
     * Get doctors by level
     */
    @Transactional(readOnly = true)
    public List<Doctors> getDoctorsByLevel(Long levelId) {
        if (levelId == null || levelId <= 0) {
            throw new IllegalArgumentException("Doctor level ID must be valid");
        }

        logger.info("Fetching doctors with level ID: {}", levelId);

        return doctorRepository.findByLevelLevelId(levelId);
    }

    /**
     * Get all doctors
     */
    @Transactional(readOnly = true)
    public List<Doctors> getAllDoctors() {
        logger.info("Fetching all doctors");
        return doctorRepository.findAll();
    }

    /**
     * Create doctor
     */
    @Transactional
    public Doctors createDoctor(Doctors doctor, String username, String rawPassword) {

        if (doctor == null) {
            throw new IllegalArgumentException("Doctor cannot be null");
        }

        if (doctor.getFirstName() == null || doctor.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Doctor first name is required");
        }

        if (doctor.getLastName() == null || doctor.getLastName().isBlank()) {
            throw new IllegalArgumentException("Doctor last name is required");
        }

        if (doctor.getEmailAddress() == null || doctor.getEmailAddress().isBlank()) {
            throw new IllegalArgumentException("Doctor email is required");
        }

        if (doctor.getDepartment() == null || doctor.getDepartment().getDepartmentId() == null) {
            throw new IllegalArgumentException("Doctor department is required");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // uniqueness check
        if (doctorRepository.findByEmailAddress(doctor.getEmailAddress()).isPresent()) {
            throw new RuntimeException("Doctor with this email already exists");
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already taken");
        }

        // validate department exists
        Departments department = departmentRepository.findById(
                doctor.getDepartment().getDepartmentId()
        ).orElseThrow(() -> new RuntimeException("Department not found"));

        doctor.setDepartment(department);

        // Create the login account first (user_id is DB-generated), since
        // Doctors.user_id is a required foreign key pointing to it.
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("DOCTOR");
        user.setFirstName(doctor.getFirstName());
        user.setLastName(doctor.getLastName());
        user.setIsActive(true);
        User savedUser = userRepository.save(user);

        doctor.setUser(savedUser);
        doctor.setDoctorId(doctorRepository.findMaxDoctorId() + 1);

        logger.info("Creating new doctor: {} {}", doctor.getFirstName(), doctor.getLastName());

        return doctorRepository.save(doctor);
    }

    /**
     * Update doctor
     */
    @Transactional
    public Doctors updateDoctor(Long doctorId, Doctors doctorDetails) {

        if (doctorId == null || doctorId <= 0) {
            throw new IllegalArgumentException("Doctor ID must be valid");
        }

        Doctors doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + doctorId));

        if (doctorDetails.getFirstName() != null && !doctorDetails.getFirstName().isBlank()) {
            doctor.setFirstName(doctorDetails.getFirstName());
        }

        if (doctorDetails.getLastName() != null && !doctorDetails.getLastName().isBlank()) {
            doctor.setLastName(doctorDetails.getLastName());
        }

        if (doctorDetails.getDepartment() != null &&
                doctorDetails.getDepartment().getDepartmentId() != null) {

            Departments dept = departmentRepository.findById(
                    doctorDetails.getDepartment().getDepartmentId()
            ).orElseThrow(() -> new RuntimeException("Department not found"));

            doctor.setDepartment(dept);
        }

        if (doctorDetails.getSpecialization() != null) {
            doctor.setSpecialization(doctorDetails.getSpecialization());
        }

        if (doctorDetails.getLevel() != null) {
            doctor.setLevel(doctorDetails.getLevel());
        }

        logger.info("Updating doctor with ID: {}", doctorId);

        return doctorRepository.save(doctor);
    }
}