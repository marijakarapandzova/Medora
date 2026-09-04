package medora.models.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "doctor_specialization")
public class DoctorSpecialization {

    @Id
    @Column(name = "specialization_id")
    private Long specializationId;

    @Column(name = "specialization_name", nullable = false, unique = true)
    private String specializationName;

    public DoctorSpecialization() {}

    public DoctorSpecialization(Long specializationId, String specializationName) {
        this.specializationId = specializationId;
        this.specializationName = specializationName;
    }
}