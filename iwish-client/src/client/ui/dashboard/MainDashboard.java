package client.ui.dashboard;

import client.network.NetworkManager;
import client.ui.components.ToastNotification;
import client.ui.components.UITheme;
import common.dto.*;
import common.protocol.ActionType;
import common.protocol.Request;
import common.protocol.Response;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main application dashboard fulfilling all client specifications:
 * 1. Register/Sign-in session management
 * 2. Add/Remove Friend
 * 3. Accept/Decline Friend Request
 * 4. Create, Update, Delete my Wish List
 * 5. View my Friends list
 * 6. View my Friends Wish List
 * 7. Contribute in buying one or more items from a friend's Wish List
 * 8. [As Buyer] Receive notification on gift completion
 * 9. [As Receiver] Receive notification that an item has been bought by friends
 * 10. Friendly GUI
 */
public class MainDashboard extends JFrame {

    private UserDTO currentUser;

    // Header Components
    private JLabel lblUserGreeting;
    private JLabel lblWalletBalance;
    private JButton btnNotifications;
    private int unreadNotificationsCount = 0;

    // Main Tabbed Pane
    private JTabbedPane mainTabs;

    // Panels
    private JPanel panelMyWishlist;
    private JPanel panelCatalog;
    private JPanel panelFriends;
    private JPanel panelFriendWishlist;
    private JPanel panelNotifications;

    // Dynamic lists containers
    private JPanel myWishlistContainer;
    private JPanel catalogContainer;
    private JPanel friendsListContainer;
    private JPanel pendingRequestsContainer;
    private JPanel searchResultsContainer;
    private JPanel friendWishlistContainer;
    private JPanel notificationsContainer;

    private JLabel lblFriendWishlistTitle;
    private UserDTO selectedFriend;

    public MainDashboard(UserDTO user) {
        super("i-Wish — Wish Together, Gift Happiness");
        this.currentUser = user;
        initComponents();
        setupPushListener();
        loadAllData();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);

        // 1. Top Navigation Bar
        root.add(createTopNavbar(), BorderLayout.NORTH);

        // 2. Center Tabs
        mainTabs = new JTabbedPane();
        mainTabs.setFont(UITheme.FONT_BOLD);

        panelMyWishlist = createMyWishlistTab();
        panelCatalog = createCatalogTab();
        panelFriends = createFriendsTab();
        panelFriendWishlist = createFriendWishlistTab();
        panelNotifications = createNotificationsTab();

        mainTabs.addTab("🎁 My Wish List", panelMyWishlist);
        mainTabs.addTab("🛍️ Explore Catalog", panelCatalog);
        mainTabs.addTab("👥 Friends & Requests", panelFriends);
        mainTabs.addTab("💖 Friend's Wish List", panelFriendWishlist);
        mainTabs.addTab("🔔 Notifications", panelNotifications);

        root.add(mainTabs, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createTopNavbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_SIDEBAR);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Brand
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        JLabel logo = new JLabel("✨ i-Wish");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);

        lblUserGreeting = new JLabel("Hello, " + currentUser.getFullName() + "!");
        lblUserGreeting.setFont(UITheme.FONT_REGULAR);
        lblUserGreeting.setForeground(new Color(203, 213, 225));

        left.add(logo);
        left.add(lblUserGreeting);

        // Right (Balance, Notifications, Sign out)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setOpaque(false);

        lblWalletBalance = new JLabel("💰 Balance: $" + String.format("%.2f", currentUser.getBalance()));
        lblWalletBalance.setFont(UITheme.FONT_BOLD);
        lblWalletBalance.setForeground(new Color(52, 211, 153)); // Emerald green

        btnNotifications = new JButton("🔔 Notifications");
        btnNotifications.setFont(UITheme.FONT_BOLD);
        btnNotifications.setBackground(new Color(51, 65, 85));
        btnNotifications.setForeground(Color.WHITE);
        btnNotifications.setFocusPainted(false);
        btnNotifications.addActionListener(e -> {
            mainTabs.setSelectedComponent(panelNotifications);
            markNotificationsRead();
        });

        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setFont(UITheme.FONT_BOLD);
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> handleLogout());

        right.add(lblWalletBalance);
        right.add(btnNotifications);
        right.add(btnLogout);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ==========================================
    // TAB 1: MY WISH LIST (CRUD)
    // ==========================================
    private JPanel createMyWishlistTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(new EmptyBorder(15, 20, 15, 20));
        tab.setBackground(UITheme.BG_MAIN);

        // Action Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel title = new JLabel("My Personal Wish List");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_MAIN);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setOpaque(false);

        JButton btnAddFromCatalog = new JButton("➕ Add Gift from Catalog");
        btnAddFromCatalog.setFont(UITheme.FONT_BOLD);
        btnAddFromCatalog.setBackground(UITheme.PRIMARY);
        btnAddFromCatalog.setForeground(Color.WHITE);
        btnAddFromCatalog.setFocusPainted(false);
        btnAddFromCatalog.addActionListener(e -> mainTabs.setSelectedComponent(panelCatalog));

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadMyWishlist());

        btnGroup.add(btnAddFromCatalog);
        btnGroup.add(btnRefresh);

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnGroup, BorderLayout.EAST);
        tab.add(toolbar, BorderLayout.NORTH);

        // Scrollable Items Container
        myWishlistContainer = new JPanel();
        myWishlistContainer.setLayout(new BoxLayout(myWishlistContainer, BoxLayout.Y_AXIS));
        myWishlistContainer.setBackground(UITheme.BG_MAIN);

        JScrollPane scroll = new JScrollPane(myWishlistContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        tab.add(scroll, BorderLayout.CENTER);

        return tab;
    }

    // ==========================================
    // TAB 2: EXPLORE CATALOG
    // ==========================================
    private JPanel createCatalogTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(new EmptyBorder(15, 20, 15, 20));
        tab.setBackground(UITheme.BG_MAIN);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel title = new JLabel("Explore Products Catalog");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_MAIN);

        JButton btnRefresh = new JButton("🔄 Refresh Catalog");
        btnRefresh.addActionListener(e -> loadCatalog());

        toolbar.add(title, BorderLayout.WEST);
        toolbar.add(btnRefresh, BorderLayout.EAST);
        tab.add(toolbar, BorderLayout.NORTH);

        catalogContainer = new JPanel();
        catalogContainer.setLayout(new BoxLayout(catalogContainer, BoxLayout.Y_AXIS));
        catalogContainer.setBackground(UITheme.BG_MAIN);

        JScrollPane scroll = new JScrollPane(catalogContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        tab.add(scroll, BorderLayout.CENTER);

        return tab;
    }

    // ==========================================
    // TAB 3: FRIENDS & REQUESTS
    // ==========================================
    private JPanel createFriendsTab() {
        JPanel tab = new JPanel(new GridLayout(1, 2, 20, 0));
        tab.setBorder(new EmptyBorder(15, 20, 15, 20));
        tab.setBackground(UITheme.BG_MAIN);

        // Left Column: My Friends & Pending Requests
        JPanel leftCol = new JPanel(new BorderLayout(0, 15));
        leftCol.setOpaque(false);

        // 1. Friends Sub-panel
        JPanel friendsPanel = new JPanel(new BorderLayout(5, 5));
        friendsPanel.setBackground(Color.WHITE);
        friendsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel friendsHeader = new JPanel(new BorderLayout());
        friendsHeader.setOpaque(false);
        JLabel lblFriendsTitle = new JLabel("👥 My Friends");
        lblFriendsTitle.setFont(UITheme.FONT_SUBTITLE);
        JButton btnRefreshFriends = new JButton("🔄");
        btnRefreshFriends.addActionListener(e -> loadFriends());
        friendsHeader.add(lblFriendsTitle, BorderLayout.WEST);
        friendsHeader.add(btnRefreshFriends, BorderLayout.EAST);
        friendsPanel.add(friendsHeader, BorderLayout.NORTH);

        friendsListContainer = new JPanel();
        friendsListContainer.setLayout(new BoxLayout(friendsListContainer, BoxLayout.Y_AXIS));
        friendsListContainer.setOpaque(false);
        JScrollPane friendsScroll = new JScrollPane(friendsListContainer);
        friendsScroll.setBorder(null);
        friendsPanel.add(friendsScroll, BorderLayout.CENTER);

        // 2. Pending Requests Sub-panel
        JPanel requestsPanel = new JPanel(new BorderLayout(5, 5));
        requestsPanel.setPreferredSize(new Dimension(0, 200));
        requestsPanel.setBackground(Color.WHITE);
        requestsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblRequestsTitle = new JLabel("📩 Pending Requests");
        lblRequestsTitle.setFont(UITheme.FONT_SUBTITLE);
        requestsPanel.add(lblRequestsTitle, BorderLayout.NORTH);

        pendingRequestsContainer = new JPanel();
        pendingRequestsContainer.setLayout(new BoxLayout(pendingRequestsContainer, BoxLayout.Y_AXIS));
        pendingRequestsContainer.setOpaque(false);
        JScrollPane requestsScroll = new JScrollPane(pendingRequestsContainer);
        requestsScroll.setBorder(null);
        requestsPanel.add(requestsScroll, BorderLayout.CENTER);

        leftCol.add(friendsPanel, BorderLayout.CENTER);
        leftCol.add(requestsPanel, BorderLayout.SOUTH);

        // Right Column: Search & Add Friends
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel searchTop = new JPanel(new BorderLayout(10, 5));
        searchTop.setOpaque(false);
        JLabel lblSearchTitle = new JLabel("🔍 Find & Add Friends");
        lblSearchTitle.setFont(UITheme.FONT_SUBTITLE);

        JPanel searchBar = new JPanel(new BorderLayout(5, 0));
        searchBar.setOpaque(false);
        JTextField txtSearch = new JTextField();
        txtSearch.setFont(UITheme.FONT_REGULAR);
        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(UITheme.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> performUserSearch(txtSearch.getText().trim()));

        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnSearch, BorderLayout.EAST);

        searchTop.add(lblSearchTitle, BorderLayout.NORTH);
        searchTop.add(searchBar, BorderLayout.SOUTH);
        searchPanel.add(searchTop, BorderLayout.NORTH);

        searchResultsContainer = new JPanel();
        searchResultsContainer.setLayout(new BoxLayout(searchResultsContainer, BoxLayout.Y_AXIS));
        searchResultsContainer.setOpaque(false);
        JScrollPane searchScroll = new JScrollPane(searchResultsContainer);
        searchScroll.setBorder(null);
        searchPanel.add(searchScroll, BorderLayout.CENTER);

        tab.add(leftCol);
        tab.add(searchPanel);
        return tab;
    }

    // ==========================================
    // TAB 4: FRIEND'S WISH LIST & CONTRIBUTION
    // ==========================================
    private JPanel createFriendWishlistTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(new EmptyBorder(15, 20, 15, 20));
        tab.setBackground(UITheme.BG_MAIN);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        lblFriendWishlistTitle = new JLabel("Select a friend to view their Wish List");
        lblFriendWishlistTitle.setFont(UITheme.FONT_TITLE);
        lblFriendWishlistTitle.setForeground(UITheme.TEXT_MAIN);

        JButton btnBackToFriends = new JButton("👥 Back to Friends");
        btnBackToFriends.addActionListener(e -> mainTabs.setSelectedComponent(panelFriends));

        topBar.add(lblFriendWishlistTitle, BorderLayout.WEST);
        topBar.add(btnBackToFriends, BorderLayout.EAST);
        tab.add(topBar, BorderLayout.NORTH);

        friendWishlistContainer = new JPanel();
        friendWishlistContainer.setLayout(new BoxLayout(friendWishlistContainer, BoxLayout.Y_AXIS));
        friendWishlistContainer.setBackground(UITheme.BG_MAIN);

        JScrollPane scroll = new JScrollPane(friendWishlistContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        tab.add(scroll, BorderLayout.CENTER);

        return tab;
    }

    // ==========================================
    // TAB 5: NOTIFICATIONS CENTER
    // ==========================================
    private JPanel createNotificationsTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(new EmptyBorder(15, 20, 15, 20));
        tab.setBackground(UITheme.BG_MAIN);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Notifications Center");
        title.setFont(UITheme.FONT_TITLE);

        JButton btnMarkRead = new JButton("✓ Mark All as Read");
        btnMarkRead.addActionListener(e -> markNotificationsRead());

        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnMarkRead, BorderLayout.EAST);
        tab.add(topBar, BorderLayout.NORTH);

        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(UITheme.BG_MAIN);

        JScrollPane scroll = new JScrollPane(notificationsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        tab.add(scroll, BorderLayout.CENTER);

        return tab;
    }

    // ==========================================
    // DATA LOADING METHODS
    // ==========================================
    private void loadAllData() {
        loadMyWishlist();
        loadCatalog();
        loadFriends();
        loadPendingRequests();
        loadNotifications();
        refreshProfile();
    }

    private void refreshProfile() {
        new Thread(() -> {
            Request req = new Request(ActionType.GET_PROFILE);
            Response res = NetworkManager.getInstance().sendRequest(req);
            if (res != null && res.isSuccess()) {
                Object userObj = res.get("user");
                if (userObj instanceof Map<?, ?>) {
                    Map<?, ?> m = (Map<?, ?>) userObj;
                    Object bal = m.get("balance");
                    if (bal instanceof Number) {
                        currentUser.setBalance(((Number) bal).doubleValue());
                        SwingUtilities.invokeLater(() -> {
                            lblWalletBalance.setText("💰 Balance: $" + String.format("%.2f", currentUser.getBalance()));
                        });
                    }
                }
            }
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void loadMyWishlist() {
        new Thread(() -> {
            Request req = new Request(ActionType.GET_MY_WISHLIST);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                myWishlistContainer.removeAll();
                if (res != null && res.isSuccess()) {
                    List<?> rawList = (List<?>) res.get("items");
                    if (rawList == null || rawList.isEmpty()) {
                        myWishlistContainer.add(createEmptyState("Your wish list is empty! Explore the catalog to add gifts."));
                    } else {
                        for (Object o : rawList) {
                            WishListItemDTO item = parseWishListItem(o);
                            if (item != null) {
                                myWishlistContainer.add(createMyWishlistItemCard(item));
                                myWishlistContainer.add(Box.createVerticalStrut(12));
                            }
                        }
                    }
                }
                myWishlistContainer.revalidate();
                myWishlistContainer.repaint();
            });
        }).start();
    }

    private JPanel createMyWishlistItemCard(WishListItemDTO item) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(item.isCompleted() ? UITheme.SUCCESS : UITheme.BORDER, item.isCompleted() ? 2 : 1),
                new EmptyBorder(14, 18, 14, 18)
        ));

        // Center Details
        JPanel center = new JPanel(new GridLayout(3, 1, 0, 4));
        center.setOpaque(false);

        String statusBadge = item.isCompleted() ? "  [🎉 COMPLETED - FULLY FUNDED]" : "  [IN PROGRESS]";
        JLabel lblTitle = new JLabel(item.getProduct().getName() + statusBadge);
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(item.isCompleted() ? UITheme.SUCCESS : UITheme.TEXT_MAIN);

        // Progress text
        JLabel lblProgress = new JLabel(String.format("Funded: $%.2f / $%.2f (Remaining: $%.2f)",
                item.getPaidAmount(), item.getProduct().getPrice(), item.getRemainingAmount()));
        lblProgress.setFont(UITheme.FONT_REGULAR);
        lblProgress.setForeground(UITheme.TEXT_MUTED);

        // Progress Bar
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) item.getProgressPercentage());
        bar.setStringPainted(true);
        bar.setString(String.format("%.1f%% Funded", item.getProgressPercentage()));
        bar.setForeground(item.isCompleted() ? UITheme.SUCCESS : UITheme.PRIMARY);

        center.add(lblTitle);
        center.add(lblProgress);
        center.add(bar);

        // Right Action (Delete)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setOpaque(false);

        JButton btnDelete = new JButton("🗑 Remove");
        btnDelete.setBackground(UITheme.DANGER);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove '" + item.getProduct().getName() + "' from your wish list?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                removeFromWishlist(item.getItemId());
            }
        });

        right.add(btnDelete);

        card.add(center, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private void removeFromWishlist(int itemId) {
        new Thread(() -> {
            Request req = new Request(ActionType.REMOVE_FROM_WISHLIST).put("itemId", itemId);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                if (res != null && res.isSuccess()) {
                    loadMyWishlist();
                } else {
                    JOptionPane.showMessageDialog(this, res != null ? res.getMessage() : "Error removing item", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void loadCatalog() {
        new Thread(() -> {
            Request req = new Request(ActionType.GET_CATALOG);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                catalogContainer.removeAll();
                if (res != null && res.isSuccess()) {
                    List<?> rawList = (List<?>) res.get("products");
                    if (rawList != null) {
                        for (Object o : rawList) {
                            ProductDTO prod = parseProduct(o);
                            if (prod != null) {
                                catalogContainer.add(createCatalogProductCard(prod));
                                catalogContainer.add(Box.createVerticalStrut(10));
                            }
                        }
                    }
                }
                catalogContainer.revalidate();
                catalogContainer.repaint();
            });
        }).start();
    }

    private JPanel createCatalogProductCard(ProductDTO prod) {
        JPanel card = new JPanel(new BorderLayout(15, 5));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 4));
        center.setOpaque(false);

        JLabel lblName = new JLabel(prod.getName() + "  —  $" + String.format("%.2f", prod.getPrice()));
        lblName.setFont(UITheme.FONT_SUBTITLE);
        lblName.setForeground(UITheme.TEXT_MAIN);

        JLabel lblDesc = new JLabel("[" + prod.getCategory() + "]  " + (prod.getDescription() != null ? prod.getDescription() : ""));
        lblDesc.setFont(UITheme.FONT_SMALL);
        lblDesc.setForeground(UITheme.TEXT_MUTED);

        center.add(lblName);
        center.add(lblDesc);

        JButton btnAdd = new JButton("➕ Add to My Wishlist");
        btnAdd.setFont(UITheme.FONT_BOLD);
        btnAdd.setBackground(UITheme.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> addToWishlist(prod.getProductId(), prod.getName()));

        card.add(center, BorderLayout.CENTER);
        card.add(btnAdd, BorderLayout.EAST);
        return card;
    }

    private void addToWishlist(int productId, String productName) {
        new Thread(() -> {
            Request req = new Request(ActionType.ADD_TO_WISHLIST).put("productId", productId);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                if (res != null && res.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Added '" + productName + "' to your wish list!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadMyWishlist();
                } else {
                    JOptionPane.showMessageDialog(this, res != null ? res.getMessage() : "Could not add item", "Notice", JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void loadFriends() {
        new Thread(() -> {
            Request req = new Request(ActionType.GET_FRIENDS);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                friendsListContainer.removeAll();
                if (res != null && res.isSuccess()) {
                    List<?> list = (List<?>) res.get("friends");
                    if (list == null || list.isEmpty()) {
                        friendsListContainer.add(createEmptyState("No friends added yet. Use the search to find friends!"));
                    } else {
                        for (Object o : list) {
                            UserDTO f = parseUser(o);
                            if (f != null) {
                                friendsListContainer.add(createFriendRow(f));
                                friendsListContainer.add(Box.createVerticalStrut(8));
                            }
                        }
                    }
                }
                friendsListContainer.revalidate();
                friendsListContainer.repaint();
            });
        }).start();
    }

    private JPanel createFriendRow(UserDTO friend) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setBackground(new Color(248, 250, 252));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                new EmptyBorder(8, 12, 8, 12)
        ));

        String onlineIndicator = friend.isOnline() ? "🟢 " : "⚪ ";
        JLabel lbl = new JLabel(onlineIndicator + friend.getFullName() + " (@" + friend.getUsername() + ")");
        lbl.setFont(UITheme.FONT_BOLD);
        row.add(lbl, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        JButton btnViewWishlist = new JButton("👀 Wish List");
        btnViewWishlist.setFont(UITheme.FONT_SMALL);
        btnViewWishlist.setBackground(UITheme.PRIMARY);
        btnViewWishlist.setForeground(Color.WHITE);
        btnViewWishlist.setFocusPainted(false);
        btnViewWishlist.addActionListener(e -> viewFriendWishlist(friend));

        JButton btnRemove = new JButton("❌");
        btnRemove.setFont(UITheme.FONT_SMALL);
        btnRemove.setToolTipText("Remove Friend");
        btnRemove.setBackground(UITheme.DANGER);
        btnRemove.setForeground(Color.WHITE);
        btnRemove.setFocusPainted(false);
        btnRemove.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Remove " + friend.getFullName() + " from friends?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                removeFriend(friend.getUserId());
            }
        });

        actions.add(btnViewWishlist);
        actions.add(btnRemove);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private void removeFriend(int friendId) {
        new Thread(() -> {
            Request req = new Request(ActionType.REMOVE_FRIEND).put("friendId", friendId);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                if (res != null && res.isSuccess()) {
                    loadFriends();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to remove friend.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void loadPendingRequests() {
        new Thread(() -> {
            Request req = new Request(ActionType.GET_PENDING_REQUESTS);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                pendingRequestsContainer.removeAll();
                if (res != null && res.isSuccess()) {
                    List<?> list = (List<?>) res.get("requests");
                    if (list == null || list.isEmpty()) {
                        pendingRequestsContainer.add(createEmptyState("No pending friend requests."));
                    } else {
                        for (Object o : list) {
                            FriendRequestDTO r = parseFriendRequest(o);
                            if (r != null) {
                                pendingRequestsContainer.add(createPendingRequestRow(r));
                                pendingRequestsContainer.add(Box.createVerticalStrut(6));
                            }
                        }
                    }
                }
                pendingRequestsContainer.revalidate();
                pendingRequestsContainer.repaint();
            });
        }).start();
    }

    private JPanel createPendingRequestRow(FriendRequestDTO req) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setBackground(new Color(254, 243, 199)); // Amber tint
        row.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel lbl = new JLabel("👤 " + req.getSenderFullName() + " (@" + req.getSenderUsername() + ")");
        lbl.setFont(UITheme.FONT_BOLD);
        row.add(lbl, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setOpaque(false);

        JButton btnAccept = new JButton("✔ Accept");
        btnAccept.setBackground(UITheme.SUCCESS);
        btnAccept.setForeground(Color.WHITE);
        btnAccept.setFont(UITheme.FONT_SMALL);
        btnAccept.addActionListener(e -> acceptFriendRequest(req.getRequestId(), req.getSenderId()));

        JButton btnDecline = new JButton("✖ Decline");
        btnDecline.setBackground(UITheme.DANGER);
        btnDecline.setForeground(Color.WHITE);
        btnDecline.setFont(UITheme.FONT_SMALL);
        btnDecline.addActionListener(e -> declineFriendRequest(req.getRequestId()));

        actions.add(btnAccept);
        actions.add(btnDecline);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    private void acceptFriendRequest(int reqId, int senderId) {
        new Thread(() -> {
            Request req = new Request(ActionType.ACCEPT_FRIEND_REQUEST).put("requestId", reqId).put("senderId", senderId);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                if (res != null && res.isSuccess()) {
                    loadPendingRequests();
                    loadFriends();
                }
            });
        }).start();
    }

    private void declineFriendRequest(int reqId) {
        new Thread(() -> {
            Request req = new Request(ActionType.DECLINE_FRIEND_REQUEST).put("requestId", reqId);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                if (res != null && res.isSuccess()) {
                    loadPendingRequests();
                }
            });
        }).start();
    }

    @SuppressWarnings("unchecked")
    private void performUserSearch(String query) {
        if (query.isEmpty()) return;

        new Thread(() -> {
            Request req = new Request(ActionType.SEARCH_USERS).put("query", query);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                searchResultsContainer.removeAll();
                if (res != null && res.isSuccess()) {
                    List<?> list = (List<?>) res.get("users");
                    if (list == null || list.isEmpty()) {
                        searchResultsContainer.add(createEmptyState("No users found matching '" + query + "'"));
                    } else {
                        for (Object o : list) {
                            UserDTO u = parseUser(o);
                            if (u != null) {
                                searchResultsContainer.add(createSearchResultRow(u));
                                searchResultsContainer.add(Box.createVerticalStrut(6));
                            }
                        }
                    }
                }
                searchResultsContainer.revalidate();
                searchResultsContainer.repaint();
            });
        }).start();
    }

    private JPanel createSearchResultRow(UserDTO targetUser) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        row.setBackground(new Color(241, 245, 249));
        row.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel lbl = new JLabel(targetUser.getFullName() + " (@" + targetUser.getUsername() + ")");
        lbl.setFont(UITheme.FONT_BOLD);
        row.add(lbl, BorderLayout.CENTER);

        JButton btnAdd = new JButton("➕ Send Request");
        btnAdd.setFont(UITheme.FONT_SMALL);
        btnAdd.setBackground(UITheme.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> sendFriendRequest(targetUser.getUserId(), targetUser.getFullName()));
        row.add(btnAdd, BorderLayout.EAST);
        return row;
    }

    private void sendFriendRequest(int receiverId, String fullName) {
        new Thread(() -> {
            Request req = new Request(ActionType.SEND_FRIEND_REQUEST).put("receiverId", receiverId);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                if (res != null && res.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Friend request sent to " + fullName + "!", "Sent", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, res != null ? res.getMessage() : "Request failed", "Notice", JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }

    // ==========================================
    // FRIEND'S WISHLIST & CONTRIBUTION DIALOG
    // ==========================================
    private void viewFriendWishlist(UserDTO friend) {
        this.selectedFriend = friend;
        lblFriendWishlistTitle.setText("Viewing " + friend.getFullName() + "'s Wish List");
        mainTabs.setSelectedComponent(panelFriendWishlist);
        loadFriendWishlistData();
    }

    @SuppressWarnings("unchecked")
    private void loadFriendWishlistData() {
        if (selectedFriend == null) return;
        new Thread(() -> {
            Request req = new Request(ActionType.GET_FRIEND_WISHLIST).put("friendId", selectedFriend.getUserId());
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                friendWishlistContainer.removeAll();
                if (res != null && res.isSuccess()) {
                    List<?> list = (List<?>) res.get("items");
                    if (list == null || list.isEmpty()) {
                        friendWishlistContainer.add(createEmptyState(selectedFriend.getFullName() + " has no items in their wish list yet."));
                    } else {
                        for (Object o : list) {
                            WishListItemDTO item = parseWishListItem(o);
                            if (item != null) {
                                friendWishlistContainer.add(createFriendWishlistItemCard(item));
                                friendWishlistContainer.add(Box.createVerticalStrut(12));
                            }
                        }
                    }
                }
                friendWishlistContainer.revalidate();
                friendWishlistContainer.repaint();
            });
        }).start();
    }

    private JPanel createFriendWishlistItemCard(WishListItemDTO item) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(item.isCompleted() ? UITheme.SUCCESS : UITheme.BORDER, item.isCompleted() ? 2 : 1),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel center = new JPanel(new GridLayout(3, 1, 0, 4));
        center.setOpaque(false);

        String badge = item.isCompleted() ? "  [🎉 FULLY BOUGHT!]" : "  [WISH ITEM]";
        JLabel lblTitle = new JLabel(item.getProduct().getName() + badge);
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(item.isCompleted() ? UITheme.SUCCESS : UITheme.TEXT_MAIN);

        JLabel lblProgress = new JLabel(String.format("Price: $%.2f | Paid: $%.2f | Still Needed: $%.2f",
                item.getProduct().getPrice(), item.getPaidAmount(), item.getRemainingAmount()));
        lblProgress.setFont(UITheme.FONT_REGULAR);
        lblProgress.setForeground(UITheme.TEXT_MUTED);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) item.getProgressPercentage());
        bar.setStringPainted(true);
        bar.setString(String.format("%.1f%% Funded", item.getProgressPercentage()));
        bar.setForeground(item.isCompleted() ? UITheme.SUCCESS : UITheme.ACCENT);

        center.add(lblTitle);
        center.add(lblProgress);
        center.add(bar);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        right.setOpaque(false);

        if (item.isCompleted()) {
            JLabel lblCompleted = new JLabel("✨ Completed! 🎁");
            lblCompleted.setFont(UITheme.FONT_BOLD);
            lblCompleted.setForeground(UITheme.SUCCESS);
            right.add(lblCompleted);
        } else {
            JButton btnContribute = new JButton("💖 Contribute");
            btnContribute.setFont(UITheme.FONT_BOLD);
            btnContribute.setBackground(UITheme.ACCENT);
            btnContribute.setForeground(Color.WHITE);
            btnContribute.setFocusPainted(false);
            btnContribute.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnContribute.addActionListener(e -> showContributionDialog(item));
            right.add(btnContribute);
        }

        card.add(center, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private void showContributionDialog(WishListItemDTO item) {
        JDialog dlg = new JDialog(this, "Contribute to " + selectedFriend.getFullName() + "'s Gift", true);
        dlg.setSize(420, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel p = new JPanel(new GridLayout(6, 1, 5, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel lblHeader = new JLabel("Gift: " + item.getProduct().getName(), SwingConstants.CENTER);
        lblHeader.setFont(UITheme.FONT_SUBTITLE);

        JLabel lblRemaining = new JLabel(String.format("Remaining needed: $%.2f", item.getRemainingAmount()), SwingConstants.CENTER);
        lblRemaining.setFont(UITheme.FONT_BOLD);
        lblRemaining.setForeground(UITheme.PRIMARY);

        JLabel lblWallet = new JLabel(String.format("Your Wallet Balance: $%.2f", currentUser.getBalance()), SwingConstants.CENTER);
        lblWallet.setFont(UITheme.FONT_SMALL);
        lblWallet.setForeground(UITheme.TEXT_MUTED);

        // Preset amount buttons
        JPanel presets = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        presets.setOpaque(false);
        JTextField txtAmount = new JTextField(10);
        txtAmount.setFont(UITheme.FONT_BOLD);
        txtAmount.setHorizontalAlignment(JTextField.CENTER);

        double[] presetVals = {10, 25, 50, 100};
        for (double v : presetVals) {
            if (v <= item.getRemainingAmount()) {
                JButton b = new JButton("$" + (int) v);
                b.setFont(UITheme.FONT_SMALL);
                b.addActionListener(e -> txtAmount.setText(String.valueOf((int) v)));
                presets.add(b);
            }
        }
        JButton bMax = new JButton("Full ($" + (int) item.getRemainingAmount() + ")");
        bMax.setFont(UITheme.FONT_SMALL);
        bMax.addActionListener(e -> txtAmount.setText(String.format("%.2f", item.getRemainingAmount())));
        presets.add(bMax);

        JButton btnPay = new JButton("Confirm Contribution");
        btnPay.setFont(UITheme.FONT_BOLD);
        btnPay.setBackground(UITheme.ACCENT);
        btnPay.setForeground(Color.WHITE);
        btnPay.setFocusPainted(false);
        btnPay.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(txtAmount.getText().trim());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Please enter an amount greater than 0.", "Invalid", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (amount > currentUser.getBalance()) {
                    JOptionPane.showMessageDialog(dlg, "Insufficient wallet balance ($" + String.format("%.2f", currentUser.getBalance()) + ").", "Insufficient Funds", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (amount > item.getRemainingAmount() + 0.001) {
                    JOptionPane.showMessageDialog(dlg, "Amount exceeds remaining needed ($" + String.format("%.2f", item.getRemainingAmount()) + ").", "Amount Exceeded", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                dlg.dispose();
                submitContribution(item.getItemId(), amount);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Please enter a valid numeric contribution amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            }
        });

        p.add(lblHeader);
        p.add(lblRemaining);
        p.add(lblWallet);
        p.add(presets);
        p.add(txtAmount);
        p.add(btnPay);

        dlg.setContentPane(p);
        dlg.setVisible(true);
    }

    private void submitContribution(int itemId, double amount) {
        new Thread(() -> {
            Request req = new Request(ActionType.CONTRIBUTE)
                    .put("itemId", itemId)
                    .put("amount", amount);

            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                if (res != null && res.isSuccess()) {
                    Object newBal = res.get("updatedBalance");
                    if (newBal instanceof Number) {
                        currentUser.setBalance(((Number) newBal).doubleValue());
                        lblWalletBalance.setText("💰 Balance: $" + String.format("%.2f", currentUser.getBalance()));
                    }

                    boolean completed = Boolean.TRUE.equals(res.get("isCompleted"));
                    if (completed) {
                        ToastNotification.show(this, "🎉 GIFT FULLY COMPLETED!",
                                "Awesome! Your contribution completed the gift for " + selectedFriend.getFullName() + "!", UITheme.SUCCESS);
                    } else {
                        ToastNotification.show(this, "💖 Contribution Received",
                                "You successfully contributed $" + String.format("%.2f", amount) + "!", UITheme.ACCENT);
                    }

                    loadFriendWishlistData();
                    loadNotifications();
                } else {
                    JOptionPane.showMessageDialog(this, res != null ? res.getMessage() : "Contribution failed", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    // ==========================================
    // NOTIFICATIONS & PUSH LISTENER
    // ==========================================
    @SuppressWarnings("unchecked")
    private void loadNotifications() {
        new Thread(() -> {
            Request req = new Request(ActionType.GET_NOTIFICATIONS);
            Response res = NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                notificationsContainer.removeAll();
                if (res != null && res.isSuccess()) {
                    List<?> list = (List<?>) res.get("notifications");
                    if (list == null || list.isEmpty()) {
                        notificationsContainer.add(createEmptyState("No notifications yet."));
                    } else {
                        unreadNotificationsCount = 0;
                        for (Object o : list) {
                            NotificationDTO n = parseNotification(o);
                            if (n != null) {
                                if (!n.isRead()) unreadNotificationsCount++;
                                notificationsContainer.add(createNotificationCard(n));
                                notificationsContainer.add(Box.createVerticalStrut(8));
                            }
                        }
                        updateNotificationBadge();
                    }
                }
                notificationsContainer.revalidate();
                notificationsContainer.repaint();
            });
        }).start();
    }

    private JPanel createNotificationCard(NotificationDTO n) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        card.setBackground(n.isRead() ? Color.WHITE : new Color(238, 242, 255)); // Highlight unread
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(n.isRead() ? UITheme.BORDER : UITheme.PRIMARY),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblMsg = new JLabel(n.getMessage());
        lblMsg.setFont(n.isRead() ? UITheme.FONT_REGULAR : UITheme.FONT_BOLD);
        lblMsg.setForeground(UITheme.TEXT_MAIN);

        JLabel lblTime = new JLabel(n.getCreatedAt() != null ? n.getCreatedAt() : "");
        lblTime.setFont(UITheme.FONT_SMALL);
        lblTime.setForeground(UITheme.TEXT_MUTED);

        card.add(lblMsg, BorderLayout.CENTER);
        card.add(lblTime, BorderLayout.EAST);
        return card;
    }

    private void markNotificationsRead() {
        new Thread(() -> {
            Request req = new Request(ActionType.MARK_NOTIFICATION_READ);
            NetworkManager.getInstance().sendRequest(req);
            SwingUtilities.invokeLater(() -> {
                unreadNotificationsCount = 0;
                updateNotificationBadge();
                loadNotifications();
            });
        }).start();
    }

    private void updateNotificationBadge() {
        if (unreadNotificationsCount > 0) {
            btnNotifications.setText("🔔 Notifications (" + unreadNotificationsCount + ")");
            btnNotifications.setBackground(UITheme.ACCENT);
        } else {
            btnNotifications.setText("🔔 Notifications");
            btnNotifications.setBackground(new Color(51, 65, 85));
        }
    }

    private void setupPushListener() {
        NetworkManager.getInstance().addPushListener(pushEvent -> {
            SwingUtilities.invokeLater(() -> {
                String msg = pushEvent.getMessage();
                String type = (String) pushEvent.get("type");

                // Specification 8 & 9 Push Events
                if ("GIFT_COMPLETED_BUYER".equalsIgnoreCase(type)) {
                    ToastNotification.show(this, "🎁 Gift Completed!", msg, UITheme.SUCCESS);
                } else if ("GIFT_BOUGHT_RECEIVER".equalsIgnoreCase(type)) {
                    ToastNotification.show(this, "🎉 You Received a Gift!", msg, UITheme.ACCENT);
                } else {
                    ToastNotification.show(this, "🔔 i-Wish Alert", msg, UITheme.PRIMARY);
                }

                // Refresh active data tabs
                loadNotifications();
                loadMyWishlist();
                if (selectedFriend != null) loadFriendWishlistData();
                loadPendingRequests();
                loadFriends();
                refreshProfile();
            });
        });
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Sign Out", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Thread(() -> {
                Request req = new Request(ActionType.LOGOUT);
                NetworkManager.getInstance().sendRequest(req);
            }).start();
            NetworkManager.getInstance().disconnect();
            dispose();
            System.exit(0);
        }
    }

    private JLabel createEmptyState(String text) {
        JLabel lbl = new JLabel("<html><center style='color:#94a3b8; font-size:14px; padding:30px;'>" + text + "</center></html>", SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    // ==========================================
    // DTO PARSERS FROM JSON MAPS
    // ==========================================
    private UserDTO parseUser(Object o) {
        if (o instanceof UserDTO) return (UserDTO) o;
        if (o instanceof Map<?, ?>) {
            Map<?, ?> m = (Map<?, ?>) o;
            UserDTO u = new UserDTO();
            Object id = m.get("userId");
            if (id instanceof Number) u.setUserId(((Number) id).intValue());
            u.setUsername((String) m.get("username"));
            u.setEmail((String) m.get("email"));
            u.setFullName((String) m.get("fullName"));
            Object bal = m.get("balance");
            if (bal instanceof Number) u.setBalance(((Number) bal).doubleValue());
            u.setOnline(Boolean.TRUE.equals(m.get("isOnline")));
            return u;
        }
        return null;
    }

    private ProductDTO parseProduct(Object o) {
        if (o instanceof ProductDTO) return (ProductDTO) o;
        if (o instanceof Map<?, ?>) {
            Map<?, ?> m = (Map<?, ?>) o;
            ProductDTO p = new ProductDTO();
            Object id = m.get("productId");
            if (id instanceof Number) p.setProductId(((Number) id).intValue());
            p.setName((String) m.get("name"));
            p.setDescription((String) m.get("description"));
            Object pr = m.get("price");
            if (pr instanceof Number) p.setPrice(((Number) pr).doubleValue());
            p.setCategory((String) m.get("category"));
            p.setImageUrl((String) m.get("imageUrl"));
            return p;
        }
        return null;
    }

    private WishListItemDTO parseWishListItem(Object o) {
        if (o instanceof WishListItemDTO) return (WishListItemDTO) o;
        if (o instanceof Map<?, ?>) {
            Map<?, ?> m = (Map<?, ?>) o;
            WishListItemDTO w = new WishListItemDTO();
            Object id = m.get("itemId");
            if (id instanceof Number) w.setItemId(((Number) id).intValue());
            Object uid = m.get("userId");
            if (uid instanceof Number) w.setUserId(((Number) uid).intValue());
            w.setOwnerName((String) m.get("ownerName"));
            w.setStatus((String) m.get("status"));
            Object paid = m.get("paidAmount");
            if (paid instanceof Number) w.setPaidAmount(((Number) paid).doubleValue());
            Object rem = m.get("remainingAmount");
            if (rem instanceof Number) w.setRemainingAmount(((Number) rem).doubleValue());
            w.setProduct(parseProduct(m.get("product")));
            return w;
        }
        return null;
    }

    private FriendRequestDTO parseFriendRequest(Object o) {
        if (o instanceof FriendRequestDTO) return (FriendRequestDTO) o;
        if (o instanceof Map<?, ?>) {
            Map<?, ?> m = (Map<?, ?>) o;
            FriendRequestDTO r = new FriendRequestDTO();
            Object id = m.get("requestId");
            if (id instanceof Number) r.setRequestId(((Number) id).intValue());
            Object sid = m.get("senderId");
            if (sid instanceof Number) r.setSenderId(((Number) sid).intValue());
            r.setSenderUsername((String) m.get("senderUsername"));
            r.setSenderFullName((String) m.get("senderFullName"));
            Object rid = m.get("receiverId");
            if (rid instanceof Number) r.setReceiverId(((Number) rid).intValue());
            r.setStatus((String) m.get("status"));
            return r;
        }
        return null;
    }

    private NotificationDTO parseNotification(Object o) {
        if (o instanceof NotificationDTO) return (NotificationDTO) o;
        if (o instanceof Map<?, ?>) {
            Map<?, ?> m = (Map<?, ?>) o;
            NotificationDTO n = new NotificationDTO();
            Object id = m.get("notificationId");
            if (id instanceof Number) n.setNotificationId(((Number) id).intValue());
            n.setType((String) m.get("type"));
            n.setMessage((String) m.get("message"));
            n.setRead(Boolean.TRUE.equals(m.get("isRead")));
            n.setCreatedAt((String) m.get("createdAt"));
            return n;
        }
        return null;
    }
}
