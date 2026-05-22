package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "report_symptom")
@IdClass(medora.models.domain.id.ReportSymptomId.class)
public class ReportSymptom {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private MedicalReport report;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "symptom_id", nullable = false)
    private Symptoms symptom;

    public ReportSymptom() {}

    public ReportSymptom(MedicalReport report, Symptoms symptom) {
        this.report = report;
        this.symptom = symptom;
    }
}
