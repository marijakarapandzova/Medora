package medora.exception;

public class ProcedureNotFoundException extends RuntimeException {
    public ProcedureNotFoundException(Long id) {
        super("A procedure with id %d does not exist.".formatted(id));
    }
}

