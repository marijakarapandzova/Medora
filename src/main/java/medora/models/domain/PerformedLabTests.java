package medora.models.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "performed_lab_tests")
public class PerformedLabTests {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "performed_test_seq")
    @SequenceGenerator(name = "performed_test_seq", sequenceName = "performed_test_id_seq", allocationSize = 1)
    @Column(name = "performed_test_id")
    private Long performedTestId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private LabTests labTest;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctors doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private LabTechnician technician;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    private String notes;

    public PerformedLabTests() {
    }

    public PerformedLabTests(Long performedTestId,
                            LabTests labTest,
                            Patient patient,
                            Doctors doctor,
                            LabTechnician technician,
                            LocalDate testDate,
                            String notes) {
        this.performedTestId = performedTestId;
        this.labTest = labTest;
        this.patient = patient;
        this.doctor = doctor;
        this.technician = technician;
        this.testDate = testDate;
        this.notes = notes;
    }
}