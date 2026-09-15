package common.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String username;
    private String email;
    private String fullName;
    private double balance;
    private boolean isOnline;

    public UserDTO() {}

    public UserDTO(int userId, String username, String email, String fullName, double balance) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.balance = balance;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    @Override
    public String toString() {
        return fullName + " (@" + username + ")";
    }
}
