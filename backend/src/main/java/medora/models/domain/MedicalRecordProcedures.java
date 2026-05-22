package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.MedicalRecordProcedureId;

@Getter
@Setter
@Entity
@Table(name = "medical_record_procedures")
@IdClass(MedicalRecordProcedureId.class)
public class MedicalRecordProcedures {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    public MedicalRecordProcedures() {
    }

    public MedicalRecordProcedures(MedicalRecord medicalRecord, Procedure procedure) {
        this.medicalRecord = medicalRecord;
        this.procedure = procedure;
    }
}