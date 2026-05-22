package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.BillingProcedureId;

@Getter
@Setter
@Entity
@Table(name = "billing_procedures")
@IdClass(BillingProcedureId.class)
public class BillingProcedures {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Billing billing;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", nullable = false)
    private Procedure procedure;

    public BillingProcedures() {
    }

    public BillingProcedures(Billing billing, Procedure procedure) {
        this.billing = billing;
        this.procedure = procedure;
    }
}