package medora.repository;

import medora.models.domain.BillingProcedures;
import medora.models.domain.id.BillingProcedureId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BillingProceduresRepository extends JpaRepository<BillingProcedures, BillingProcedureId> {

    // Find all procedures for a billing record
    @Query("""
        SELECT bp FROM BillingProcedures bp
        WHERE bp.billing.billId = :billId
    """)
    List<BillingProcedures> findByBillingBillId(@Param("billId") Long billId);

    // Calculate total cost of procedures for a billing record
    @Query("""
        SELECT COALESCE(SUM(bp.procedure.cost), 0) FROM BillingProcedures bp
        WHERE bp.billing.billId = :billId
    """)
    BigDecimal calculateTotalCostForBilling(@Param("billId") Long billId);
}

