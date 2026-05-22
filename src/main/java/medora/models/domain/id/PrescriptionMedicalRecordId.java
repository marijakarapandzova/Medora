package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class PrescriptionMedicalRecordId implements Serializable {

    private Long prescription;
    private Long medicalRecord;

    public PrescriptionMedicalRecordId() {
    }

    public PrescriptionMedicalRecordId(Long prescription, Long medicalRecord) {
        this.prescription = prescription;
        this.medicalRecord = medicalRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrescriptionMedicalRecordId that)) return false;

        return Objects.equals(prescription, that.prescription)
                && Objects.equals(medicalRecord, that.medicalRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prescription, medicalRecord);
    }
}