package medora.models.domain;

import jakarta.persistence.*;
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

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public LabTechnician() {}

    public LabTechnician(Long technicianId, String username, String name, String lastname, String email, User user) {
        this.technicianId = technicianId;
        this.username = username;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.user = user;
    }
}
