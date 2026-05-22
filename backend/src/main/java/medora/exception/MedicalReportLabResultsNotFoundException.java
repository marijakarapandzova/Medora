package medora.exception;

public class MedicalReportLabResultsNotFoundException extends RuntimeException {
    public MedicalReportLabResultsNotFoundException(Long reportId, Long resultId) {
        super("Medical report lab result relationship with report id %d and result id %d does not exist.".formatted(reportId, resultId));
    }
}

