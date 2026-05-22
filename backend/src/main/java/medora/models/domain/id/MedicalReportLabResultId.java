package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class MedicalReportLabResultId implements Serializable {

    private Long medicalReport;
    private Long labResult;

    public MedicalReportLabResultId() {
    }

    public MedicalReportLabResultId(Long medicalReport, Long labResult) {
        this.medicalReport = medicalReport;
        this.labResult = labResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalReportLabResultId that)) return false;

        return Objects.equals(medicalReport, that.medicalReport)
                && Objects.equals(labResult, that.labResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalReport, labResult);
    }
}