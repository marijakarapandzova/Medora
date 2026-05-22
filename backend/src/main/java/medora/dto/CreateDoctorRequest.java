package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDoctorRequest {
    private String firstName;
    private String lastName;
    private String emailAddress;
    private Long levelId;
    private Long specializationId;
    private Long departmentId;
}
