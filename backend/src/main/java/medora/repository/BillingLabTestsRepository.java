package medora.repository;

import medora.models.domain.BillingLabTests;
import medora.models.domain.id.BillingLabTestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BillingLabTestsRepository extends JpaRepository<BillingLabTests, BillingLabTestId> {

    // Find all lab tests for a billing record
    @Query("""
        SELECT blt FROM BillingLabTests blt
        WHERE blt.billing.billId = :billId
    """)
    List<BillingLabTests> findByBillingBillId(@Param("billId") Long billId);

    // Calculate total cost of lab tests for a billing record
    @Query("""
        SELECT COALESCE(SUM(blt.labTest.cost), 0) FROM BillingLabTests blt
        WHERE blt.billing.billId = :billId
    """)
    BigDecimal calculateTotalCostForBilling(@Param("billId") Long billId);
}

