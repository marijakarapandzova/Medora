package medora.models.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "admin")
public class Admin {

    @Id
    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "lastname", nullable = false)
    private String lastname;

    @Pattern(regexp = ".*@adminmedora.*")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    public Admin() {}

    public Admin(Long adminId, String username, String name, String lastname, String email) {
        this.adminId = adminId;
        this.username = username;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
    }
}