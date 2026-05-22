package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabResultDTO {
    private Long resultId;
    private Long testId;
    private String testName;
    private Long medicalRecordId;
    private String results;
    private LocalDate resultDate;
}
