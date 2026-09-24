package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "diagnosis")
public class Diagnosis {

    @Id
    @Column(name = "diagnosis_id")
    private Long diagnosisId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctors doctor;

    public Diagnosis() {}

    public Diagnosis(Long diagnosisId,
                     String name,
                     String description,
                     Patient patient,
                     Doctors doctor) {

        this.diagnosisId = diagnosisId;
        this.name = name;
        this.description = description;
        this.patient = patient;
        this.doctor = doctor;
    }
}