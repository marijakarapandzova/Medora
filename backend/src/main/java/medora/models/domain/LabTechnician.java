package medora.models.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lab_technician")
public class LabTechnician {

    @Id
    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Pattern(regexp = ".*@labmedora.*")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    public LabTechnician() {}

    public LabTechnician(Long technicianId, String username, String name, String lastname, String email) {
        this.technicianId = technicianId;
        this.username = username;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
    }
}