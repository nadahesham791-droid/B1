package client.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Floating non-intrusive toast notification for real-time buyer/receiver gift alerts.
 */
public class ToastNotification {

    public static void show(Component parent, String title, String message, Color accentColor) {
        SwingUtilities.invokeLater(() -> {
            JWindow window = new JWindow();
            window.setAlwaysOnTop(true);

            JPanel panel = new JPanel(new BorderLayout(10, 5));
            panel.setBackground(UITheme.BG_DARK);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor != null ? accentColor : UITheme.PRIMARY),
                    new EmptyBorder(12, 16, 12, 16)
            ));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setFont(UITheme.FONT_BOLD);
            lblTitle.setForeground(Color.WHITE);

            JLabel lblMsg = new JLabel("<html><body style='width: 250px;'>" + message + "</body></html>");
            lblMsg.setFont(UITheme.FONT_REGULAR);
            lblMsg.setForeground(UITheme.TEXT_LIGHT);

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
            textPanel.setOpaque(false);
            textPanel.add(lblTitle);
            textPanel.add(lblMsg);

            JButton btnClose = new JButton("✕");
            btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnClose.setForeground(UITheme.TEXT_MUTED);
            btnClose.setOpaque(false);
            btnClose.setContentAreaFilled(false);
            btnClose.setBorderPainted(false);
            btnClose.setFocusPainted(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> window.dispose());

            panel.add(textPanel, BorderLayout.CENTER);
            panel.add(btnClose, BorderLayout.EAST);

            window.setContentPane(panel);
            window.pack();

            // Position at bottom-right of parent or screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screenSize.width - window.getWidth() - 25;
            int y = screenSize.height - window.getHeight() - 60;
            window.setLocation(x, y);
            window.setVisible(true);

            // Auto dismiss after 5 seconds
            Timer timer = new Timer(5000, e -> window.dispose());
            timer.setRepeats(false);
            timer.start();
        });
    }
}
