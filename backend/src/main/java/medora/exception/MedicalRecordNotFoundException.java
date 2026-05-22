package medora.exception;

public class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(Long id) {
        super("A medical record with id %d does not exist.".formatted(id));
    }
}

