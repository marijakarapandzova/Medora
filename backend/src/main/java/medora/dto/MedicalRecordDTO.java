package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDTO {
    private Long recordId;
    private Long patientId;
    private String patientName;
    private String embg;
}
