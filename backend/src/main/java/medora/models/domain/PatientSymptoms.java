package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.PatientSymptomsId;

@Getter
@Setter
@Entity
@Table(name = "patient_symptoms")
@IdClass(PatientSymptomsId.class)
public class PatientSymptoms {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "symptom_id", nullable = false)
    private Symptoms symptom;

    public PatientSymptoms() {}

    public PatientSymptoms(Patient patient, Symptoms symptom) {
        this.patient = patient;
        this.symptom = symptom;
    }
}