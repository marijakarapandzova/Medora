package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class ReportPrescriptionId implements Serializable {
    private Long report;
    private Long prescription;

    public ReportPrescriptionId() {}

    public ReportPrescriptionId(Long report, Long prescription) {
        this.report = report;
        this.prescription = prescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportPrescriptionId that = (ReportPrescriptionId) o;
        return Objects.equals(report, that.report) && Objects.equals(prescription, that.prescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(report, prescription);
    }
}
