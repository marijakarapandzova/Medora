package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.MedicalRecordProcedureResultId;

@Getter
@Setter
@Entity
@Table(name = "medical_record_procedure_results")
@IdClass(MedicalRecordProcedureResultId.class)
public class MedicalRecordProcedureResults {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private ProcedureResults procedureResult;

    public MedicalRecordProcedureResults() {
    }

    public MedicalRecordProcedureResults(MedicalRecord medicalRecord, ProcedureResults procedureResult) {
        this.medicalRecord = medicalRecord;
        this.procedureResult = procedureResult;
    }
}
