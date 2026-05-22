package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {
    private Long doctorId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private DoctorLevelDTO level;
    private DoctorSpecializationDTO specialization;
    private DepartmentDTO department;
}
