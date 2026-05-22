package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisDTO {
    private Long diagnosisId;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String name;
    private String description;
}
