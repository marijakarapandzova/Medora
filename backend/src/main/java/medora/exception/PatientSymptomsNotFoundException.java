package medora.exception;

public class PatientSymptomsNotFoundException extends RuntimeException {
    public PatientSymptomsNotFoundException(Long patientId, Long symptomId) {
        super("Patient symptom relationship with patient id %d and symptom id %d does not exist.".formatted(patientId, symptomId));
    }
}

