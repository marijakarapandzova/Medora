package medora.repository;

import medora.models.domain.ReportAllergy;
import medora.models.domain.id.ReportAllergyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportAllergyRepository extends JpaRepository<ReportAllergy, ReportAllergyId> {
    List<ReportAllergy> findByReportReportId(Long reportId);
}
