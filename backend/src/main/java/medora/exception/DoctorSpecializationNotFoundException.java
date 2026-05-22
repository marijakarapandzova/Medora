package medora.exception;

public class DoctorSpecializationNotFoundException extends RuntimeException {
    public DoctorSpecializationNotFoundException(Long id) {
        super("A doctor specialization with id %d does not exist.".formatted(id));
    }
}

