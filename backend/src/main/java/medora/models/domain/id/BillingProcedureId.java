package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class BillingProcedureId implements Serializable {

    private Long billing;
    private Long procedure;

    public BillingProcedureId() {
    }

    public BillingProcedureId(Long billing, Long procedure) {
        this.billing = billing;
        this.procedure = procedure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BillingProcedureId that)) return false;

        return Objects.equals(billing, that.billing)
                && Objects.equals(procedure, that.procedure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billing, procedure);
    }
}