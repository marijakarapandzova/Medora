package medora.models.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "doctor_level")
public class DoctorLevel {

    @Id
    @Column(name = "level_id")
    private Long levelId;

    @Column(nullable = false, unique = true)
    private String level;

    public DoctorLevel() {}

    public DoctorLevel(Long levelId, String level) {
        this.levelId = levelId;
        this.level = level;
    }
}