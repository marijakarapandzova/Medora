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
public class RequestLabTestRequest {
    private Long medicalRecordId;
    private Long patientId;
    private Long doctorId;
    private Long testId;
    private LocalDate testDate;
    private String notes;
}
