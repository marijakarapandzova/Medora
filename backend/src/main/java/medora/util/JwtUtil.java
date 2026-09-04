package medora.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpirationMs;

    @PostConstruct
    public void init() {
        logger.info("JwtUtil initialized - JWT Secret length: {}, Expiration: {}ms",
                jwtSecret != null ? jwtSecret.length() : 0, jwtExpirationMs);
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            logger.error("⚠️ JWT_SECRET is not set or empty!");
        } else {
            logger.info("✅ JWT Secret is configured (length: {})", jwtSecret.length());
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String username, String role, Long userId, Long patientId) {
        return generateTokenWithDoctorId(username, role, userId, patientId, null);
    }

    public String generateTokenWithDoctorId(String username, String role, Long userId, Long patientId, Long doctorId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        if (patientId != null) {
            claims.put("patientId", patientId);
        }
        if (doctorId != null) {
            claims.put("doctorId", doctorId);
        }
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        logger.info("🔐 Creating token - secret hash: {}", jwtSecret.hashCode());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> (String) claims.get("role"));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> ((Number) claims.get("userId")).longValue());
    }

    public Long extractPatientId(String token) {
        return extractClaim(token, claims -> {
            Object patientId = claims.get("patientId");
            return patientId != null ? ((Number) patientId).longValue() : null;
        });
    }

    public Long extractDoctorId(String token) {
        return extractClaim(token, claims -> {
            Object doctorId = claims.get("doctorId");
            return doctorId != null ? ((Number) doctorId).longValue() : null;
        });
    }

    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            logger.debug("Extracting claims from token");
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Failed to extract claims: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.isEmpty()) {
                logger.warn("Token validation failed: token is null or empty");
                return false;
            }

            logger.info("🔐 Validating token - secret hash: {}, secret length: {}, token length: {}",
                    jwtSecret.hashCode(), jwtSecret.length(), token.length());

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            logger.info("✅ Token validation succeeded");
            return true;
        } catch (io.jsonwebtoken.SignatureException e) {
            logger.error("❌ JWT Signature validation FAILED - secret mismatch? Error: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("❌ JWT Token EXPIRED: {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("❌ JWT Malformed: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("❌ JWT Token validation failed - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
