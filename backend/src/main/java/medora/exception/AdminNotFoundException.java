package medora.exception;

public class AdminNotFoundException extends RuntimeException {
    public AdminNotFoundException(Long id) {
        super("An admin with id %d does not exist.".formatted(id));
    }
}

