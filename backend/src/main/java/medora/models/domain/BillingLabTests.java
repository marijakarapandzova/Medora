package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.domain.id.BillingLabTestId;

@Getter
@Setter
@Entity
@Table(name = "billing_lab_tests")
@IdClass(BillingLabTestId.class)
public class BillingLabTests {

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Billing billing;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private LabTests labTest;

    public BillingLabTests() {
    }

    public BillingLabTests(Billing billing, LabTests labTest) {
        this.billing = billing;
        this.labTest = labTest;
    }
}