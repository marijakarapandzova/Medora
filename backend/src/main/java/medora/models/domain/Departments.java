package medora.models.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "departments")
public class Departments {

    @Id
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", nullable = false, unique = true)
    private String departmentName;

    public Departments() {}

    public Departments(Long departmentId, String departmentName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }
}