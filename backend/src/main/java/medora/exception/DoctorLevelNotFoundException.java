package medora.exception;

public class DoctorLevelNotFoundException extends RuntimeException {
    public DoctorLevelNotFoundException(Long id) {
        super("A doctor level with id %d does not exist.".formatted(id));
    }
}

