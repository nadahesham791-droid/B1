# ✨ i-Wish — Desktop Client/Server Application

[![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![NetBeans](https://img.shields.io/badge/IDE-Apache%20NetBeans-1B6AC6?style=for-the-badge&logo=apache-netbeans-ide&logoColor=white)](https://netbeans.apache.org/)
[![Database](https://img.shields.io/badge/Database-MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Repository](https://img.shields.io/badge/GitHub-B1-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/nadahesham791-droid/B1)

**i-Wish** is a desktop application where users can create wish lists, add friends, inspect friends' wish lists, and collaboratively contribute money to help fund each other's dream gifts with real-time push notifications upon completion.

---

## 🎯 Features Checklist (ITI Specifications Compliant)

### Client Features (1 – 10)
- [x] **1. Register / Sign-in**: Secure user registration and authentication with session handling & password validation (min. 6 characters).
- [x] **2. Add / Remove Friend**: Search users across the system and manage friendships.
- [x] **3. Accept / Decline Friend Request**: Real-time handling of incoming friendship requests.
- [x] **4. Wish List CRUD**: Create, view, and delete items in personal wish list from available catalog.
- [x] **5. View Friends List**: Shows friends with online/offline availability status badges.
- [x] **6. View Friend's Wish List**: Inspect friend's desired items, prices, and funded progress percentages.
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
c:\Users\User\Downloads\b1\ft\ft/
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
├── iwish-client/                  # Client NetBeans / Ant Project
│   ├── nbproject/                 # NetBeans Ant configuration & properties
│   ├── build.xml                  # Ant build file
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

## 👥 3-Member Task Split & Team Responsibilities

| Team Member | Role | Key Modules Owned | Difficulty |
|---|---|---|:---:|
| **Member 1** | **Server & Database Architecture** | Database Schema (`iwish_db.sql`), Multithreaded ServerSocket, `ClientHandler`, JDBC DAOs, ACID Transactions, Admin Server GUI. | ⭐⭐⭐ (Advanced) |
| **Member 2** | **Client Core, Auth & Wishlist** | Client Socket Engine (`NetworkManager`), Authentication & Validation (Login/Register), Wallet/Profile, Catalog Browser & My Wishlist CRUD. | ⭐ (Easiest) |
| **Member 3** | **Social, Contribution & Push Alerts** | Friends System (Search/Request/Accept/Decline), Collaborative Funding Dialog ($10/$25/$50/Full), Real-time Push Listener & Toast Popups. | ⭐⭐ (Moderate) |

---

### 👤 Member 1: Server & Database Architecture Specialist

> **Role Overview:** Responsible for the entire backend infrastructure, concurrent client connections, persistent database storage, and financial transaction integrity.

#### 📁 Files & Classes Owned:
* `sql/iwish_db.sql` (MySQL Schema, tables, relations, seed data)
* `iwish-server/src/server/ServerApp.java` (Server launcher)
* `iwish-server/src/server/db/DBConnection.java` (JDBC connection provider)
* `iwish-server/src/server/network/ServerCore.java` (ServerSocket engine)
* `iwish-server/src/server/network/ClientHandler.java` (Thread-per-client protocol router)
* `iwish-server/src/server/network/SessionManager.java` (Tracks active sessions for push dispatch)
* `iwish-server/src/server/dao/*` (`UserDAO`, `ProductDAO`, `WishListDAO`, `FriendDAO`, `ContributionDAO`, `NotificationDAO`)
* `iwish-server/src/server/ui/ServerMainFrame.java` (Admin dashboard GUI)

#### ⚙️ Technical Highlights:
1. **Multithreaded ServerSocket:** In `ServerCore`, the server listens on port `5005`. Every incoming client connection spawns a new `ClientHandler` thread, allowing multiple clients to interact simultaneously without blocking each other.
2. **ACID Financial Transactions:** In `ContributionDAO.contribute()`, transactions are managed with `conn.setAutoCommit(false)`. If either payer balance deduction or item contribution fails, a full `conn.rollback()` executes, protecting against balance loss or data inconsistency.
3. **Session Management:** `SessionManager` maintains a thread-safe map (`ConcurrentHashMap`) of active users. When a gift is completed, it looks up the recipient and all contributors and pushes instant alerts.
4. **Admin GUI:** `ServerMainFrame` features a start/stop server toggle, live connection count badge, DB test connectivity button, and an Admin form to add new products to the catalog.

#### 🎓 Defense / Exam Questions & Answers:
* **Q: How does the server handle multiple clients simultaneously?**
  * *Answer:* When `serverSocket.accept()` returns a socket connection, we wrap it in a `ClientHandler` instance and start a new `Thread`. Each client has its own thread handling input/output streams.
* **Q: How do you prevent SQL Injection attacks?**
  * *Answer:* Every database query uses `PreparedStatement` with parameterized placeholders (`?`). No string concatenation is used for SQL inputs.
* **Q: How are money transfers kept safe during contributions?**
  * *Answer:* We disable auto-commit (`conn.setAutoCommit(false)`), execute the deduction and contribution insert, and only call `conn.commit()` if all queries succeed. In the `catch` block, `conn.rollback()` restores the initial state.

---

### 👤 Member 2: Client Core, Auth & Wishlist Specialist

> **Role Overview:** Responsible for client application bootstrapping, secure user authentication with field validations, profile/wallet views, and the catalog & personal wishlist management.

#### 📁 Files & Classes Owned:
* `iwish-client/src/client/ClientApp.java` (Client entry point)
* `iwish-client/src/client/network/NetworkManager.java` (Socket connection & synchronous request sender)
* `iwish-client/src/client/ui/components/UITheme.java` (Color palette, fonts, card styles)
* `iwish-client/src/client/ui/auth/LoginRegisterDialog.java` (Sign-in & Sign-up dialog)
* `iwish-client/src/client/ui/dashboard/MainDashboard.java` — Specifically:
  * Profile header (User details, balance display)
  * Catalog Tab (Search & browse available products, add to wishlist)
  * My Wishlist Tab (View personal wishlist, funded progress bar, remove item)

#### ⚙️ Technical Highlights:
1. **Input Validation:** Enforces strict validation rules:
   - Validates that no required field is empty.
   - Enforces password minimum length of 6 characters (`pass.length() >= 6`) on both client and server sides.
2. **Network Protocol Bridge:** `NetworkManager.sendRequest(req)` serializes `Request` objects into JSON lines, sends them across the TCP socket, and awaits the server's `Response`.
3. **Personal Wishlist CRUD:** Users can browse products from the catalog, add items to their personal wishlist, delete items they no longer want, and observe a visual `JProgressBar` reflecting funding completion.
4. **UI Styling:** Clean and responsive Swing layout using custom font hierarchies, soft borders, and color tokens from `UITheme`.

#### 🎓 Defense / Exam Questions & Answers:
* **Q: How did you implement password validation?**
  * *Answer:* In `LoginRegisterDialog.java`, inside `handleRegister()` and `handleLogin()`, we check `if (pass.length() < 6)` and display an informative dialog (`JOptionPane.showMessageDialog`), halting the request before contacting the server.
* **Q: How does the client communicate with the server?**
  * *Answer:* Through `NetworkManager.getInstance().sendRequest(req)`. It sends a JSON-formatted command with an `ActionType` (like `LOGIN` or `ADD_TO_WISHLIST`) and returns the parsed `Response` object.
* **Q: How does the wishlist percentage progress bar work?**
  * *Answer:* Each item has a `price` and `paidAmount`. The percentage is calculated as `(int) ((paidAmount / price) * 100)` and assigned to `JProgressBar.setValue()`. When reaching 100%, the status switches to `COMPLETED`.

---

### 👤 Member 3: Social System, Contributions & Push Alerts Specialist

> **Role Overview:** Responsible for user-to-user social interactions, friend search and requests, collaborative funding calculations, and the asynchronous real-time push notification system.

#### 📁 Files & Classes Owned:
* `iwish-client/src/client/ui/components/ToastNotification.java` (Floating animated toast alert)
* `iwish-common/src/common/dto/FriendRequestDTO.java`
* `iwish-common/src/common/dto/ContributionDTO.java`
* `iwish-common/src/common/dto/NotificationDTO.java`
* `iwish-client/src/client/ui/dashboard/MainDashboard.java` — Specifically:
  * Friends Tab (Search users, send requests, accept/decline incoming requests, online status)
  * Friend's Wishlist Tab (Inspect friend's items, remaining amounts, contribute buttons)
  * Contribution Dialog ($10, $25, $50, or Full remaining contribution)
  * Notifications Tab (History of notifications, mark as read)
  * Background Push Listener hookup

#### ⚙️ Technical Highlights:
1. **Friends System:** Search users by username or email, send friendship requests, review pending requests with Accept/Decline actions, and see friend availability with online badges.
2. **Collaborative Funding:** When inspecting a friend's wishlist, users can contribute money with preset quick buttons ($10, $25, $50) or pay the exact remaining balance. It verifies that the user has enough wallet balance before sending.
3. **Asynchronous Push Listener:** A dedicated background thread inside `NetworkManager` continuously monitors the TCP socket for unsolicited messages with `ActionType.PUSH_NOTIFICATION`.
4. **Toast Notification System:** When a push alert arrives, `ToastNotification` displays a modern floating popup in the lower-right corner of the screen that automatically disappears after a few seconds without freezing the UI.
5. **Dual Notification Flow:** Upon 100% item completion, both the buyer and the receiver receive distinct tailored push notifications.

#### 🎓 Defense / Exam Questions & Answers:
* **Q: How do push notifications reach the client without pressing refresh (Push vs Pull)?**
  * *Answer:* The client maintains a long-lived TCP connection. A background listener thread waits on `reader.readLine()`. When the server dispatches a `PUSH_NOTIFICATION`, the thread intercepts it immediately and triggers the UI listener.
* **Q: How do you safely update the Swing GUI from a background push thread?**
  * *Answer:* Swing is not thread-safe. We use `SwingUtilities.invokeLater(() -> { ... })` so the `ToastNotification` popup and table reloads are dispatched on the Event Dispatch Thread (EDT).
* **Q: What happens when you contribute to a friend's wish?**
  * *Answer:* The client checks that `contributionAmount <= remainingAmount` and `contributionAmount <= userBalance`, then dispatches `ActionType.CONTRIBUTE`. If the contribution completes the gift, the server pushes completion alerts to all participants.

---

## 🚀 How to Run the Project

### Step 1: Database Setup
1. Open **MySQL Workbench**, **phpMyAdmin (XAMPP)**, or MySQL command line.
2. Execute the SQL script located at:
   ```bash
   sql/iwish_db.sql
   ```
   *This creates the database `iwish_db` and seeds users (`ahmed`, `mohamed`, `sara`, `omar`), catalog products, and sample wish lists.*

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
     - Username: `sara` / Password: `123456`
     - Username: `omar` / Password: `123456`
   - *Tip:* You can launch multiple client instances to test real-time contributions and push notifications between friends!

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
