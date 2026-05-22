package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "report_diagnosis")
@IdClass(medora.models.domain.id.ReportDiagnosisId.class)
public class ReportDiagnosis {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private MedicalReport report;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    public ReportDiagnosis() {}

    public ReportDiagnosis(MedicalReport report, Diagnosis diagnosis) {
        this.report = report;
        this.diagnosis = diagnosis;
    }
}
