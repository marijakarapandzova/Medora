package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import medora.models.enums.PaymentStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBillingRequest {
    private PaymentStatus paymentStatus;
    private LocalDate paymentDate;
}
