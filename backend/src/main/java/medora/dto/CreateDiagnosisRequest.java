package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDiagnosisRequest {
    private Long patientId;
    private Long doctorId;
    private String name;
    private String description;
}
