package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReferralDTO {
    private Long referralId;
    private Long medicalRecordId;
    private Long patientId;
    private String patientName;
    private Long fromDoctorId;
    private String fromDoctorName;
    private Long toDoctorId;
    private String toDoctorName;
    private String reason;
    private LocalDate referralDate;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
}
