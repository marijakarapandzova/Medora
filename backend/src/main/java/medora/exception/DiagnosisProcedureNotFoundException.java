package medora.exception;

public class DiagnosisProcedureNotFoundException extends RuntimeException {
    public DiagnosisProcedureNotFoundException(Long diagnosisId, Long procedureId) {
        super("Diagnosis procedure relationship with diagnosis id %d and procedure id %d does not exist.".formatted(diagnosisId, procedureId));
    }
}

