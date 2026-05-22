package medora.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllergyDTO {
    private Long allergyId;
    private String allergyName;
    private String reaction;
    private String severity;
}
