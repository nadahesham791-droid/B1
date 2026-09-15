package server;

import server.ui.ServerMainFrame;

import javax.swing.*;

/**
 * Main entry point for the i-Wish Server Application.
 */
public class ServerApp {

    public static void main(String[] args) {
        // Set System Look and Feel for native operating system appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            ServerMainFrame frame = new ServerMainFrame();
            frame.setVisible(true);
        });
    }
}
