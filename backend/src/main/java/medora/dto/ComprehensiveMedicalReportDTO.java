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
public class ComprehensiveMedicalReportDTO {
    private Long reportId;
    private Long medicalRecordId;
    private Long patientId;
    private String patientName;
    private String patientEmbg;
    private LocalDate reportDate;
    private Long doctorId;
    private String doctorName;
    private String reportDescription;
    private List<DiagnosisDTO> diagnoses;
    private List<PrescriptionDTO> prescriptions;
    private List<AllergyDTO> allergies;
    private List<SymptomDTO> symptoms;
}
