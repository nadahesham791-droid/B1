# ✨ i-Wish — Desktop Client/Server Application

[![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![NetBeans](https://img.shields.io/badge/IDE-Apache%20NetBeans-1B6AC6?style=for-the-badge&logo=apache-netbeans-ide&logoColor=white)](https://netbeans.apache.org/)
[![Database](https://img.shields.io/badge/Database-MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

**i-Wish** is a desktop application where users can create wish lists, add friends, check their friends' wish lists, and collaboratively contribute money to help fund each other's dream gifts with real-time push notifications upon completion.

---

## 🎯 Features Checklist (ITI Specifications Compliant)

### Client Features (1 – 10)
- [x] **1. Register / Sign-in**: Secure user registration and authentication with session handling.
- [x] **2. Add / Remove Friend**: Search users and manage friendships.
- [x] **3. Accept / Decline Friend Request**: Real-time handling of incoming requests.
- [x] **4. Wish List CRUD**: Create, view, and delete items in personal wish list from available catalog.
- [x] **5. View Friends List**: Shows friends with online/offline availability badges.
- [x] **6. View Friend's Wish List**: Inspect friend's desired items, prices, and funded progress.
- [x] **7. Collaborative Contribution**: Contribute custom or preset amounts ($10, $25, $50, Full) toward any friend's wish item.
- [x] **8. [As Buyer] Completion Notification**: Real-time push alert when a gift you contributed to reaches 100%.
- [x] **9. [As Receiver] Gift Bought Notification**: Real-time push alert when friends fully fund your wish item.
- [x] **10. Friendly GUI**: Modern, responsive layout with progress bars, toast pop-ups, and color-coded statuses.

### Server Features (11 – 14)
- [x] **11. Start / Stop Control**: Admin dashboard to launch or gracefully terminate the server socket.
- [x] **12. Database Manipulation**:
  - Connection management & connection test button.
  - ACID transactions for contributions and wallet balance updates.
  - **Admin Catalog Management**: Form to add new products to the database catalog directly from the server.
- [x] **13. Handles Client Connections**: Multi-threaded `ClientHandler` pool with live client counter.
- [x] **14. Handles Client Requests**: JSON-based protocol with request routing and push event dispatcher.

---

## 📂 Project Architecture

```text
c:\Users\T O  S H Y\Downloads\ft/
├── sql/
│   └── iwish_db.sql               # Complete MySQL schema + initial seed data
│
├── iwish-server/                  # Server NetBeans / Maven Project
│   ├── nbproject/                 # NetBeans configuration
│   ├── pom.xml                    # Maven configuration
│   └── src/
│       ├── common/                # Shared Protocol, DTOs & JSON Engine
│       └── server/
│           ├── db/                # DBConnection (JDBC Connection Manager)
│           ├── dao/               # UserDAO, ProductDAO, WishListDAO, FriendDAO, ContributionDAO, NotificationDAO
│           ├── network/           # ServerCore, ClientHandler, SessionManager
│           ├── ui/                # ServerMainFrame (Admin GUI)
│           └── ServerApp.java     # Main Server Entry Point
│
├── iwish-client/                  # Client NetBeans / Maven Project
│   ├── nbproject/                 # NetBeans configuration
│   ├── pom.xml                    # Maven configuration
│   └── src/
│       ├── common/                # Shared Protocol, DTOs & JSON Engine
│       └── client/
│           ├── network/           # NetworkManager (TCP Socket + Push Listener)
│           ├── ui/
│           │   ├── auth/          # LoginRegisterDialog
│           │   ├── components/    # UITheme, ToastNotification
│           │   └── dashboard/     # MainDashboard (Tabs: Wishlist, Catalog, Friends, Contribution, Notifs)
│           └── ClientApp.java     # Main Client Entry Point
│
└── README.md
```

---

## 🚀 How to Run the Project

### Step 1: Database Setup
1. Open **MySQL Workbench**, **phpMyAdmin (XAMPP)**, or MySQL command line.
2. Execute the SQL script located at:
   ```bash
   sql/iwish_db.sql
   ```
   *This creates `iwish_db` and seeds users (`ahmed`, `mohamed`, `sara`, `omar`), catalog products, and sample wish lists.*

### Step 2: Open & Run in Apache NetBeans
1. Launch **Apache NetBeans**.
2. Go to **File -> Open Project...**.
3. Select `iwish-server` and click **Open Project**.
4. Select `iwish-client` and click **Open Project**.
5. Run the **Server**:
   - Right-click `iwish-server` -> **Run** (Starts `server.ServerApp`).
   - Click **▶ Start Server** (default port: `5005`).
6. Run the **Client**:
   - Right-click `iwish-client` -> **Run** (Starts `client.ClientApp`).
   - Log in using one of the demo accounts:
     - Username: `ahmed` / Password: `123456`
     - Username: `mohamed` / Password: `123456`
   - You can launch multiple client instances to test real-time contributions and push notifications between friends!

---

## 👥 Team Roles & Contributions (Submission Template)

| Team Member | Role | Key Contributions |
|---|---|---|
| **Member 1** | Client UI & Authentication | Login/Register GUI, Session management, Profile & Wallet view, My Wishlist CRUD |
| **Member 2** | Friends & Social Features | Friends list, Search users, Send/Accept/Decline requests, Friend Wishlist viewer |
| **Member 3** | Contributions & Notifications | Collaborative funding logic, Real-time push notification toaster, Notification center |
| **Member 4** | Server & Database Architecture | ServerSocket engine, Multi-threaded ClientHandler, JDBC DAOs, ACID transactions, Admin Catalog manager |

---

## 🎁 Demo Walkthrough Scenario
1. **Launch Server**: Start `iwish-server` on port 5005. Status turns green (`ONLINE`).
2. **Launch Client 1 (`ahmed`)**: Log in. Ahmed's wallet has $1,500.
3. **Launch Client 2 (`mohamed`)**: Log in. Mohamed's wishlist has `Sony WH-1000XM5 Headphones` ($350 total, $150 already paid, $200 remaining).
4. **Contribution**: Ahmed views Mohamed's wishlist, clicks **💖 Contribute**, and enters `$200`.
5. **Real-time Dual Notifications**:
   - Mohamed instantly receives a push alert: *"🎉 Your wish item 'Sony WH-1000XM5' has been fully bought by your friends!"*
   - Ahmed instantly receives a push alert: *"🎁 Success! The gift 'Sony WH-1000XM5' for Mohamed Ali has reached 100% and is fully completed!"*
   - Progress bar turns green at 100% and status updates to `COMPLETED`.
