package medora.exception;

public class BillingLabTestsNotFoundException extends RuntimeException {
    public BillingLabTestsNotFoundException(Long billingId, Long testId) {
        super("Billing lab test relationship with billing id %d and test id %d does not exist.".formatted(billingId, testId));
    }
}

