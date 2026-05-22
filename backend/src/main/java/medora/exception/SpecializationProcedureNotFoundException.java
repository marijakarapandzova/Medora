package medora.exception;

public class SpecializationProcedureNotFoundException extends RuntimeException {
    public SpecializationProcedureNotFoundException(Long specializationId, Long procedureId) {
        super("Specialization procedure relationship with specialization id %d and procedure id %d does not exist.".formatted(specializationId, procedureId));
    }
}

