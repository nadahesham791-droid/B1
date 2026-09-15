package common.protocol;

/**
 * Protocol actions exchanged between Client and Server over TCP Sockets.
 */
public enum ActionType {
    // Authentication
    REGISTER,
    LOGIN,
    LOGOUT,
    GET_PROFILE,
    
    // Product Catalog (Managed by Server / Admin)
    GET_CATALOG,
    ADD_CATALOG_ITEM, // Admin action
    
    // Wishlist Management
    GET_MY_WISHLIST,
    ADD_TO_WISHLIST,
    REMOVE_FROM_WISHLIST,
    
    // Friends System
    GET_FRIENDS,
    GET_PENDING_REQUESTS,
    SEARCH_USERS,
    SEND_FRIEND_REQUEST,
    ACCEPT_FRIEND_REQUEST,
    DECLINE_FRIEND_REQUEST,
    REMOVE_FRIEND,
    
    // Friends Wishlist & Contribution
    GET_FRIEND_WISHLIST,
    CONTRIBUTE,
    
    // Notifications
    GET_NOTIFICATIONS,
    MARK_NOTIFICATION_READ,
    
    // Server Push Events (Asynchronous notifications pushed from Server to Client)
    PUSH_NOTIFICATION
}
