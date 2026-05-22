package medora.exception;

public class PatientAllergyNotFoundException extends RuntimeException {
    public PatientAllergyNotFoundException(Long patientId, Long allergyId) {
        super("Patient allergy relationship with patient id %d and allergy id %d does not exist.".formatted(patientId, allergyId));
    }
}

