package medora.exception;

public class DiagnosisSymptomsNotFoundException extends RuntimeException {
    public DiagnosisSymptomsNotFoundException(Long diagnosisId, Long symptomId) {
        super("Diagnosis symptom relationship with diagnosis id %d and symptom id %d does not exist.".formatted(diagnosisId, symptomId));
    }
}

