package medora.models.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "referrals")
public class Referrals {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "referral_seq")
    @SequenceGenerator(name = "referral_seq", sequenceName = "referral_id_seq", allocationSize = 1)
    @Column(name = "referral_id")
    private Long referralId;

    @NotBlank
    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "referral_date", nullable = false)
    private LocalDate referralDate;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "appointment_time")
    private LocalTime appointmentTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_doctor_id", nullable = false)
    private Doctors fromDoctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_doctor_id", nullable = false)
    private Doctors toDoctor;

    public Referrals() {}

    public Referrals(Long referralId,
                     String reason,
                     LocalDate referralDate,
                     LocalDate appointmentDate,
                     LocalTime appointmentTime,
                     MedicalRecord medicalRecord,
                     Doctors fromDoctor,
                     Doctors toDoctor) {

        this.referralId = referralId;
        this.reason = reason;
        this.referralDate = referralDate;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.medicalRecord = medicalRecord;
        this.fromDoctor = fromDoctor;
        this.toDoctor = toDoctor;
    }
}