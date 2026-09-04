package medora.service;

import medora.models.domain.User;
import medora.repository.UserRepository;
import medora.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("Login attempt with non-existent username: {}", username);
            throw new RuntimeException("Invalid username or password");
        }

        User user = userOpt.get();

        if (!user.getIsActive()) {
            logger.warn("Login attempt with inactive user: {}", username);
            throw new RuntimeException("User account is inactive");
        }

        // Verify password using BCrypt
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Failed login attempt for user: {}", username);
            throw new RuntimeException("Invalid username or password");
        }

        Long patientId = user.getPatient() != null ? user.getPatient().getPatientId() : null;
        Long doctorId = user.getDoctor() != null ? user.getDoctor().getDoctorId() : null;
        String token = jwtUtil.generateTokenWithDoctorId(user.getUsername(), user.getRole(), user.getUserId(), patientId, doctorId);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getUserId());
        response.put("patientId", patientId);
        response.put("doctorId", doctorId);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());

        logger.info("User logged in successfully: {}", username);
        return response;
    }

    public String generateTestToken() {
        return jwtUtil.generateTokenWithDoctorId("admin", "ADMIN", 1L, null, null);
    }

    @Transactional
    public void createUser(String username, String password, String role, String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        // Hash password before storing
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, hashedPassword, role, firstName, lastName);
        userRepository.save(user);
        logger.info("User created: {} with role: {}", username, role);
    }

    /**
     * One-time migration: re-hashes any user whose stored password is still
     * plaintext (i.e. not already a BCrypt hash) into a proper BCrypt hash.
     * Safe to call more than once — already-hashed users are skipped.
     *
     * Intended to be run once via a temporary CommandLineRunner bean, then
     * the bean should be removed so this doesn't run on every startup.
     */
    @Transactional
    public int migratePlaintextPasswords() {
        List<User> allUsers = userRepository.findAll();
        int migratedCount = 0;

        for (User user : allUsers) {
            String currentPassword = user.getPassword();

            // BCrypt hashes always start with $2a$, $2b$, or $2y$ — anything
            // else is assumed to still be plaintext and needs migrating
            if (currentPassword != null && !currentPassword.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(currentPassword));
                userRepository.save(user);
                migratedCount++;
                logger.info("Migrated password for user: {}", user.getUsername());
            }
        }

        logger.info("Password migration complete: {} of {} users migrated", migratedCount, allUsers.size());
        return migratedCount;
    }
}