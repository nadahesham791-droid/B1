package common.dto;

import java.io.Serializable;

public class NotificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int notificationId;
    private int userId;
    private String type; // GIFT_COMPLETED_BUYER, GIFT_BOUGHT_RECEIVER, FRIEND_REQUEST, etc.
    private String message;
    private boolean isRead;
    private String createdAt;

    public NotificationDTO() {}

    public NotificationDTO(int notificationId, int userId, String type, String message, boolean isRead, String createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
