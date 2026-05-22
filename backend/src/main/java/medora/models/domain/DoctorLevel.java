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