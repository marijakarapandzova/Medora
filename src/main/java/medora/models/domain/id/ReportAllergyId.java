package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class ReportAllergyId implements Serializable {
    private Long report;
    private Long allergy;

    public ReportAllergyId() {}

    public ReportAllergyId(Long report, Long allergy) {
        this.report = report;
        this.allergy = allergy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportAllergyId that = (ReportAllergyId) o;
        return Objects.equals(report, that.report) && Objects.equals(allergy, that.allergy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(report, allergy);
    }
}
