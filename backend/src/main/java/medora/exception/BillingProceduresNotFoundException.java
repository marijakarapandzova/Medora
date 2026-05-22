package medora.exception;

public class BillingProceduresNotFoundException extends RuntimeException {
    public BillingProceduresNotFoundException(Long billingId, Long procedureId) {
        super("Billing procedure relationship with billing id %d and procedure id %d does not exist.".formatted(billingId, procedureId));
    }
}

