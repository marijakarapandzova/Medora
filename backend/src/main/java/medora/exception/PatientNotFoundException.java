package medora.exception;

public class PatientNotFoundException extends RuntimeException {
    public PatientNotFoundException(Long id) {
        super("A patient with id %d does not exist.".formatted(id));
    }
}

