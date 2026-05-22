package medora.models.domain.id;



import java.io.Serializable;
import java.util.Objects;

public class SpecializationProcedureId implements Serializable {

    private Long specialization;
    private Long procedure;

    public SpecializationProcedureId() {
    }

    public SpecializationProcedureId(Long specialization, Long procedure) {
        this.specialization = specialization;
        this.procedure = procedure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecializationProcedureId that)) return false;

        return Objects.equals(specialization, that.specialization)
                && Objects.equals(procedure, that.procedure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specialization, procedure);
    }
}