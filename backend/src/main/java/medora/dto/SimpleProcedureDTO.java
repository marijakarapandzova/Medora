package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleProcedureDTO {
    private Long procedureId;
    private String procedureType;
    private String description;
    private BigDecimal cost;
}
