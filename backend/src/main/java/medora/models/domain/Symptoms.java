package medora.models.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "symptoms")
public class Symptoms {

    @Id
    @Column(name = "symptom_id")
    private Long symptomId;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    public Symptoms() {}

    public Symptoms(Long symptomId, String name, String description) {
        this.symptomId = symptomId;
        this.name = name;
        this.description = description;
    }
}