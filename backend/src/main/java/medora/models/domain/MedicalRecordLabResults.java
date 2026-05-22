package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.MedicalRecordLabResultId;

@Getter
@Setter
@Entity
@Table(name = "medical_record_lab_results")
@IdClass(MedicalRecordLabResultId.class)
public class MedicalRecordLabResults {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private LabResults labResult;

    public MedicalRecordLabResults() {
    }

    public MedicalRecordLabResults(MedicalRecord medicalRecord, LabResults labResult) {
        this.medicalRecord = medicalRecord;
        this.labResult = labResult;
    }
}