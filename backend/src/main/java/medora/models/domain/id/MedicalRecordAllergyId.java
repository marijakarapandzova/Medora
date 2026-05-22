package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class MedicalRecordAllergyId implements Serializable {

    private Long medicalRecord;
    private Long allergy;

    public MedicalRecordAllergyId() {
    }

    public MedicalRecordAllergyId(Long medicalRecord, Long allergy) {
        this.medicalRecord = medicalRecord;
        this.allergy = allergy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecordAllergyId that)) return false;

        return Objects.equals(medicalRecord, that.medicalRecord)
                && Objects.equals(allergy, that.allergy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicalRecord, allergy);
    }
}