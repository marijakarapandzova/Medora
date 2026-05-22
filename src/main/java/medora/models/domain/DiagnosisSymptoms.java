package medora.models.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.DiagnosisSymptomId;

@Getter
@Setter
@Entity
@Table(name = "diagnosis_symptoms")
@IdClass(DiagnosisSymptomId.class)
public class DiagnosisSymptoms {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "symptom_id", nullable = false)
    private Symptoms symptom;

    public DiagnosisSymptoms() {
    }

    public DiagnosisSymptoms(Diagnosis diagnosis, Symptoms symptom) {
        this.diagnosis = diagnosis;
        this.symptom = symptom;
    }
}