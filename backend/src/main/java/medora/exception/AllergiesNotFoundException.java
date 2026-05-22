package medora.exception;

public class AllergiesNotFoundException extends RuntimeException {
    public AllergiesNotFoundException(Long id) {
        super("An allergy with id %d does not exist.".formatted(id));
    }
}

