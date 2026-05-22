package medora.exception;

public class PrescriptionRestrictionNotFoundException extends RuntimeException {
    public PrescriptionRestrictionNotFoundException(Long id) {
        super("A prescription restriction with id %d does not exist.".formatted(id));
    }
}

