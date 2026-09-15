package common.dto;

import java.io.Serializable;

public class FriendRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int requestId;
    private int senderId;
    private String senderUsername;
    private String senderFullName;
    private int receiverId;
    private String receiverUsername;
    private String receiverFullName;
    private String status; // PENDING, ACCEPTED, DECLINED
    private String createdAt;

    public FriendRequestDTO() {}

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getSenderFullName() { return senderFullName; }
    public void setSenderFullName(String senderFullName) { this.senderFullName = senderFullName; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }

    public String getReceiverFullName() { return receiverFullName; }
    public void setReceiverFullName(String receiverFullName) { this.receiverFullName = receiverFullName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
