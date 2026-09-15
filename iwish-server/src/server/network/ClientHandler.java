package server.network;

import common.dto.*;
import common.protocol.*;
import server.dao.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles individual client TCP socket connection, reading requests and dispatching responses.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Consumer<String> logger;
    private BufferedReader reader;
    private PrintWriter writer;
    private volatile boolean running = true;

    private int currentUserId = -1;
    private String currentUsername = "Anonymous";

    private final UserDAO userDAO = new UserDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final WishListDAO wishListDAO = new WishListDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final ContributionDAO contributionDAO = new ContributionDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public ClientHandler(Socket socket, Consumer<String> logger) {
        this.socket = socket;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            log("Client connected from " + socket.getRemoteSocketAddress());

            String line;
            while (running && (line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Request request = JsonHelper.parseRequest(line);
                if (request != null) {
                    Response response = handleRequest(request);
                    if (response != null) {
                        sendMessage(JsonHelper.toJson(response));
                    }
                }
            }
        } catch (IOException e) {
            log("Client disconnected: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public synchronized void sendMessage(String json) {
        if (writer != null) {
            writer.println(json);
        }
    }

    private Response handleRequest(Request req) {
        ActionType action = req.getAction();
        if (action == null) return Response.error(null, "Invalid action specified.");

        try {
            switch (action) {
                case REGISTER: {
                    String u = req.getString("username");
                    String e = req.getString("email");
                    String p = req.getString("password");
                    String f = req.getString("fullName");

                    if (u == null || e == null || p == null || f == null) {
                        return Response.error(action, "All registration fields are required.");
                    }

                    UserDTO user = userDAO.register(u.trim(), e.trim(), p, f.trim());
                    if (user == null) {
                        return Response.error(action, "Username or Email already in use.");
                    }
                    this.currentUserId = user.getUserId();
                    this.currentUsername = user.getUsername();
                    SessionManager.getInstance().register(currentUserId, this);
                    log("User registered: @" + user.getUsername());
                    return Response.ok(action, "Registration successful.").put("user", user);
                }

                case LOGIN: {
                    String u = req.getString("username");
                    String p = req.getString("password");

                    if (u == null || p == null) {
                        return Response.error(action, "Username/Email and Password are required.");
                    }

                    UserDTO user = userDAO.login(u.trim(), p);
                    if (user == null) {
                        return Response.error(action, "Invalid username or password.");
                    }

                    this.currentUserId = user.getUserId();
                    this.currentUsername = user.getUsername();
                    SessionManager.getInstance().register(currentUserId, this);
                    log("User logged in: @" + user.getUsername());
                    return Response.ok(action, "Welcome back, " + user.getFullName() + "!").put("user", user);
                }

                case LOGOUT: {
                    if (currentUserId > 0) {
                        SessionManager.getInstance().unregister(currentUserId);
                        log("User logged out: @" + currentUsername);
                        currentUserId = -1;
                        currentUsername = "Anonymous";
                    }
                    return Response.ok(action, "Logged out successfully.");
                }

                case GET_PROFILE: {
                    int uid = req.getInt("userId", currentUserId);
                    UserDTO user = userDAO.getUserById(uid);
                    if (user != null) {
                        return Response.ok(action, "Profile fetched.").put("user", user);
                    }
                    return Response.error(action, "User not found.");
                }

                case GET_CATALOG: {
                    List<ProductDTO> products = productDAO.getAllProducts();
                    return Response.ok(action, "Catalog loaded.").put("products", products);
                }

                case ADD_CATALOG_ITEM: {
                    String name = req.getString("name");
                    String desc = req.getString("description");
                    double price = req.getDouble("price", 0.0);
                    String cat = req.getString("category");
                    String img = req.getString("imageUrl");

                    if (name == null || price <= 0) {
                        return Response.error(action, "Product name and positive price required.");
                    }
                    ProductDTO added = productDAO.addProduct(name, desc, price, cat, img);
                    log("Admin added new catalog product: " + name + " ($" + price + ")");
                    return Response.ok(action, "Product added to catalog.").put("product", added);
                }

                case GET_MY_WISHLIST: {
                    int uid = req.getInt("userId", currentUserId);
                    List<WishListItemDTO> items = wishListDAO.getWishListByUser(uid);
                    return Response.ok(action, "Wishlist retrieved.").put("items", items);
                }

                case ADD_TO_WISHLIST: {
                    int prodId = req.getInt("productId", -1);
                    if (prodId <= 0) return Response.error(action, "Invalid product ID.");
                    boolean added = wishListDAO.addToWishList(currentUserId, prodId);
                    if (added) {
                        log("@" + currentUsername + " added product #" + prodId + " to wishlist");
                        return Response.ok(action, "Item added to your wishlist!");
                    } else {
                        return Response.error(action, "Item is already in your wishlist.");
                    }
                }

                case REMOVE_FROM_WISHLIST: {
                    int itemId = req.getInt("itemId", -1);
                    boolean removed = wishListDAO.removeFromWishList(currentUserId, itemId);
                    if (removed) {
                        log("@" + currentUsername + " removed item #" + itemId + " from wishlist");
                        return Response.ok(action, "Item removed from your wishlist.");
                    } else {
                        return Response.error(action, "Could not remove item.");
                    }
                }

                case GET_FRIENDS: {
                    List<UserDTO> friends = friendDAO.getFriends(currentUserId);
                    for (UserDTO f : friends) {
                        f.setOnline(SessionManager.getInstance().isUserOnline(f.getUserId()));
                    }
                    return Response.ok(action, "Friends list loaded.").put("friends", friends);
                }

                case GET_PENDING_REQUESTS: {
                    List<FriendRequestDTO> requests = friendDAO.getPendingRequests(currentUserId);
                    return Response.ok(action, "Pending requests loaded.").put("requests", requests);
                }

                case SEARCH_USERS: {
                    String query = req.getString("query");
                    if (query == null || query.trim().isEmpty()) {
                        return Response.error(action, "Search query cannot be empty.");
                    }
                    List<UserDTO> users = userDAO.searchUsers(query.trim(), currentUserId);
                    return Response.ok(action, "Search results.").put("users", users);
                }

                case SEND_FRIEND_REQUEST: {
                    int targetUserId = req.getInt("receiverId", -1);
                    if (targetUserId <= 0 || targetUserId == currentUserId) {
                        return Response.error(action, "Invalid user to add.");
                    }
                    boolean sent = friendDAO.sendFriendRequest(currentUserId, targetUserId);
                    if (sent) {
                        log("@" + currentUsername + " sent friend request to user #" + targetUserId);
                        // Push notification to recipient if online
                        UserDTO me = userDAO.getUserById(currentUserId);
                        String msg = "👤 " + (me != null ? me.getFullName() : "A user") + " sent you a friend request!";
                        notificationDAO.createNotification(targetUserId, "FRIEND_REQUEST", msg);
                        SessionManager.getInstance().pushToUser(targetUserId, Response.pushEvent(msg).put("type", "FRIEND_REQUEST"));
                        return Response.ok(action, "Friend request sent!");
                    } else {
                        return Response.error(action, "Request already sent or already friends.");
                    }
                }

                case ACCEPT_FRIEND_REQUEST: {
                    int reqId = req.getInt("requestId", -1);
                    int senderId = req.getInt("senderId", -1);
                    boolean accepted = friendDAO.acceptFriendRequest(reqId, currentUserId);
                    if (accepted) {
                        log("@" + currentUsername + " accepted friend request #" + reqId);
                        UserDTO me = userDAO.getUserById(currentUserId);
                        String msg = "🤝 " + (me != null ? me.getFullName() : "Your friend") + " accepted your friend request!";
                        if (senderId > 0) {
                            notificationDAO.createNotification(senderId, "FRIEND_ACCEPTED", msg);
                            SessionManager.getInstance().pushToUser(senderId, Response.pushEvent(msg).put("type", "FRIEND_ACCEPTED"));
                        }
                        return Response.ok(action, "Friend request accepted!");
                    }
                    return Response.error(action, "Failed to accept friend request.");
                }

                case DECLINE_FRIEND_REQUEST: {
                    int reqId = req.getInt("requestId", -1);
                    boolean declined = friendDAO.declineFriendRequest(reqId, currentUserId);
                    if (declined) {
                        return Response.ok(action, "Friend request declined.");
                    }
                    return Response.error(action, "Failed to decline friend request.");
                }

                case REMOVE_FRIEND: {
                    int friendId = req.getInt("friendId", -1);
                    boolean removed = friendDAO.removeFriend(currentUserId, friendId);
                    if (removed) {
                        log("@" + currentUsername + " removed friend #" + friendId);
                        return Response.ok(action, "Friend removed successfully.");
                    }
                    return Response.error(action, "Failed to remove friend.");
                }

                case GET_FRIEND_WISHLIST: {
                    int friendId = req.getInt("friendId", -1);
                    if (!friendDAO.areFriends(currentUserId, friendId)) {
                        return Response.error(action, "You must be friends to view this wishlist.");
                    }
                    List<WishListItemDTO> items = wishListDAO.getWishListByUser(friendId);
                    return Response.ok(action, "Friend's wishlist loaded.").put("items", items);
                }

                case CONTRIBUTE: {
                    int itemId = req.getInt("itemId", -1);
                    double amount = req.getDouble("amount", 0.0);

                    ContributionDAO.ContributionResult res = contributionDAO.processContribution(itemId, currentUserId, amount);
                    if (!res.success) {
                        return Response.error(action, res.message);
                    }

                    log("Contribution: @" + currentUsername + " paid $" + amount + " to item #" + itemId + (res.isCompleted ? " [COMPLETED!]" : ""));

                    // Reload user's updated balance
                    UserDTO updatedMe = userDAO.getUserById(currentUserId);

                    // Fetch updated item
                    WishListItemDTO updatedItem = wishListDAO.getWishListItemById(itemId);

                    // If gift is fully funded, trigger DUAL NOTIFICATIONS as required by specs 8 & 9!
                    if (res.isCompleted && updatedItem != null) {
                        String giftName = updatedItem.getProduct().getName();
                        String receiverName = updatedItem.getOwnerName();

                        // 1. [As Receiver] Specification 9:
                        // "Receive a notification that an item of the wish list has been bought by specific friend(s)."
                        String receiverMsg = "🎉 Great news! Your wish item \"" + giftName + "\" has been fully bought by your friends!";
                        notificationDAO.createNotification(res.receiverUserId, "GIFT_BOUGHT_RECEIVER", receiverMsg);
                        SessionManager.getInstance().pushToUser(res.receiverUserId,
                                Response.pushEvent(receiverMsg).put("type", "GIFT_BOUGHT_RECEIVER").put("item", updatedItem));

                        // 2. [As Buyer] Specification 8:
                        // "Receive a notification on the completion of a gift item price"
                        String buyerMsg = "🎁 Success! The gift \"" + giftName + "\" for " + receiverName + " has reached 100% and is fully completed!";
                        for (int buyerId : res.contributorUserIds) {
                            notificationDAO.createNotification(buyerId, "GIFT_COMPLETED_BUYER", buyerMsg);
                            SessionManager.getInstance().pushToUser(buyerId,
                                    Response.pushEvent(buyerMsg).put("type", "GIFT_COMPLETED_BUYER").put("item", updatedItem));
                        }
                    }

                    return Response.ok(action, res.message)
                            .put("isCompleted", res.isCompleted)
                            .put("updatedBalance", updatedMe != null ? updatedMe.getBalance() : 0.0)
                            .put("item", updatedItem);
                }

                case GET_NOTIFICATIONS: {
                    List<NotificationDTO> notifs = notificationDAO.getUserNotifications(currentUserId);
                    return Response.ok(action, "Notifications loaded.").put("notifications", notifs);
                }

                case MARK_NOTIFICATION_READ: {
                    notificationDAO.markAllRead(currentUserId);
                    return Response.ok(action, "Notifications marked as read.");
                }

                default:
                    return Response.error(action, "Unsupported action.");
            }
        } catch (Exception e) {
            log("Error handling request " + action + ": " + e.getMessage());
            e.printStackTrace();
            return Response.error(action, "Internal Server Error: " + e.getMessage());
        }
    }

    public boolean isAlive() {
        return running && socket != null && !socket.isClosed() && socket.isConnected();
    }

    public void disconnect() {
        running = false;
        cleanup();
    }

    private void cleanup() {
        running = false;
        if (currentUserId > 0) {
            SessionManager.getInstance().unregister(currentUserId);
        }
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    private void log(String msg) {
        if (logger != null) logger.accept(msg);
    }

    public int getCurrentUserId() { return currentUserId; }
    public String getCurrentUsername() { return currentUsername; }
    public Socket getSocket() { return socket; }
}
