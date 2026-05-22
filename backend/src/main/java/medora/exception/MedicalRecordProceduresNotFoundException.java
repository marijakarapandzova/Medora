package medora.exception;

public class MedicalRecordProceduresNotFoundException extends RuntimeException {
    public MedicalRecordProceduresNotFoundException(Long recordId, Long procedureId) {
        super("Medical record procedure relationship with record id %d and procedure id %d does not exist.".formatted(recordId, procedureId));
    }
}

