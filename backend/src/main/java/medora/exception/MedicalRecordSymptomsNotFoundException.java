package medora.exception;

public class MedicalRecordSymptomsNotFoundException extends RuntimeException {
    public MedicalRecordSymptomsNotFoundException(Long recordId, Long symptomId) {
        super("Medical record symptom relationship with record id %d and symptom id %d does not exist.".formatted(recordId, symptomId));
    }
}

