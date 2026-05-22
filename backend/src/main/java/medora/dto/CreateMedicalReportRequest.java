package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicalReportRequest {
    private Long doctorId;
    private Long medicalRecordId;
    private String description;
    private LocalDate reportDate;
    private List<Long> selectedDiagnosisIds;
    private List<Long> selectedPrescriptionIds;
    private List<Long> selectedAllergyIds;
    private List<Long> selectedSymptomIds;
}
