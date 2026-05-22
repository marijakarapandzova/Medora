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
public class LabTestRequestDTO {
    private Long testId;
    private String testName;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private LocalDate requestDate;
    private String notes;
}
