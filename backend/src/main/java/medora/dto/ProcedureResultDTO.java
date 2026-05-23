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
public class ProcedureResultDTO {
    private Long resultId;
    private Long procedureId;
    private String procedureType;
    private Long medicalRecordId;
    private String resultDescription;
    private LocalDate resultDate;
}
