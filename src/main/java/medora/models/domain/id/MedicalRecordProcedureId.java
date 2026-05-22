package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class MedicalRecordProcedureId implements Serializable {

    private Long medicalRecord;
    private Long procedure;

    public MedicalRecordProcedureId() {
    }

    public MedicalRecordProcedureId(Long medicalRecord, Long procedure) {
        this.medicalRecord = medicalRecord;
        this.procedure = procedure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecordProcedureId that)) return false;

        return Objects.equals(medicalRecord, that.medicalRecord)
                && Objects.equals(procedure, that.procedure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalRecord, procedure);
    }
}