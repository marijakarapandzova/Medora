package medora.models.domain.id;

import java.io.Serializable;
import java.util.Objects;

public class BillingLabTestId implements Serializable {

    private Long billing;
    private Long labTest;

    public BillingLabTestId() {
    }

    public BillingLabTestId(Long billing, Long labTest) {
        this.billing = billing;
        this.labTest = labTest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BillingLabTestId that)) return false;

        return Objects.equals(billing, that.billing)
                && Objects.equals(labTest, that.labTest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billing, labTest);
    }
}