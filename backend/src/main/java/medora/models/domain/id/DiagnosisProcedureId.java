package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class DiagnosisProcedureId implements Serializable {

    private Long diagnosis;
    private Long procedure;

    public DiagnosisProcedureId() {
    }

    public DiagnosisProcedureId(Long diagnosis, Long procedure) {
        this.diagnosis = diagnosis;
        this.procedure = procedure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiagnosisProcedureId that)) return false;

        return Objects.equals(diagnosis, that.diagnosis)
                && Objects.equals(procedure, that.procedure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diagnosis, procedure);
    }
}