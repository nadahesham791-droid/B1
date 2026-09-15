package server.network;

import common.protocol.JsonHelper;
import common.protocol.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages active connected client sessions.
 * Enables direct routing of server push events (e.g. Gift Completed notifications).
 */
public class SessionManager {

    private static final SessionManager instance = new SessionManager();

    private final Map<Integer, ClientHandler> activeSessions = new ConcurrentHashMap<>();
    private Consumer<Integer> clientCountListener;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return instance;
    }

    public void setClientCountListener(Consumer<Integer> listener) {
        this.clientCountListener = listener;
    }

    public void register(int userId, ClientHandler handler) {
        activeSessions.put(userId, handler);
        notifyCountChanged();
    }

    public void unregister(int userId) {
        activeSessions.remove(userId);
        notifyCountChanged();
    }

    public boolean isUserOnline(int userId) {
        return activeSessions.containsKey(userId);
    }

    public ClientHandler getHandler(int userId) {
        return activeSessions.get(userId);
    }

    public int getActiveCount() {
        return activeSessions.size();
    }

    public Map<Integer, ClientHandler> getAllSessions() {
        return activeSessions;
    }

    /**
     * Push an asynchronous notification to an online user.
     */
    public boolean pushToUser(int userId, Response pushNotification) {
        ClientHandler handler = activeSessions.get(userId);
        if (handler != null && handler.isAlive()) {
            handler.sendMessage(JsonHelper.toJson(pushNotification));
            return true;
        }
        return false;
    }

    /**
     * Broadcast to all connected clients.
     */
    public void broadcast(Response response) {
        String json = JsonHelper.toJson(response);
        for (ClientHandler handler : activeSessions.values()) {
            if (handler.isAlive()) {
                handler.sendMessage(json);
            }
        }
    }

    private void notifyCountChanged() {
        if (clientCountListener != null) {
            clientCountListener.accept(activeSessions.size());
        }
    }
}
