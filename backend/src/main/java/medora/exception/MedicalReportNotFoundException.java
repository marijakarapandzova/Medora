package medora.exception;

public class MedicalReportNotFoundException extends RuntimeException {
    public MedicalReportNotFoundException(Long id) {
        super("A medical report with id %d does not exist.".formatted(id));
    }
}

