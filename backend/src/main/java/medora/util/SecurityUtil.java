package medora.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private final JwtUtil jwtUtil;

    public SecurityUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public String getUsernameFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.isTokenValid(token)) {
            return jwtUtil.extractUsername(token);
        }
        return null;
    }

    public String getRoleFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.isTokenValid(token)) {
            return jwtUtil.extractRole(token);
        }
        return null;
    }

    public Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.isTokenValid(token)) {
            return jwtUtil.extractUserId(token);
        }
        return null;
    }

    public Long getPatientIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.isTokenValid(token)) {
            return jwtUtil.extractPatientId(token);
        }
        return null;
    }

    public Long getDoctorIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null && jwtUtil.isTokenValid(token)) {
            return jwtUtil.extractDoctorId(token);
        }
        return null;
    }

    public boolean isValidToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        return token != null && jwtUtil.isTokenValid(token);
    }

    public boolean hasRole(String role, HttpServletRequest request) {
        String userRole = getRoleFromRequest(request);
        return userRole != null && userRole.equals(role);
    }

    public boolean hasAnyRole(String[] roles, HttpServletRequest request) {
        String userRole = getRoleFromRequest(request);
        if (userRole == null) {
            return false;
        }
        for (String role : roles) {
            if (userRole.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
