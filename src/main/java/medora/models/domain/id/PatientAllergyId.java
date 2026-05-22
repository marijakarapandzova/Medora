package medora.models.domain.id;


import java.io.Serializable;
import java.util.Objects;

public class PatientAllergyId implements Serializable {

    private Long patient;
    private Long allergy;

    public PatientAllergyId() {}

    public PatientAllergyId(Long patient, Long allergy) {
        this.patient = patient;
        this.allergy = allergy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientAllergyId that)) return false;
        return Objects.equals(patient, that.patient)
                && Objects.equals(allergy, that.allergy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patient, allergy);
    }
}