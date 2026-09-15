package client.ui.auth;

import client.network.NetworkManager;
import client.ui.components.UITheme;
import common.dto.UserDTO;
import common.protocol.ActionType;
import common.protocol.Request;
import common.protocol.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Authentication dialog with tabbed Login and Register views.
 */
public class LoginRegisterDialog extends JDialog {

    private final Consumer<UserDTO> onAuthenticated;

    // Login Fields
    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;

    // Register Fields
    private JTextField txtRegUser;
    private JTextField txtRegEmail;
    private JTextField txtRegFullName;
    private JPasswordField txtRegPass;

    // Server Config
    private JTextField txtServerHost;
    private JTextField txtServerPort;

    public LoginRegisterDialog(Frame owner, Consumer<UserDTO> onAuthenticated) {
        super(owner, "Welcome to i-Wish", true);
        this.onAuthenticated = onAuthenticated;
        initComponents();
    }

    private void initComponents() {
        setSize(460, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // Header Banner
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 5));
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel lblTitle = new JLabel("✨ i-Wish", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Make your friends happy & share wishes together", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(224, 231, 255));

        header.add(lblTitle);
        header.add(lblSub);
        root.add(header, BorderLayout.NORTH);

        // Center Tabs (Sign In vs Register)
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("🔑 Sign In", createLoginPanel());
        tabs.addTab("📝 Create Account", createRegisterPanel());
        root.add(tabs, BorderLayout.CENTER);

        // Footer Server Config
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        footer.setBackground(UITheme.BG_MAIN);
        footer.setBorder(new EmptyBorder(5, 5, 10, 5));

        footer.add(new JLabel("Server:"));
        txtServerHost = new JTextField("localhost", 9);
        footer.add(txtServerHost);

        footer.add(new JLabel("Port:"));
        txtServerPort = new JTextField("5005", 4);
        footer.add(txtServerPort);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel lblUser = new JLabel("Username or Email:");
        lblUser.setFont(UITheme.FONT_BOLD);
        panel.add(lblUser, gbc);

        gbc.gridy++;
        txtLoginUser = new JTextField("ahmed", 20);
        txtLoginUser.setFont(UITheme.FONT_REGULAR);
        txtLoginUser.setPreferredSize(new Dimension(0, 34));
        panel.add(txtLoginUser, gbc);

        gbc.gridy++;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_BOLD);
        panel.add(lblPass, gbc);

        gbc.gridy++;
        txtLoginPass = new JPasswordField("123456", 20);
        txtLoginPass.setFont(UITheme.FONT_REGULAR);
        txtLoginPass.setPreferredSize(new Dimension(0, 34));
        panel.add(txtLoginPass, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 10, 5);
        JButton btnLogin = new JButton("Sign In to i-Wish");
        btnLogin.setFont(UITheme.FONT_BOLD);
        btnLogin.setBackground(UITheme.PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(0, 40));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> handleLogin());
        panel.add(btnLogin, gbc);

        // Demo hint label
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel lblHint = new JLabel("<html><center style='color:#64748b;'>Demo Accounts: ahmed, mohamed, sara, omar<br>Password: 123456</center></html>", SwingConstants.CENTER);
        lblHint.setFont(UITheme.FONT_SMALL);
        panel.add(lblHint, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;

        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridy++;
        txtRegFullName = new JTextField(20);
        txtRegFullName.setPreferredSize(new Dimension(0, 32));
        panel.add(txtRegFullName, gbc);

        gbc.gridy++;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        txtRegUser = new JTextField(20);
        txtRegUser.setPreferredSize(new Dimension(0, 32));
        panel.add(txtRegUser, gbc);

        gbc.gridy++;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridy++;
        txtRegEmail = new JTextField(20);
        txtRegEmail.setPreferredSize(new Dimension(0, 32));
        panel.add(txtRegEmail, gbc);

        gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridy++;
        txtRegPass = new JPasswordField(20);
        txtRegPass.setPreferredSize(new Dimension(0, 32));
        panel.add(txtRegPass, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton btnRegister = new JButton("Create Free Account");
        btnRegister.setFont(UITheme.FONT_BOLD);
        btnRegister.setBackground(UITheme.SUCCESS);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(0, 38));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> handleRegister());
        panel.add(btnRegister, gbc);

        return panel;
    }

    private boolean ensureConnected() {
        String host = txtServerHost.getText().trim();
        int port;
        try {
            port = Integer.parseInt(txtServerPort.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid server port number.", "Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (!NetworkManager.getInstance().isConnected()) {
            boolean ok = NetworkManager.getInstance().connect(host, port);
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Could not connect to i-Wish Server at " + host + ":" + port + ".\nPlease make sure Server is started first!",
                        "Connection Failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private void handleLogin() {
        String user = txtLoginUser.getText().trim();
        String pass = new String(txtLoginPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ensureConnected()) return;

        Request req = new Request(ActionType.LOGIN)
                .put("username", user)
                .put("password", pass);

        Response res = NetworkManager.getInstance().sendRequest(req);
        if (res != null && res.isSuccess()) {
            UserDTO u = extractUser(res);
            if (u != null) {
                NetworkManager.getInstance().setCurrentUser(u);
                dispose();
                onAuthenticated.accept(u);
            }
        } else {
            String msg = res != null ? res.getMessage() : "No response from server.";
            JOptionPane.showMessageDialog(this, msg, "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String fullName = txtRegFullName.getText().trim();
        String user = txtRegUser.getText().trim();
        String email = txtRegEmail.getText().trim();
        String pass = new String(txtRegPass.getPassword());

        if (fullName.isEmpty() || user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required to register.", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!ensureConnected()) return;

        Request req = new Request(ActionType.REGISTER)
                .put("fullName", fullName)
                .put("username", user)
                .put("email", email)
                .put("password", pass);

        Response res = NetworkManager.getInstance().sendRequest(req);
        if (res != null && res.isSuccess()) {
            UserDTO u = extractUser(res);
            if (u != null) {
                NetworkManager.getInstance().setCurrentUser(u);
                JOptionPane.showMessageDialog(this, "Account created successfully! Welcome to i-Wish.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                onAuthenticated.accept(u);
            }
        } else {
            String msg = res != null ? res.getMessage() : "Registration failed.";
            JOptionPane.showMessageDialog(this, msg, "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private UserDTO extractUser(Response res) {
        Object userObj = res.get("user");
        if (userObj instanceof UserDTO) {
            return (UserDTO) userObj;
        }
        if (userObj instanceof Map<?, ?>) {
            Map<?, ?> m = (Map<?, ?>) userObj;
            UserDTO u = new UserDTO();
            Object id = m.get("userId");
            if (id instanceof Number) u.setUserId(((Number) id).intValue());
            u.setUsername((String) m.get("username"));
            u.setEmail((String) m.get("email"));
            u.setFullName((String) m.get("fullName"));
            Object bal = m.get("balance");
            if (bal instanceof Number) u.setBalance(((Number) bal).doubleValue());
            return u;
        }
        return null;
    }
}
