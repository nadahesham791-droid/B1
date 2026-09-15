package server.ui;

import common.dto.ProductDTO;
import server.dao.ProductDAO;
import server.db.DBConnection;
import server.network.ClientHandler;
import server.network.ServerCore;
import server.network.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Server Administration Graphical User Interface.
 * Implements Specifications 11, 12, 13, and 14:
 * - Start / Stop Server
 * - Client Connections & Requests Handling
 * - Real-time activity logs
 * - Database Connection tester
 * - Admin Product Catalog management (Add items for users to add to wish lists)
 */
public class ServerMainFrame extends JFrame {

    private static final int DEFAULT_PORT = 5005;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    private ServerCore serverCore;
    private final ProductDAO productDAO = new ProductDAO();

    // UI Components
    private JLabel lblStatusBadge;
    private JLabel lblConnectedCount;
    private JTextField txtPort;
    private JButton btnStart;
    private JButton btnStop;
    private JTextArea txtLogs;
    private DefaultTableModel clientsTableModel;
    private DefaultTableModel productsTableModel;

    // Database fields
    private JTextField txtDbHost;
    private JTextField txtDbPort;
    private JTextField txtDbName;
    private JTextField txtDbUser;
    private JPasswordField txtDbPass;
    private JLabel lblDbStatus;

    // Add Product fields
    private JTextField txtProdName;
    private JTextField txtProdPrice;
    private JComboBox<String> cmbProdCategory;
    private JTextField txtProdImage;
    private JTextArea txtProdDesc;

    public ServerMainFrame() {
        super("i-Wish Control Center — Server Administration");
        initServerCore();
        initComponents();
        setupSessionListener();
        testInitialDb();
    }

    private void initServerCore() {
        serverCore = new ServerCore(this::appendLog);
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 680);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);

        // Main Layout
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(new Color(245, 247, 250));

        // 1. Top Header Banner
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("🖥️ Dashboard & Logs", createDashboardTab());
        tabbedPane.addTab("👥 Connected Clients", createClientsTab());
        tabbedPane.addTab("🎁 Admin Catalog Manager", createCatalogTab());
        tabbedPane.addTab("🗄️ Database Settings", createDatabaseTab());

        root.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("✨ i-Wish Application Server");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        lblStatusBadge = new JLabel("● OFFLINE");
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatusBadge.setForeground(new Color(239, 68, 68)); // Red

        lblConnectedCount = new JLabel("Clients: 0");
        lblConnectedCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblConnectedCount.setForeground(new Color(148, 163, 184));

        rightPanel.add(lblStatusBadge);
        rightPanel.add(lblConnectedCount);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 250, 252));

        // Top Control Bar
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        controlBar.setBackground(Color.WHITE);
        controlBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 15, 10, 15)
        ));

        controlBar.add(new JLabel("Server Port:"));
        txtPort = new JTextField(String.valueOf(DEFAULT_PORT), 6);
        txtPort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        controlBar.add(txtPort);

        btnStart = new JButton("▶ Start Server");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStart.setBackground(new Color(34, 197, 94));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.addActionListener(e -> startServer());
        controlBar.add(btnStart);

        btnStop = new JButton("⏹ Stop Server");
        btnStop.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnStop.setBackground(new Color(239, 68, 68));
        btnStop.setForeground(Color.WHITE);
        btnStop.setFocusPainted(false);
        btnStop.setEnabled(false);
        btnStop.addActionListener(e -> stopServer());
        controlBar.add(btnStop);

        JButton btnClear = new JButton("🗑 Clear Logs");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.addActionListener(e -> txtLogs.setText(""));
        controlBar.add(btnClear);

        panel.add(controlBar, BorderLayout.NORTH);

        // Center Log Area
        txtLogs = new JTextArea();
        txtLogs.setEditable(false);
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtLogs.setBackground(new Color(15, 23, 42)); // Dark Slate
        txtLogs.setForeground(new Color(241, 245, 249));
        txtLogs.setCaretColor(Color.WHITE);
        txtLogs.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtLogs);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createClientsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 250, 252));

        String[] cols = {"User ID", "Username", "IP Address / Port", "Status"};
        clientsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(clientsTableModel);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(table);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton btnRefresh = new JButton("🔄 Refresh List");
        btnRefresh.addActionListener(e -> refreshClientsTable());
        bottomBar.add(btnRefresh);

        panel.add(bottomBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCatalogTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(248, 250, 252));

        // Form Panel (West)
        JPanel form = new JPanel(new GridBagLayout());
        form.setPreferredSize(new Dimension(340, 0));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel formTitle = new JLabel("➕ Add New Item to Catalog");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formTitle.setForeground(new Color(30, 41, 59));
        gbc.gridwidth = 2;
        form.add(formTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        form.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        txtProdName = new JTextField();
        form.add(txtProdName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Price ($):"), gbc);
        gbc.gridx = 1;
        txtProdPrice = new JTextField();
        form.add(txtProdPrice, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        cmbProdCategory = new JComboBox<>(new String[]{"Electronics", "Gaming", "Audio", "Wearables", "Accessories", "Books", "Fashion", "General"});
        form.add(cmbProdCategory, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Image URL/Name:"), gbc);
        gbc.gridx = 1;
        txtProdImage = new JTextField("default_product.png");
        form.add(txtProdImage, gbc);

        gbc.gridx = 0; gbc.gridy++;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        txtProdDesc = new JTextArea(3, 15);
        txtProdDesc.setLineWrap(true);
        form.add(new JScrollPane(txtProdDesc), gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JButton btnAddProduct = new JButton("Save Product to Database");
        btnAddProduct.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAddProduct.setBackground(new Color(59, 130, 246));
        btnAddProduct.setForeground(Color.WHITE);
        btnAddProduct.setFocusPainted(false);
        btnAddProduct.addActionListener(e -> saveProduct());
        form.add(btnAddProduct, gbc);

        panel.add(form, BorderLayout.WEST);

        // Catalog Table (Center)
        String[] pCols = {"ID", "Name", "Price", "Category", "Image"};
        productsTableModel = new DefaultTableModel(pCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable pTable = new JTable(productsTableModel);
        pTable.setRowHeight(26);
        pTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel tableHolder = new JPanel(new BorderLayout(5, 5));
        tableHolder.setOpaque(false);
        tableHolder.add(new JScrollPane(pTable), BorderLayout.CENTER);

        JButton btnReloadCatalog = new JButton("🔄 Refresh Catalog");
        btnReloadCatalog.addActionListener(e -> loadCatalogTable());
        tableHolder.add(btnReloadCatalog, BorderLayout.SOUTH);

        panel.add(tableHolder, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDatabaseTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(248, 250, 252));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel title = new JLabel("Database Configuration (MySQL)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridwidth = 2;
        card.add(title, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        card.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        txtDbHost = new JTextField("localhost", 15);
        card.add(txtDbHost, gbc);

        gbc.gridx = 0; gbc.gridy++;
        card.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1;
        txtDbPort = new JTextField("3306", 15);
        card.add(txtDbPort, gbc);

        gbc.gridx = 0; gbc.gridy++;
        card.add(new JLabel("Database Name:"), gbc);
        gbc.gridx = 1;
        txtDbName = new JTextField("iwish_db", 15);
        card.add(txtDbName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        card.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtDbUser = new JTextField("root", 15);
        card.add(txtDbUser, gbc);

        gbc.gridx = 0; gbc.gridy++;
        card.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtDbPass = new JPasswordField("root", 15);
        card.add(txtDbPass, gbc);

        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        JButton btnApplyDb = new JButton("Apply & Test DB Connection");
        btnApplyDb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnApplyDb.setBackground(new Color(14, 165, 233));
        btnApplyDb.setForeground(Color.WHITE);
        btnApplyDb.addActionListener(e -> applyAndTestDb());
        card.add(btnApplyDb, gbc);

        gbc.gridy++;
        lblDbStatus = new JLabel("DB Status: Checking...", SwingConstants.CENTER);
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(lblDbStatus, gbc);

        panel.add(card);
        return panel;
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            serverCore.start(port);
            lblStatusBadge.setText("● ONLINE (Port " + port + ")");
            lblStatusBadge.setForeground(new Color(34, 197, 94)); // Green
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            txtPort.setEnabled(false);
            appendLog("Server started on port " + port);
            loadCatalogTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to start server: " + e.getMessage(), "Server Error", JOptionPane.ERROR_MESSAGE);
            appendLog("Start Error: " + e.getMessage());
        }
    }

    private void stopServer() {
        serverCore.stop();
        lblStatusBadge.setText("● OFFLINE");
        lblStatusBadge.setForeground(new Color(239, 68, 68));
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        txtPort.setEnabled(true);
        lblConnectedCount.setText("Clients: 0");
        refreshClientsTable();
        appendLog("Server stopped.");
    }

    private void setupSessionListener() {
        SessionManager.getInstance().setClientCountListener(count -> {
            SwingUtilities.invokeLater(() -> {
                lblConnectedCount.setText("Clients: " + count);
                refreshClientsTable();
            });
        });
    }

    private void refreshClientsTable() {
        clientsTableModel.setRowCount(0);
        Map<Integer, ClientHandler> sessions = SessionManager.getInstance().getAllSessions();
        for (Map.Entry<Integer, ClientHandler> entry : sessions.entrySet()) {
            ClientHandler h = entry.getValue();
            clientsTableModel.addRow(new Object[]{
                    h.getCurrentUserId(),
                    "@" + h.getCurrentUsername(),
                    h.getSocket().getRemoteSocketAddress().toString(),
                    "Connected"
            });
        }
    }

    private void saveProduct() {
        String name = txtProdName.getText().trim();
        String priceStr = txtProdPrice.getText().trim();
        String cat = (String) cmbProdCategory.getSelectedItem();
        String img = txtProdImage.getText().trim();
        String desc = txtProdDesc.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product Name and Price are required.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) throw new NumberFormatException();

            ProductDTO prod = productDAO.addProduct(name, desc, price, cat, img);
            if (prod != null) {
                JOptionPane.showMessageDialog(this, "Product added successfully to catalog!", "Success", JOptionPane.INFORMATION_MESSAGE);
                txtProdName.setText("");
                txtProdPrice.setText("");
                txtProdDesc.setText("");
                loadCatalogTable();
                appendLog("Admin added catalog product: " + name + " ($" + price + ")");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save product to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number for price.", "Invalid Price", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCatalogTable() {
        productsTableModel.setRowCount(0);
        try {
            List<ProductDTO> list = productDAO.getAllProducts();
            for (ProductDTO p : list) {
                productsTableModel.addRow(new Object[]{
                        p.getProductId(),
                        p.getName(),
                        "$" + String.format("%.2f", p.getPrice()),
                        p.getCategory(),
                        p.getImageUrl()
                });
            }
        } catch (Exception e) {
            appendLog("Could not load catalog: " + e.getMessage());
        }
    }

    private void testInitialDb() {
        new Thread(() -> {
            boolean ok = DBConnection.getInstance().testConnection();
            SwingUtilities.invokeLater(() -> updateDbStatus(ok));
        }).start();
    }

    private void applyAndTestDb() {
        String host = txtDbHost.getText().trim();
        String port = txtDbPort.getText().trim();
        String name = txtDbName.getText().trim();
        String user = txtDbUser.getText().trim();
        String pass = new String(txtDbPass.getPassword());

        String url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        DBConnection.configure(url, user, pass);

        new Thread(() -> {
            boolean ok = DBConnection.getInstance().testConnection();
            SwingUtilities.invokeLater(() -> {
                updateDbStatus(ok);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Database connection successful!", "DB Connected", JOptionPane.INFORMATION_MESSAGE);
                    loadCatalogTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to connect to database at " + url + "\nPlease ensure MySQL service is running and database is imported.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    private void updateDbStatus(boolean ok) {
        if (ok) {
            lblDbStatus.setText("● Database: CONNECTED");
            lblDbStatus.setForeground(new Color(34, 197, 94));
        } else {
            lblDbStatus.setText("● Database: DISCONNECTED (Check credentials)");
            lblDbStatus.setForeground(new Color(239, 68, 68));
        }
    }

    public void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            String time = timeFormat.format(new Date());
            txtLogs.append("[" + time + "] " + msg + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
        });
    }
}
