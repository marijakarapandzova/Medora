package medora.models.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.AllergyPrescriptionRestrictionId;

@Getter
@Setter
@Entity
@Table(name = "allergy_prescription_restrictions")
@IdClass(AllergyPrescriptionRestrictionId.class)
public class AllergyPrescriptionRestriction {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergies allergy;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "restriction_id", nullable = false)
    private PrescriptionRestriction restriction;

    public AllergyPrescriptionRestriction() {}

    public AllergyPrescriptionRestriction(Allergies allergy,
                                          PrescriptionRestriction restriction) {
        this.allergy = allergy;
        this.restriction = restriction;
    }
}