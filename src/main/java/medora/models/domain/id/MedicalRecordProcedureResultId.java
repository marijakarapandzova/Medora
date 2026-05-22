package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class MedicalRecordProcedureResultId implements Serializable {

    private Long medicalRecord;
    private Long procedureResult;

    public MedicalRecordProcedureResultId() {
    }

    public MedicalRecordProcedureResultId(Long medicalRecord, Long procedureResult) {
        this.medicalRecord = medicalRecord;
        this.procedureResult = procedureResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecordProcedureResultId that)) return false;

        return Objects.equals(medicalRecord, that.medicalRecord)
                && Objects.equals(procedureResult, that.procedureResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalRecord, procedureResult);
    }
}
