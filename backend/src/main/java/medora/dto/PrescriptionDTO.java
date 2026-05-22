package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {
    private Long prescriptionId;
    private Long medicalRecordId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String duration;
    private String notes;
}
