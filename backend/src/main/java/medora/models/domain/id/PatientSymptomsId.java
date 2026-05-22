package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class PatientSymptomsId implements Serializable {

    private Long patient;
    private Long symptom;

    public PatientSymptomsId() {}

    public PatientSymptomsId(Long patient, Long symptom) {
        this.patient = patient;
        this.symptom = symptom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientSymptomsId that)) return false;
        return Objects.equals(patient, that.patient)
                && Objects.equals(symptom, that.symptom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patient, symptom);
    }
}