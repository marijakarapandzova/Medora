package medora.exception;

public class ProcedureResultsNotFoundException extends RuntimeException {
    public ProcedureResultsNotFoundException(Long id) {
        super("Procedure results with id %d does not exist.".formatted(id));
    }
}

