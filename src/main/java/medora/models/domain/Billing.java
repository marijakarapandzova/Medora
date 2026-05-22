package medora.models.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import medora.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "billing")
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_seq")
    @SequenceGenerator(name = "bill_seq", sequenceName = "bill_id_seq", allocationSize = 1)
    @Column(name = "bill_id")
    private Long billId;

    @DecimalMin(value = "0.0")
    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @ManyToOne(optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    public Billing() {}

    public Billing(Long billId,
                   BigDecimal totalCost,
                   PaymentStatus paymentStatus,
                   LocalDate paymentDate,
                   MedicalRecord medicalRecord,
                   Admin admin) {

        this.billId = billId;
        this.totalCost = totalCost;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
        this.medicalRecord = medicalRecord;
        this.admin = admin;
    }
}