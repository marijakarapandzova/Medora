package medora.repository;

import medora.models.domain.ReportDiagnosis;
import medora.models.domain.id.ReportDiagnosisId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportDiagnosisRepository extends JpaRepository<ReportDiagnosis, ReportDiagnosisId> {
    List<ReportDiagnosis> findByReportReportId(Long reportId);
}
