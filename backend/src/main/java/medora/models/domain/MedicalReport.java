package medora.models.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "medical_report")
public class MedicalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "medical_report_seq")
    @SequenceGenerator(name = "medical_report_seq", sequenceName = "medical_report_id_seq", initialValue = 100000, allocationSize = 1)
    @Column(name = "report_id")
    private Long reportId;

    @NotBlank
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctors doctor;

    public MedicalReport() {}

    public MedicalReport(Long reportId,
                         String description,
                         LocalDate reportDate,
                         MedicalRecord medicalRecord,
                         Doctors doctor) {

        this.reportId = reportId;
        this.description = description;
        this.reportDate = reportDate;
        this.medicalRecord = medicalRecord;
        this.doctor = doctor;
    }
}