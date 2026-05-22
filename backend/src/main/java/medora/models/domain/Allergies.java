package medora.models.domain;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import medora.models.enums.AllergySeverity;

@Getter
@Setter
@Entity
@Table(name = "allergies")
public class Allergies {

    @Id
    @Column(name = "allergy_id")
    private Long allergyId;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "allergy_severity", nullable = false)
    private AllergySeverity allergySeverity;

    public Allergies() {}

    public Allergies(Long allergyId, String name, AllergySeverity allergySeverity) {
        this.allergyId = allergyId;
        this.name = name;
        this.allergySeverity = allergySeverity;
    }
}