package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "report_prescription")
@IdClass(medora.models.domain.id.ReportPrescriptionId.class)
public class ReportPrescription {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private MedicalReport report;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescriptions prescription;

    public ReportPrescription() {}

    public ReportPrescription(MedicalReport report, Prescriptions prescription) {
        this.report = report;
        this.prescription = prescription;
    }
}
