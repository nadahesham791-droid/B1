package common.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WishListItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int itemId;
    private int userId;
    private String ownerName;
    private ProductDTO product;
    private String status; // "AVAILABLE" or "COMPLETED"
    private double paidAmount;
    private double remainingAmount;
    private List<ContributionDTO> contributions = new ArrayList<>();

    public WishListItemDTO() {}

    public WishListItemDTO(int itemId, int userId, String ownerName, ProductDTO product, String status, double paidAmount, double remainingAmount) {
        this.itemId = itemId;
        this.userId = userId;
        this.ownerName = ownerName;
        this.product = product;
        this.status = status;
        this.paidAmount = paidAmount;
        this.remainingAmount = remainingAmount;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public ProductDTO getProduct() { return product; }
    public void setProduct(ProductDTO product) { this.product = product; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(double remainingAmount) { this.remainingAmount = remainingAmount; }

    public List<ContributionDTO> getContributions() { return contributions; }
    public void setContributions(List<ContributionDTO> contributions) { this.contributions = contributions; }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status) || remainingAmount <= 0.001;
    }

    public double getProgressPercentage() {
        if (product == null || product.getPrice() <= 0) return 0.0;
        double pct = (paidAmount / product.getPrice()) * 100.0;
        return Math.min(100.0, Math.max(0.0, pct));
    }
}
