package medora.exception;

public class DiagnosisNotFoundException extends RuntimeException {
    public DiagnosisNotFoundException(Long id) {
        super("A diagnosis with id %d does not exist.".formatted(id));
    }
}

