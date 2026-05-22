package medora.models.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.PatientAllergyId;

@Getter
@Setter
@Entity
@Table(name = "patient_allergies")
@IdClass(PatientAllergyId.class)
public class PatientAllergy {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergies allergy;

    public PatientAllergy() {}

    public PatientAllergy(Patient patient, Allergies allergy) {
        this.patient = patient;
        this.allergy = allergy;
    }
}