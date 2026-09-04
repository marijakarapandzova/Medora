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

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "certification")
    private String certification;

    public LabTechnician() {}

    public LabTechnician(Long technicianId, User user, String certification) {
        this.technicianId = technicianId;
        this.user = user;
        this.certification = certification;
    }

    public LabTechnician(Long technicianId, User user) {
        this.technicianId = technicianId;
        this.user = user;
    }
}
