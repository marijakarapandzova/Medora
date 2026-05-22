package medora.models.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "record_seq")
    @SequenceGenerator(name = "record_seq", sequenceName = "record_id_seq", allocationSize = 1)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    public MedicalRecord() {}

    public MedicalRecord(Long recordId, Patient patient) {
        this.recordId = recordId;
        this.patient = patient;
    }
}