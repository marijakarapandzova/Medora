package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.MedicalReportLabResultId;

@Getter
@Setter
@Entity
@Table(name = "medical_report_lab_results")
@IdClass(MedicalReportLabResultId.class)
public class MedicalReportLabResults {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private MedicalReport medicalReport;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private LabResults labResult;

    public MedicalReportLabResults() {
    }

    public MedicalReportLabResults(MedicalReport medicalReport, LabResults labResult) {
        this.medicalReport = medicalReport;
        this.labResult = labResult;
    }
}