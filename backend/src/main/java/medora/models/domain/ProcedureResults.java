package medora.models.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "procedure_results")
public class ProcedureResults {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "result_seq")
    @SequenceGenerator(name = "result_seq", sequenceName = "procedure_results_result_id_seq", allocationSize = 1)
    @Column(name = "result_id")
    private Long resultId;

    @Column(name = "result_description")
    private String resultDescription;

    @Column(name = "result_date", nullable = false)
    private LocalDate resultDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    public ProcedureResults() {}

    public ProcedureResults(Long resultId,
                           String resultDescription,
                           LocalDate resultDate,
                           Procedure procedure) {

        this.resultId = resultId;
        this.resultDescription = resultDescription;
        this.resultDate = resultDate;
        this.procedure = procedure;
    }
}