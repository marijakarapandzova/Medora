package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "performed_procedures")
public class PerformedProcedures {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "performed_id_seq")
    @SequenceGenerator(name = "performed_id_seq", sequenceName = "performed_procedures_performed_id_seq", allocationSize = 1)
    @Column(name = "performed_id")
    private Long performedId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctors doctor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id")
    private Diagnosis diagnosis;

    @Column(name = "procedure_date", nullable = false)
    private LocalDate procedureDate;

    private String notes;

    public PerformedProcedures() {
    }

    public PerformedProcedures(Long performedId,
                              Procedure procedure,
                              Doctors doctor,
                              Patient patient,
                              Diagnosis diagnosis,
                              LocalDate procedureDate,
                              String notes) {
        this.performedId = performedId;
        this.procedure = procedure;
        this.doctor = doctor;
        this.patient = patient;
        this.diagnosis = diagnosis;
        this.procedureDate = procedureDate;
        this.notes = notes;
    }
}