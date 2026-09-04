package medora.models.domain;

import jakarta.persistence.*;
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

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "permissions")
    private String permissions;

    public Admin() {}

    public Admin(Long adminId, User user, String permissions) {
        this.adminId = adminId;
        this.user = user;
        this.permissions = permissions;
    }

    public Admin(Long adminId, User user) {
        this.adminId = adminId;
        this.user = user;
    }
}
