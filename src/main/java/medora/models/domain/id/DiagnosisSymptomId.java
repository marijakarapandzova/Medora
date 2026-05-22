package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class DiagnosisSymptomId implements Serializable {

    private Long diagnosis;
    private Long symptom;

    public DiagnosisSymptomId() {
    }

    public DiagnosisSymptomId(Long diagnosis, Long symptom) {
        this.diagnosis = diagnosis;
        this.symptom = symptom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiagnosisSymptomId that)) return false;
        return Objects.equals(diagnosis, that.diagnosis)
                && Objects.equals(symptom, that.symptom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnosis, symptom);
    }
}