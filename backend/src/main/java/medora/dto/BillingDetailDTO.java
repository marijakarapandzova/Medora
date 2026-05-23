package medora.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BillingDetailDTO {
    private Long billId;
    private Long patientId;
    private String patientName;
    private String patientEmbg;
    private String patientPhone;
    private BigDecimal totalCost;
    private String paymentStatus;
    private LocalDate paymentDate;
    private LocalDate billDate;
    private List<BillingItemDTO> procedures;
    private List<BillingItemDTO> labTests;

    public BillingDetailDTO() {}

    public BillingDetailDTO(Long billId, Long patientId, String patientName, String patientEmbg, String patientPhone,
                            BigDecimal totalCost, String paymentStatus, LocalDate paymentDate,
                            LocalDate billDate, List<BillingItemDTO> procedures, List<BillingItemDTO> labTests) {
        this.billId = billId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmbg = patientEmbg;
        this.patientPhone = patientPhone;
        this.totalCost = totalCost;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
        this.billDate = billDate;
        this.procedures = procedures;
        this.labTests = labTests;
    }

    // Getters and Setters
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientEmbg() { return patientEmbg; }
    public void setPatientEmbg(String patientEmbg) { this.patientEmbg = patientEmbg; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }

    public List<BillingItemDTO> getProcedures() { return procedures; }
    public void setProcedures(List<BillingItemDTO> procedures) { this.procedures = procedures; }

    public List<BillingItemDTO> getLabTests() { return labTests; }
    public void setLabTests(List<BillingItemDTO> labTests) { this.labTests = labTests; }
}