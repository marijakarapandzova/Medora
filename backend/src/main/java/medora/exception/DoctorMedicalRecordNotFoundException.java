package medora.exception;

public class DoctorMedicalRecordNotFoundException extends RuntimeException {
    public DoctorMedicalRecordNotFoundException(Long doctorId, Long recordId) {
        super("Doctor medical record relationship with doctor id %d and record id %d does not exist.".formatted(doctorId, recordId));
    }
}

