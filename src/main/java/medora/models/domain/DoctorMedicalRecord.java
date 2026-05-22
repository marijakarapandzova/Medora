package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.DoctorMedicalRecordId;

@Getter
@Setter
@Entity
@Table(name = "doctor_medical_records")
@IdClass(DoctorMedicalRecordId.class)
public class DoctorMedicalRecord {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctors doctor;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    public DoctorMedicalRecord() {
    }

    public DoctorMedicalRecord(Doctors doctor, MedicalRecord medicalRecord) {
        this.doctor = doctor;
        this.medicalRecord = medicalRecord;
    }
}