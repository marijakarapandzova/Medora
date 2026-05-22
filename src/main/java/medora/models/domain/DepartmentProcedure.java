package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.DepartmentProcedureId;

@Getter
@Setter
@Entity
@Table(name = "department_procedures")
@IdClass(DepartmentProcedureId.class)
public class DepartmentProcedure {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    public DepartmentProcedure() {
    }

    public DepartmentProcedure(Departments department, Procedure procedure) {
        this.department = department;
        this.procedure = procedure;
    }
}