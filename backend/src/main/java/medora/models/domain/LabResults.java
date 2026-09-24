package medora.models.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "lab_results")
public class LabResults {

    @Id
    @Column(name = "result_id")
    private Long resultId;

    @NotBlank
    @Column(name = "results", nullable = false, columnDefinition = "TEXT")
    private String results;

    @Column(name = "result_date", nullable = false)
    private LocalDate resultDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private LabTests labTest;

    public LabResults() {}

    public LabResults(Long resultId,
                     String results,
                     LocalDate resultDate,
                     LabTests labTest) {

        this.resultId = resultId;
        this.results = results;
        this.resultDate = resultDate;
        this.labTest = labTest;
    }
}