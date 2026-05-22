package medora.exception;

public class BillingNotFoundException extends RuntimeException {
    public BillingNotFoundException(Long id) {
        super("A billing record with id %d does not exist.".formatted(id));
    }
}

