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
@Table(name = "lab_tests")
public class LabTests {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "test_seq")
    @SequenceGenerator(name = "test_seq", sequenceName = "test_id_seq", allocationSize = 1)
    @Column(name = "test_id")
    private Long testId;

    @NotBlank
    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "description")
    private String description;

    @DecimalMin(value = "0.0")
    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    public LabTests() {}

    public LabTests(Long testId,
                   String testName,
                   String description,
                   BigDecimal cost) {

        this.testId = testId;
        this.testName = testName;
        this.description = description;
        this.cost = cost;
    }
}