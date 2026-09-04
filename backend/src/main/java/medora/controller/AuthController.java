package medora.controller;

import medora.dto.LoginRequest;
import medora.service.AuthService;
import medora.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final SecurityUtil securityUtil;

    public AuthController(AuthService authService, SecurityUtil securityUtil) {
        this.authService = authService;
        this.securityUtil = securityUtil;
    }

    /**
     * UC002 – User Login
     * Authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            if (loginRequest.getUsername() == null || loginRequest.getUsername().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username is required"));
            }
            if (loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password is required"));
            }

            Map<String, Object> response = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Login error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }

    /**
     * UC003 – User Logout
     * Client-side logout (token is discarded)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "healthy"));
    }

    /**
     * Debug endpoint to test token validation
     */
    @GetMapping("/debug/token")
    public ResponseEntity<?> debugToken(HttpServletRequest httpRequest) {
        String role = securityUtil.getRoleFromRequest(httpRequest);
        String username = securityUtil.getUsernameFromRequest(httpRequest);
        Long userId = securityUtil.getUserIdFromRequest(httpRequest);

        Map<String, Object> debug = new HashMap<>();
        debug.put("role", role);
        debug.put("username", username);
        debug.put("userId", userId);
        debug.put("isValid", role != null);

        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(debug);
        }
        return ResponseEntity.ok(debug);
    }

    /**
     * Test endpoint - no auth required, returns test token
     */
    @GetMapping("/test/generate-token")
    public ResponseEntity<?> generateTestToken() {
        String testToken = authService.generateTestToken();
        Map<String, Object> response = new HashMap<>();
        response.put("token", testToken);
        response.put("message", "Copy this token and try it");
        return ResponseEntity.ok(response);
    }
}
