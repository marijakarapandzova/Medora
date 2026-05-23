package medora.dto;

import java.math.BigDecimal;

public class BillingItemDTO {
    private Long itemId;
    private String description;
    private BigDecimal cost;

    public BillingItemDTO() {}

    public BillingItemDTO(Long itemId, String description, BigDecimal cost) {
        this.itemId = itemId;
        this.description = description;
        this.cost = cost;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
}