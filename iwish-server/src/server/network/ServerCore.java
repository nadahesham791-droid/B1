package server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ServerSocket engine managing the listener thread and active client handlers.
 */
public class ServerCore {

    private ServerSocket serverSocket;
    private Thread listenerThread;
    private volatile boolean running = false;
    private final Consumer<String> logger;
    private final List<ClientHandler> handlers = new ArrayList<>();

    public ServerCore(Consumer<String> logger) {
        this.logger = logger;
    }

    public synchronized void start(int port) throws IOException {
        if (running) return;

        serverSocket = new ServerSocket(port);
        running = true;
        log("Server listening on port " + port + "...");

        listenerThread = new Thread(() -> {
            while (running && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket, logger);
                    synchronized (handlers) {
                        handlers.add(handler);
                    }
                    new Thread(handler, "Client-" + socket.getRemoteSocketAddress()).start();
                } catch (IOException e) {
                    if (running) {
                        log("Accept error: " + e.getMessage());
                    }
                }
            }
        }, "ServerListenerThread");

        listenerThread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        log("Stopping server...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        synchronized (handlers) {
            for (ClientHandler h : handlers) {
                h.disconnect();
            }
            handlers.clear();
        }

        if (listenerThread != null) {
            listenerThread.interrupt();
        }

        log("Server stopped successfully.");
    }

    public boolean isRunning() {
        return running;
    }

    public List<ClientHandler> getHandlers() {
        synchronized (handlers) {
            return new ArrayList<>(handlers);
        }
    }

    private void log(String msg) {
        if (logger != null) logger.accept(msg);
    }
}
