package medora.exception;

public class SymptomsNotFoundException extends RuntimeException {
    public SymptomsNotFoundException(Long id) {
        super("A symptom with id %d does not exist.".formatted(id));
    }
}

