package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.PrescriptionMedicalRecordId;

@Getter
@Setter
@Entity
@Table(name = "prescription_medical_records")
@IdClass(PrescriptionMedicalRecordId.class)
public class PrescriptionMedicalRecord {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescriptions prescription;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Column(nullable = false)
    private String dosage;

    @Column(nullable = false)
    private String frequency;

    @Column(nullable = false)
    private String duration;

    private String notes;

    public PrescriptionMedicalRecord() {
    }

    public PrescriptionMedicalRecord(Prescriptions prescription,
                                     MedicalRecord medicalRecord,
                                     String dosage,
                                     String frequency,
                                     String duration,
                                     String notes) {
        this.prescription = prescription;
        this.medicalRecord = medicalRecord;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.notes = notes;
    }
}