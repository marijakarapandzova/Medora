package medora.repository;

import medora.models.domain.ReportPrescription;
import medora.models.domain.id.ReportPrescriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportPrescriptionRepository extends JpaRepository<ReportPrescription, ReportPrescriptionId> {
    List<ReportPrescription> findByReportReportId(Long reportId);
}
