package medora.exception;

public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(Long id) {
        super("An appointment with id %d does not exist.".formatted(id));
    }
}

