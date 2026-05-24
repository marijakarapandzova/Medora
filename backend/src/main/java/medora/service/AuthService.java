package medora.service;

import medora.models.domain.User;
import medora.repository.UserRepository;
import medora.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
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

        // Simple password check (in production, use BCrypt)
        if (!user.getPassword().equals(password)) {
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

    @Transactional
    public void createUser(String username, String password, String role, String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User(username, password, role, firstName, lastName);
        userRepository.save(user);
        logger.info("User created: {} with role: {}", username, role);
    }
}
