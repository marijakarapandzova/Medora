package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "report_allergy")
@IdClass(medora.models.domain.id.ReportAllergyId.class)
public class ReportAllergy {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private MedicalReport report;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergies allergy;

    public ReportAllergy() {}

    public ReportAllergy(MedicalReport report, Allergies allergy) {
        this.report = report;
        this.allergy = allergy;
    }
}
