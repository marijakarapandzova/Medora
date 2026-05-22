package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import medora.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillingDTO {
    private Long billId;
    private Long medicalRecordId;
    private String patientName;
    private BigDecimal totalCost;
    private PaymentStatus paymentStatus;
    private LocalDate paymentDate;
}
