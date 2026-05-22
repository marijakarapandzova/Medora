package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class DoctorMedicalRecordId implements Serializable {

    private Long doctor;
    private Long medicalRecord;

    public DoctorMedicalRecordId() {
    }

    public DoctorMedicalRecordId(Long doctor, Long medicalRecord) {
        this.doctor = doctor;
        this.medicalRecord = medicalRecord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorMedicalRecordId that)) return false;

        return Objects.equals(doctor, that.doctor)
                && Objects.equals(medicalRecord, that.medicalRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doctor, medicalRecord);
    }
}