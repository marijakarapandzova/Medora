package medora.exception;

public class LabResultsNotFoundException extends RuntimeException {
    public LabResultsNotFoundException(Long id) {
        super("Lab results with id %d does not exist.".formatted(id));
    }
}

