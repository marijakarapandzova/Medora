package medora.models.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.SpecializationProcedureId;

@Getter
@Setter
@Entity
@Table(name = "specialization_procedures")
@IdClass(SpecializationProcedureId.class)
public class SpecializationProcedure {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private DoctorSpecialization specialization;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    public SpecializationProcedure() {
    }

    public SpecializationProcedure(DoctorSpecialization specialization, Procedure procedure) {
        this.specialization = specialization;
        this.procedure = procedure;
    }
}