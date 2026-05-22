package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class MedicalRecordLabResultId implements Serializable {

    private Long medicalRecord;
    private Long labResult;

    public MedicalRecordLabResultId() {
    }

    public MedicalRecordLabResultId(Long medicalRecord, Long labResult) {
        this.medicalRecord = medicalRecord;
        this.labResult = labResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecordLabResultId that)) return false;

        return Objects.equals(medicalRecord, that.medicalRecord)
                && Objects.equals(labResult, that.labResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalRecord, labResult);
    }
}