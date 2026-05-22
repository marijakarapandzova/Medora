package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class ReportSymptomId implements Serializable {
    private Long report;
    private Long symptom;

    public ReportSymptomId() {}

    public ReportSymptomId(Long report, Long symptom) {
        this.report = report;
        this.symptom = symptom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportSymptomId that = (ReportSymptomId) o;
        return Objects.equals(report, that.report) && Objects.equals(symptom, that.symptom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(report, symptom);
    }
}
