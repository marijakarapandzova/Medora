package medora.models.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "procedures")
public class Procedure {

    @Id
    @Column(name = "procedure_id")
    private Long procedureId;

    @NotBlank
    @Column(name = "procedure_type", nullable = false)
    private String procedureType;

    @Column(name = "procedure_date", nullable = false)
    private LocalDate procedureDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @DecimalMin(value = "0.0")
    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctors doctor;

    public Procedure() {}

    public Procedure(Long procedureId,
                     String procedureType,
                     LocalDate procedureDate,
                     String description,
                     BigDecimal cost,
                     Doctors doctor) {

        this.procedureId = procedureId;
        this.procedureType = procedureType;
        this.procedureDate = procedureDate;
        this.description = description;
        this.cost = cost;
        this.doctor = doctor;
    }
}