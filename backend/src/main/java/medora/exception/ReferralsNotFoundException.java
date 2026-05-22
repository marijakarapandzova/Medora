package medora.exception;

public class ReferralsNotFoundException extends RuntimeException {
    public ReferralsNotFoundException(Long id) {
        super("A referral with id %d does not exist.".formatted(id));
    }
}

