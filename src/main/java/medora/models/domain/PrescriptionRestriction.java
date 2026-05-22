package medora.models.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "prescription_restriction")
public class PrescriptionRestriction {

    @Id
    @Column(name = "restriction_id")
    private Long restrictionId;

    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescriptions prescription;

    public PrescriptionRestriction() {}

    public PrescriptionRestriction(Long restrictionId,
                                   String description,
                                   Prescriptions prescription) {
        this.restrictionId = restrictionId;
        this.description = description;
        this.prescription = prescription;
    }
}