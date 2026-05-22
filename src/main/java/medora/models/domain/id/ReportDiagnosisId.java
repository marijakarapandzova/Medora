package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class ReportDiagnosisId implements Serializable {
    private Long report;
    private Long diagnosis;

    public ReportDiagnosisId() {}

    public ReportDiagnosisId(Long report, Long diagnosis) {
        this.report = report;
        this.diagnosis = diagnosis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportDiagnosisId that = (ReportDiagnosisId) o;
        return Objects.equals(report, that.report) && Objects.equals(diagnosis, that.diagnosis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(report, diagnosis);
    }
}
