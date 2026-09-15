package common.dto;

import java.io.Serializable;

public class ContributionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int contributionId;
    private int itemId;
    private int contributorId;
    private String contributorName;
    private double amount;
    private String contributedAt;

    public ContributionDTO() {}

    public ContributionDTO(int contributionId, int itemId, int contributorId, String contributorName, double amount, String contributedAt) {
        this.contributionId = contributionId;
        this.itemId = itemId;
        this.contributorId = contributorId;
        this.contributorName = contributorName;
        this.amount = amount;
        this.contributedAt = contributedAt;
    }

    public int getContributionId() { return contributionId; }
    public void setContributionId(int contributionId) { this.contributionId = contributionId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getContributorId() { return contributorId; }
    public void setContributorId(int contributorId) { this.contributorId = contributorId; }

    public String getContributorName() { return contributorName; }
    public void setContributorName(String contributorName) { this.contributorName = contributorName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getContributedAt() { return contributedAt; }
    public void setContributedAt(String contributedAt) { this.contributedAt = contributedAt; }
}
