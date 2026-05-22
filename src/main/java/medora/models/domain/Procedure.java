package medora.models.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "procedures")
public class Procedure {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "procedure_seq")
    @SequenceGenerator(name = "procedure_seq", sequenceName = "procedure_id_seq", allocationSize = 1)
    @Column(name = "procedure_id")
    private Long procedureId;

    @NotBlank
    @Column(name = "procedure_type", nullable = false)
    private String procedureType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.0")
    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    public Procedure() {}

    public Procedure(Long procedureId,
                     String procedureType,
                     String description,
                     BigDecimal cost) {

        this.procedureId = procedureId;
        this.procedureType = procedureType;
        this.description = description;
        this.cost = cost;
    }
}