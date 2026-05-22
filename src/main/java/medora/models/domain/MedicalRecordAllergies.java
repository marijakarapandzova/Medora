package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.MedicalRecordAllergyId;

@Getter
@Setter
@Entity
@Table(name = "medical_record_allergies")
@IdClass(MedicalRecordAllergyId.class)
public class MedicalRecordAllergies {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "allergy_id", nullable = false)
    private Allergies allergy;

    @Column(name = "reaction")
    private String reaction;

    @Column(name = "severity")
    private String severity;

    public MedicalRecordAllergies() {
    }

    public MedicalRecordAllergies(MedicalRecord medicalRecord,
                                Allergies allergy,
                                String reaction,
                                String severity) {
        this.medicalRecord = medicalRecord;
        this.allergy = allergy;
        this.reaction = reaction;
        this.severity = severity;
    }
}