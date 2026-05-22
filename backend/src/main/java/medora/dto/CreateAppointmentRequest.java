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
public class CreateAppointmentRequest {
    private Long patientId;
    private Long doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
}
