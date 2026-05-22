package medora.exception;

public class MedicalRecordAllergiesNotFoundException extends RuntimeException {
    public MedicalRecordAllergiesNotFoundException(Long recordId, Long allergyId) {
        super("Medical record allergy relationship with record id %d and allergy id %d does not exist.".formatted(recordId, allergyId));
    }
}

