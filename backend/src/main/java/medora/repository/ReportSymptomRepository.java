package medora.repository;

import medora.models.domain.ReportSymptom;
import medora.models.domain.id.ReportSymptomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportSymptomRepository extends JpaRepository<ReportSymptom, ReportSymptomId> {
    List<ReportSymptom> findByReportReportId(Long reportId);
}
