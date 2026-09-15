package client.network;

import common.dto.UserDTO;
import common.protocol.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Thread-safe Socket client manager maintaining continuous TCP connection
 * to i-Wish Server. Handles synchronous request/response and asynchronous push events.
 */
public class NetworkManager {

    private static final NetworkManager instance = new NetworkManager();

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread listenerThread;
    private volatile boolean connected = false;

    private UserDTO currentUser;
    private final CopyOnWriteArrayList<Consumer<Response>> pushListeners = new CopyOnWriteArrayList<>();
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();

    private String serverHost = "localhost";
    private int serverPort = 5005;

    private NetworkManager() {}

    public static NetworkManager getInstance() {
        return instance;
    }

    public synchronized boolean connect(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        disconnect();

        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            connected = true;

            startListener();
            return true;
        } catch (IOException e) {
            System.err.println("[NetworkManager] Connection failed to " + host + ":" + port + " -> " + e.getMessage());
            connected = false;
            return false;
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while (connected && (line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    Response res = JsonHelper.parseResponse(line);
                    if (res != null) {
                        if (res.getStatus() == ResponseStatus.PUSH_EVENT) {
                            dispatchPushEvent(res);
                        } else {
                            responseQueue.offer(res);
                        }
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("[NetworkManager] Connection dropped: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        }, "ClientSocketListener");

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized Response sendRequest(Request request) {
        if (!connected || writer == null) {
            boolean reconnected = connect(serverHost, serverPort);
            if (!reconnected) {
                return Response.error(request.getAction(), "Cannot connect to server at " + serverHost + ":" + serverPort);
            }
        }

        if (currentUser != null) {
            request.setUserId(currentUser.getUserId());
        }

        try {
            responseQueue.clear();
            String json = JsonHelper.toJson(request);
            writer.println(json);

            // Wait for response up to 8 seconds
            Response res = responseQueue.poll(8, TimeUnit.SECONDS);
            if (res == null) {
                return Response.error(request.getAction(), "Server request timed out.");
            }
            return res;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Response.error(request.getAction(), "Request interrupted.");
        } catch (Exception e) {
            return Response.error(request.getAction(), "Communication error: " + e.getMessage());
        }
    }

    public void addPushListener(Consumer<Response> listener) {
        pushListeners.add(listener);
    }

    public void removePushListener(Consumer<Response> listener) {
        pushListeners.remove(listener);
    }

    private void dispatchPushEvent(Response pushEvent) {
        for (Consumer<Response> listener : pushListeners) {
            try {
                listener.accept(pushEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void disconnect() {
        connected = false;
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() { return connected; }
    public UserDTO getCurrentUser() { return currentUser; }
    public void setCurrentUser(UserDTO currentUser) { this.currentUser = currentUser; }
}
