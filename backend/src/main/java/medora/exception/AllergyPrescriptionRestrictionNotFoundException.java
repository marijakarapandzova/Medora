package medora.exception;

public class AllergyPrescriptionRestrictionNotFoundException extends RuntimeException {
    public AllergyPrescriptionRestrictionNotFoundException(Long allergyId, Long restrictionId) {
        super("Allergy prescription restriction relationship with allergy id %d and restriction id %d does not exist.".formatted(allergyId, restrictionId));
    }
}

