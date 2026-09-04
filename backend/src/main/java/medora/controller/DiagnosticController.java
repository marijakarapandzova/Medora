package medora.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/referrals-count")
    public ResponseEntity<?> getReferralsCount() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM referrals")) {

            if (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("total_referrals", rs.getInt("count"));
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("total_referrals", 0));
    }

    @GetMapping("/referrals-recent")
    public ResponseEntity<?> getRecentReferrals() {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT referral_id, reason, referral_date, appointment_date, appointment_time " +
                     "FROM referrals " +
                     "ORDER BY referral_id DESC " +
                     "LIMIT 20")) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("referral_id", rs.getLong("referral_id"));
                row.put("reason", rs.getString("reason"));
                row.put("referral_date", rs.getDate("referral_date"));
                row.put("appointment_date", rs.getDate("appointment_date"));
                row.put("appointment_time", rs.getTime("appointment_time"));
                results.add(row);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/referrals-128-129-130")
    public ResponseEntity<?> checkSpecificReferrals() {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT referral_id, reason, referral_date, appointment_date, appointment_time " +
                     "FROM referrals " +
                     "WHERE referral_id IN (128, 129, 130) " +
                     "ORDER BY referral_id")) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("referral_id", rs.getLong("referral_id"));
                row.put("reason", rs.getString("reason"));
                row.put("referral_date", rs.getDate("referral_date"));
                row.put("appointment_date", rs.getDate("appointment_date"));
                row.put("appointment_time", rs.getTime("appointment_time"));
                results.add(row);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(results);
    }

    @GetMapping("/database-info")
    public ResponseEntity<?> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            info.put("database", conn.getCatalog());
            info.put("url", conn.getMetaData().getURL());
            info.put("username", conn.getMetaData().getUserName());
            info.put("driver", conn.getMetaData().getDriverName());
            info.put("schema", conn.getSchema());
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        return ResponseEntity.ok(info);
    }

    @GetMapping("/table-columns")
    public ResponseEntity<?> getTableColumns() {
        List<Map<String, Object>> columns = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT column_name, data_type, is_nullable " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = 'referrals' " +
                     "ORDER BY ordinal_position")) {

            while (rs.next()) {
                Map<String, Object> col = new HashMap<>();
                col.put("column_name", rs.getString("column_name"));
                col.put("data_type", rs.getString("data_type"));
                col.put("is_nullable", rs.getString("is_nullable"));
                columns.add(col);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
        return ResponseEntity.ok(columns);
    }
}
