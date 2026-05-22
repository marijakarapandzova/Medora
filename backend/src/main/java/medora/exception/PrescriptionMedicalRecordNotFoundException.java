package medora.exception;

public class PrescriptionMedicalRecordNotFoundException extends RuntimeException {
    public PrescriptionMedicalRecordNotFoundException(Long prescriptionId, Long recordId) {
        super("Prescription medical record relationship with prescription id %d and record id %d does not exist.".formatted(prescriptionId, recordId));
    }
}

