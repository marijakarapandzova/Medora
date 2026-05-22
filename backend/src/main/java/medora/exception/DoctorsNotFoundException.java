package medora.exception;

public class DoctorsNotFoundException extends RuntimeException {
    public DoctorsNotFoundException(Long id) {
        super("A doctor with id %d does not exist.".formatted(id));
    }
}

