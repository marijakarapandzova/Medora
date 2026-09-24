package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "doctors")
public class Doctors {

    @Id
    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;

    @ManyToOne(optional = false)
    @JoinColumn(name = "level_id", nullable = false)
    private DoctorLevel level;

    @ManyToOne(optional = false)
    @JoinColumn(name = "specialization_id", nullable = false)
    private DoctorSpecialization specialization;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Departments department;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Doctors() {}

    public Doctors(Long doctorId, String firstName, String lastName, String emailAddress,
                  DoctorLevel level, DoctorSpecialization specialization, Departments department,
                  User user) {
        this.doctorId = doctorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.level = level;
        this.specialization = specialization;
        this.department = department;
        this.user = user;
    }
}