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
public class CreateReferralRequest {
    private Long medicalRecordId;
    private Long fromDoctorId;
    private Long toDoctorId;
    private String reason;
    private LocalDate referralDate;
}
