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
public class SubmitLabResultRequest {
    private Long medicalRecordId;
    private Long testId;
    private String results;
    private LocalDate resultDate;
}
