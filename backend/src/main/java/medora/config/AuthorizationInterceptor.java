package medora.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import medora.util.SecurityUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Request interceptor that enforces JWT token validation and role-based access control.
 *
 * Checks:
 * 1. Non-auth endpoints require a valid JWT token
 * 2. Role-specific endpoints require the appropriate role
 *
 * Resource-level ownership checks (e.g., patient can only access their own data)
 * are handled in individual service methods, not here.
 */
@Component




public class AuthorizationInterceptor implements HandlerInterceptor {

    private final SecurityUtil securityUtil;

    public AuthorizationInterceptor(SecurityUtil securityUtil) {
        this.securityUtil = securityUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Allow login/registration endpoints without authentication
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // Allow health checks
        if (path.startsWith("/actuator/")) {
            return true;
        }

        // All other endpoints require a valid token
        if (!securityUtil.isValidToken(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized - missing or invalid token\"}");
            return false;
        }

        // Enforce role-based access control
        if (path.startsWith("/api/admin/")) {
            if (!securityUtil.hasRole("ADMIN", request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden - ADMIN role required\"}");
                return false;
            }
        } else if (path.startsWith("/api/doctor/")) {
            if (!securityUtil.hasAnyRole(new String[]{"DOCTOR", "ADMIN"}, request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden - DOCTOR or ADMIN role required\"}");
                return false;
            }
        } else if (path.startsWith("/api/patient/")) {
            if (!securityUtil.hasAnyRole(new String[]{"PATIENT", "DOCTOR", "ADMIN"}, request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden - PATIENT, DOCTOR, or ADMIN role required\"}");
                return false;
            }
        }

        // Request passes all checks
        return true;
    }
}
