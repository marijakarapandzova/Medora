package medora.exception;

public class LabTestsNotFoundException extends RuntimeException {
    public LabTestsNotFoundException(Long id) {
        super("A lab test with id %d does not exist.".formatted(id));
    }
}

