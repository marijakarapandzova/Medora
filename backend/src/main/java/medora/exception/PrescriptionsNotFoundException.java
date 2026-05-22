package medora.exception;

public class PrescriptionsNotFoundException extends RuntimeException {
    public PrescriptionsNotFoundException(Long id) {
        super("A prescription with id %d does not exist.".formatted(id));
    }
}

