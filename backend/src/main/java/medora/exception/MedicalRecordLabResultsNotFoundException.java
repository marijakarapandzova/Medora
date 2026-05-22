package medora.exception;

public class MedicalRecordLabResultsNotFoundException extends RuntimeException {
    public MedicalRecordLabResultsNotFoundException(Long recordId, Long resultId) {
        super("Medical record lab result relationship with record id %d and result id %d does not exist.".formatted(recordId, resultId));
    }
}

