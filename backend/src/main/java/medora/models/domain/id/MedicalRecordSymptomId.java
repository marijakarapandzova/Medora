package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class MedicalRecordSymptomId implements Serializable {

    private Long medicalRecord;
    private Long symptom;

    public MedicalRecordSymptomId() {
    }

    public MedicalRecordSymptomId(Long medicalRecord, Long symptom) {
        this.medicalRecord = medicalRecord;
        this.symptom = symptom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecordSymptomId that)) return false;

        return Objects.equals(medicalRecord, that.medicalRecord)
                && Objects.equals(symptom, that.symptom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalRecord, symptom);
    }
}