package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.DiagnosisProcedureId;

@Getter
@Setter
@Entity
@Table(name = "diagnosis_procedures")
@IdClass(DiagnosisProcedureId.class)
public class DiagnosisProcedure {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    private Diagnosis diagnosis;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    public DiagnosisProcedure() {
    }

    public DiagnosisProcedure(Diagnosis diagnosis, Procedure procedure) {
        this.diagnosis = diagnosis;
        this.procedure = procedure;
    }
}