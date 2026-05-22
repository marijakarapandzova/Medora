package medora.models.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import medora.models.enums.BloodType;
import medora.models.enums.Gender;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "patients")

public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_seq")
    @SequenceGenerator(name = "patient_seq", sequenceName = "patient_id_seq", allocationSize = 1)
    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email_address", unique = true)
    private String emailAddress;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Convert(converter = medora.models.converter.BloodTypeConverter.class)
    @Column(name = "blood_type")
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "embg", nullable = false, unique = true)
    private String embg;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<MedicalRecord> medicalRecords;

    public Patient() {}

    public Patient(Long patientId,
                   String firstName,
                   String lastName,
                   String emailAddress,
                   LocalDate dateOfBirth,
                   BloodType bloodType,
                   Gender gender,
                   String phoneNumber,
                   String embg) {

        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.dateOfBirth = dateOfBirth;
        this.bloodType = bloodType;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.embg = embg;
    }
}