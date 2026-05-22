package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.MedicalRecordSymptomId;

@Getter
@Setter
@Entity
@Table(name = "medical_record_symptoms")
@IdClass(MedicalRecordSymptomId.class)
public class MedicalRecordSymptoms {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Symptoms symptom;

    @Column(name = "severity")
    private String severity;

    public MedicalRecordSymptoms() {
    }

    public MedicalRecordSymptoms(MedicalRecord medicalRecord, Symptoms symptom, String severity) {
        this.medicalRecord = medicalRecord;
        this.symptom = symptom;
        this.severity = severity;
    }
}