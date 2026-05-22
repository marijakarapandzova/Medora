package medora.exception;

public class LabTechnicianNotFoundException extends RuntimeException {
    public LabTechnicianNotFoundException(Long id) {
        super("A lab technician with id %d does not exist.".formatted(id));
    }
}

