package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import medora.models.enums.BloodType;
import medora.models.enums.Gender;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private Long patientId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private LocalDate dateOfBirth;
    private BloodType bloodType;
    private Gender gender;
    private String phoneNumber;
    private String embg;
}
