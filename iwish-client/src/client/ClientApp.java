package client;

import client.ui.auth.LoginRegisterDialog;
import client.ui.dashboard.MainDashboard;

import javax.swing.*;

/**
 * Main entry point for the i-Wish Client Application.
 */
public class ClientApp {

    public static void main(String[] args) {
        // Set System Look and Feel for native, crisp rendering
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            LoginRegisterDialog authDialog = new LoginRegisterDialog(null, user -> {
                MainDashboard dashboard = new MainDashboard(user);
                dashboard.setVisible(true);
            });
            authDialog.setVisible(true);
        });
    }
}
