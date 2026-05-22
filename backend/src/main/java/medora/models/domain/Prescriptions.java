package medora.models.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "prescriptions")
public class Prescriptions {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prescription_seq")
    @SequenceGenerator(name = "prescription_seq", sequenceName = "prescription_id_seq", allocationSize = 1)
    @Column(name = "prescription_id")
    private Long prescriptionId;

    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    public Prescriptions() {}

    public Prescriptions(Long prescriptionId, String medicationName) {
        this.prescriptionId = prescriptionId;
        this.medicationName = medicationName;
    }
}