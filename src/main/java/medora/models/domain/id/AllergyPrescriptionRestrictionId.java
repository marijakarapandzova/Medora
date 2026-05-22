package medora.models.domain.id;


import java.io.Serializable;
import java.util.Objects;

public class AllergyPrescriptionRestrictionId implements Serializable {

    private Long allergy;
    private Long restriction;

    public AllergyPrescriptionRestrictionId() {}

    public AllergyPrescriptionRestrictionId(Long allergy, Long restriction) {
        this.allergy = allergy;
        this.restriction = restriction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllergyPrescriptionRestrictionId that)) return false;
        return Objects.equals(allergy, that.allergy)
                && Objects.equals(restriction, that.restriction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allergy, restriction);
    }
}