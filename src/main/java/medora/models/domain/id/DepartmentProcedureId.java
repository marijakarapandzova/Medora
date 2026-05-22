package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class DepartmentProcedureId implements Serializable {

    private Long department;
    private Long procedure;

    public DepartmentProcedureId() {
    }

    public DepartmentProcedureId(Long department, Long procedure) {
        this.department = department;
        this.procedure = procedure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DepartmentProcedureId that)) return false;

        return Objects.equals(department, that.department)
                && Objects.equals(procedure, that.procedure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(department, procedure);
    }
}