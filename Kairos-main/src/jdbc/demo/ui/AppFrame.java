package jdbc.demo.ui;

import jdbc.demo.Database;
import jdbc.demo.model.ItemRecord;
import jdbc.demo.model.UserRecord;
import jdbc.demo.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Main application window.
 * Layout: dark sidebar (WEST) | header + CardLayout content (CENTER).
 * Navigation is role-gated; panels are created once and cached in CardLayout.
 */
public class AppFrame extends JFrame {

    private static boolean isBorrowerRole(String type) {
        return "Student".equals(type) || "Professor".equals(type);
    }

    // ── Nav metadata ─────────────────────────────────────────────────────────
    /**
     * @param id      Unique sidebar row id (used for selection highlight).
     * @param cardKey CardLayout panel key; multiple rows may share one panel.
     */
    private record NavItem(String id, String cardKey, String label, String subtitle, String[] roles) {
        /** Sidebar id equals card key when each row maps to a distinct panel. */
        NavItem(String cardKey, String label, String subtitle, String[] roles) {
            this(cardKey, cardKey, label, subtitle, roles);
        }
    }

    private static final NavItem[] ALL_NAV = {
        new NavItem("dashboard",  "Dashboard",  "Overview of your system",                 new String[]{"Admin","Student","Professor"}),
        new NavItem("items",      "Equipment",  "Manage all borrowable equipment",          new String[]{"Admin","Custodian"}),
        new NavItem("users",      "Users",      "View and manage system users",             new String[]{"Admin"}),
        new NavItem("activities", "Activities", "Activity requests and approvals",          new String[]{"Admin","Custodian","Student","Professor"}),
        new NavItem("borrows",    "Borrows",    "Equipment borrow records",                 new String[]{"Admin","Custodian","Student","Professor"}),
        new NavItem("facilities", "Facilities", "Available rooms and spaces",               new String[]{"Admin"}),
        new NavItem("reports",    "Reports",    "30 analytical reports",                    new String[]{"Admin"}),
        new NavItem("profile",    "My Profile", "Your account and history",                 new String[]{"Admin"}),
    };

    /** Admin: dashboard, user management, and borrow tracking only. */
    private static final List<NavItem> ADMIN_NAV = List.of(
            new NavItem("dashboard", "Dashboard", "Overview of your system", null),
            new NavItem("users", " Manage Users", "View and manage system users", null),
            new NavItem("borrows", "Track Borrowed Equipment", "Borrow records, returns, and status", null)
    );

    /** Student / Professor: dashboard, activity request, borrow request, view history. */
    private static final List<NavItem> BORROWER_NAV = List.of(
            new NavItem("dashboard", "Dashboard",      "Equipment available to borrow",           null),
            new NavItem("activities", "Activity Requests", "Submit requests; custodian approves", null),
            new NavItem("borrows",   "Borrow Request", "Request equipment for an approved activity", null),
            new NavItem("profile",   "View History",   "Your borrow history",                   null)
    );

    /** Custodian-only sidebar: dashboard plus shared panels (items / activities reuse one card each). */
    private static final List<NavItem> CUSTODIAN_NAV = List.of(
            new NavItem("cu-dashboard", "dashboard", "Dashboard", "Borrow and activity analytics", null),
            new NavItem("cu-users",       "users",       "View Users",                  "Browse system users (read-only)",                       null),
            new NavItem("cu-activities",  "activities",  "Manage Activities",           "Review requests, approve, reject, edit, or remove records", null),
            new NavItem("cu-items-add",   "items",       "Add Equipment",               "Register new borrowable equipment",                     null),
            new NavItem("cu-borrows",     "borrows",     "Manage Borrowed Equipment",   "Track borrows, returns, and equipment status",        null),
            new NavItem("cu-items-man",   "items",       "Manage Equipment",          "Edit, filter, and maintain the equipment inventory",    null)
    );

    // ── State ─────────────────────────────────────────────────────────────────
    private final UserRecord user;
    private final Database   db;
    private final boolean    demoMode;

    private final CardLayout cardLayout  = new CardLayout();
    private final JPanel     contentArea = new JPanel(cardLayout);

    private final JLabel pageTitle    = UIUtils.title("Dashboard");
    private final JLabel pageSubtitle = UIUtils.muted("");

    /** Selected sidebar row id (unique); may share the same panel as another row. */
    private String    currentNavId = "dashboard";
    private JButton[] navBtns;
    private String[]  navIds;
    private List<NavItem> visibleNav;

    /** Custodian shares one {@link CardLayout} card for multiple sidebar rows — sync toolbar modes after navigate. */
    private ItemsPanel      itemsPanelRef;
    private ActivitiesPanel activitiesPanelRef;
    /** Borrow panel — used to open “Create Borrow” from dashboard Request. */
    private BorrowsPanel    borrowsPanelRef;
    /** Borrower dashboard — reminders refresh when borrows change. */
    private DashboardPanel  dashboardPanelRef;

    // Sidebar reference kept so we can resize it on collapse
    private JPanel  sidebarPanel;
    private boolean sidebarCollapsed = false;
    private boolean sidebarToggleInProgress = false;

    // Sidebar sub-sections we show/hide on collapse
    private JPanel     sidebarTop;
    private JScrollPane sidebarNavScroll;
    private JButton    collapseBtn;
    private JButton    logoutBtn;
    private JPanel     sidebarBrand;
    private JPanel     sidebarBrandText;
    private JPanel     sidebarUserCard;
    /** Header EAST block (avatar + name + role); rebuilt when {@link #applySessionUser} runs. */
    private JPanel     headerUserChrome;
    private final int  sidebarExpandedWidth = 220;
    private final int  sidebarCollapsedWidth = 60;

    public AppFrame(UserRecord user, Database db, boolean demoMode) {
        super("Kairos Borrowing System");
        this.user     = user;
        this.db       = db;
        this.demoMode = demoMode;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null);

        contentArea.setBackground(UIColors.BG);

        // Filter nav items by role.
        if ("Custodian".equals(user.getType())) {
            visibleNav = CUSTODIAN_NAV;
        } else if (isBorrowerRole(user.getType())) {
            visibleNav = BORROWER_NAV;
        } else if ("Admin".equals(user.getType())) {
            visibleNav = ADMIN_NAV;
        } else {
            visibleNav = Arrays.stream(ALL_NAV)
                    .filter(n -> Arrays.asList(n.roles()).contains(user.getType()))
                    .toList();
        }

        navBtns = new JButton[visibleNav.size()];
        navIds = new String[visibleNav.size()];

        sidebarPanel = buildSidebar(visibleNav);

        // One CardLayout entry per distinct panel key (custodian has duplicate keys).
        Set<String> addedCards = new LinkedHashSet<>();
        for (NavItem n : visibleNav) {
            if (addedCards.add(n.cardKey())) {
                contentArea.add(buildPanel(n.cardKey()), n.cardKey());
            }
        }

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebarPanel, BorderLayout.WEST);
        root.add(buildMain(),  BorderLayout.CENTER);
        setContentPane(root);

        navigate(navIds.length > 0 ? navIds[0] : "dashboard");
    }

    // ── Panel factory ─────────────────────────────────────────────────────────
    private JPanel buildPanel(String key) {
        switch (key) {
            case "dashboard": {
                DashboardPanel dp = new DashboardPanel(user, db, demoMode,
                        isBorrowerRole(user.getType()) ? this::navigate : null,
                        isBorrowerRole(user.getType()) ? this::openBorrowRequestFromDashboard : null);
                if (isBorrowerRole(user.getType()) || "Custodian".equals(user.getType())) {
                    dashboardPanelRef = dp;
                }
                return dp;
            }
            case "items":
                itemsPanelRef = new ItemsPanel(user, db, demoMode);
                return itemsPanelRef;
            case "users":
                return new UsersPanel(user, db, demoMode);
            case "activities":
                activitiesPanelRef = new ActivitiesPanel(user, db, demoMode);
                return activitiesPanelRef;
            case "borrows": {
                Runnable onBorrowChanged = null;
                if (isBorrowerRole(user.getType())) {
                    onBorrowChanged = () -> {
                        if (dashboardPanelRef != null) {
                            dashboardPanelRef.refreshBorrowerDashboard();
                        }
                    };
                } else if ("Custodian".equals(user.getType())) {
                    onBorrowChanged = () -> {
                        if (dashboardPanelRef != null) {
                            dashboardPanelRef.refreshCustodianDashboard();
                        }
                    };
                }
                borrowsPanelRef = new BorrowsPanel(user, db, demoMode, onBorrowChanged);
                return borrowsPanelRef;
            }
            case "facilities":
                return new FacilitiesPanel(user, db, demoMode);
            case "reports":
                return new ReportsPanel(user, db, demoMode);
            case "profile":
                return new ProfilePanel(user, db, demoMode);
            default:
                return new JPanel();
        }
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar(List<NavItem> items) {
        JPanel sb = new JPanel(new BorderLayout());
        sb.setBackground(UIColors.SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(220, 0));

        sidebarTop = buildSidebarTop();
        sidebarNavScroll = buildNavScroll(items);

        sb.add(sidebarTop,               BorderLayout.NORTH);
        sb.add(sidebarNavScroll,         BorderLayout.CENTER);
        sb.add(buildSidebarBottom(sb),   BorderLayout.SOUTH);

        return sb;
    }

    /** Brand logo + optional demo banner + user card */
    private JPanel buildSidebarTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        // Brand row
        sidebarBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        sidebarBrand.setOpaque(false);

        JPanel logoBox = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIColors.PRIMARY, getWidth(), getHeight(), UIColors.SUCCESS);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        logoBox.setOpaque(false);
        logoBox.setPreferredSize(new Dimension(30, 30));
        JLabel logoIcon = new JLabel("K", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        logoIcon.setForeground(Color.WHITE);
        logoBox.add(logoIcon, BorderLayout.CENTER);

        sidebarBrandText = new JPanel(new GridLayout(2, 1, 0, 0));
        sidebarBrandText.setOpaque(false);
        JLabel appName = new JLabel("KAIROS");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        appName.setForeground(Color.WHITE);
        JLabel appSub = new JLabel("Borrowing System");
        appSub.setFont(UIUtils.F_SMALL);
        appSub.setForeground(UIColors.SIDEBAR_TEXT);
        sidebarBrandText.add(appName);
        sidebarBrandText.add(appSub);

        sidebarBrand.add(logoBox);
        sidebarBrand.add(sidebarBrandText);

        // Demo banner (only shown when MySQL is unreachable)
        if (demoMode) {
            JLabel demoBanner = new JLabel("  DEMO MODE — No DB  ", SwingConstants.CENTER);
            demoBanner.setFont(UIUtils.F_LABEL);
            demoBanner.setForeground(new Color(253, 224, 71));
            demoBanner.setBackground(new Color(133, 77, 14));
            demoBanner.setOpaque(true);
            demoBanner.setBorder(new EmptyBorder(4, 0, 4, 0));
            top.add(demoBanner, BorderLayout.NORTH);
        }

        sidebarUserCard = buildUserCard();
        top.add(sidebarBrand,  demoMode ? BorderLayout.CENTER : BorderLayout.NORTH);
        top.add(sidebarUserCard, BorderLayout.SOUTH);
        return top;
    }

    private JPanel buildUserCard() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        card.setBackground(new Color(30, 41, 59));

        JLabel av = UIUtils.avatar(
                "" + user.getFirstName().charAt(0) + user.getLastName().charAt(0),
                UIColors.roleColor(user.getType()), 28);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 0));
        info.setOpaque(false);
        JLabel nameL = new JLabel(user.getFirstName() + " " + user.getLastName());
        nameL.setFont(UIUtils.F_BOLD);
        nameL.setForeground(Color.WHITE);
        JLabel typeL = new JLabel(user.getType());
        typeL.setFont(UIUtils.F_SMALL);
        typeL.setForeground(UIColors.roleColor(user.getType()));
        info.add(nameL);
        info.add(typeL);

        card.add(av);
        card.add(info);
        return card;
    }

    private JScrollPane buildNavScroll(List<NavItem> items) {
        JPanel nav = new JPanel(new GridBagLayout());
        nav.setBackground(UIColors.SIDEBAR_BG);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        gc.insets = new Insets(1, 8, 1, 8);

        for (int i = 0; i < items.size(); i++) {
            NavItem item = items.get(i);
            navBtns[i] = makeSidebarBtn(item.label(), item.id());
            navIds[i] = item.id();
            gc.gridy = i;
            nav.add(navBtns[i], gc);
        }
        // Push buttons to the top
        gc.gridy = items.size(); gc.weighty = 1.0;
        nav.add(new JPanel() {{ setOpaque(false); }}, gc);

        JScrollPane sp = new JScrollPane(nav);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIColors.SIDEBAR_BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return sp;
    }

    private JPanel buildSidebarBottom(JPanel sidebarRef) {
        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 2));
        bottom.setBackground(UIColors.SIDEBAR_BG);
        bottom.setBorder(new EmptyBorder(6, 8, 10, 8));

        collapseBtn = makeSidebarIconBtn("⟨  Collapse", "Collapse sidebar");
        collapseBtn.addActionListener(e -> toggleSidebar(sidebarRef));

        logoutBtn = makeSidebarIconBtn("⏻ Logout", "Logout");
        logoutBtn.setForeground(new Color(252, 165, 165));
        logoutBtn.addActionListener(e -> {
            if (UIUtils.confirm(this, "Are you sure you want to logout?")) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        bottom.add(collapseBtn);
        bottom.add(logoutBtn);
        return bottom;
    }

    private JButton makeSidebarBtn(String label, String navId) {
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (navId.equals(currentNavId)) {
                    g2.setColor(UIColors.PRIMARY);
                } else if (getModel().isRollover()) {
                    g2.setColor(UIColors.SIDEBAR_HOVER);
                } else {
                    g2.setColor(new Color(0, 0, 0, 0));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(UIUtils.F_BODY);
        b.setForeground(UIColors.SIDEBAR_TEXT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(9, 14, 9, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> navigate(navId));
        return b;
    }

    private JButton makeSidebarTextBtn(String label) {
        JButton b = new JButton(label);
        b.setFont(UIUtils.F_BODY);
        b.setForeground(UIColors.SIDEBAR_TEXT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e)  { b.setForeground(UIColors.SIDEBAR_TEXT); }
        });
        return b;
    }

    private JButton makeSidebarIconBtn(String icon, String tooltip) {
        JButton b = new JButton(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(UIColors.SIDEBAR_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setToolTipText(tooltip);
        b.setFont(new Font("Segoe UI Symbol", Font.BOLD, 11));
        b.setForeground(UIColors.SIDEBAR_TEXT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setBorder(new EmptyBorder(10, 10, 10, 10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(false);
        return b;
    }

    private void toggleSidebar(JPanel sb) {
        if (sidebarToggleInProgress) return;
        sidebarToggleInProgress = true;

        sidebarCollapsed = !sidebarCollapsed;

        // When collapsed: keep logo, hide brand text + user card + nav.
        if (sidebarBrandText != null) sidebarBrandText.setVisible(!sidebarCollapsed);
        if (sidebarUserCard != null) sidebarUserCard.setVisible(!sidebarCollapsed);
        if (sidebarNavScroll != null) sidebarNavScroll.setVisible(!sidebarCollapsed);

        if (collapseBtn != null) {
            collapseBtn.setText(sidebarCollapsed ? "⟩" : "⟨ Collapse");
            collapseBtn.setToolTipText(sidebarCollapsed ? "Expand sidebar" : "Collapse sidebar");
        }
        if (logoutBtn != null) {
            logoutBtn.setText(sidebarCollapsed ? "⏻" : "⏻ Logout");
        }

        // When collapsed, keep the brand row compact and centered.
        if (sidebarBrand != null) {
            sidebarBrand.setBorder(sidebarCollapsed ? new EmptyBorder(10, 0, 6, 0) : null);
            ((FlowLayout) sidebarBrand.getLayout()).setAlignment(sidebarCollapsed ? FlowLayout.CENTER : FlowLayout.LEFT);
        }

        int w = sidebarCollapsed ? sidebarCollapsedWidth : sidebarExpandedWidth;
        sb.setPreferredSize(new Dimension(w, 0));
        sb.setMinimumSize(new Dimension(w, 0));
        sb.setMaximumSize(new Dimension(w, Integer.MAX_VALUE));

        // Avoid “slow/unresponsive” feel by debouncing rapid relayouts.
        SwingUtilities.invokeLater(() -> {
            // Ensure brand text reliably comes back after re-expanding.
            if (!sidebarCollapsed && sidebarBrandText != null) {
                sidebarBrandText.setVisible(true);
                sidebarBrandText.revalidate();
                sidebarBrandText.repaint();
            }
            sb.revalidate();
            sb.repaint();
            Container parent = sb.getParent();
            if (parent != null) { parent.revalidate(); parent.repaint(); }
            sidebarToggleInProgress = false;
        });
    }

    // ── Main area (header + content) ──────────────────────────────────────────
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIColors.BG);
        main.add(buildHeader(),  BorderLayout.NORTH);
        main.add(contentArea,    BorderLayout.CENTER);
        return main;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIColors.HEADER_BG);
        header.setPreferredSize(new Dimension(0, 64));
        // Bottom border only
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIColors.BORDER, 0, false) {
                    @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                        g.setColor(UIColors.BORDER);
                        g.drawLine(x, y + h - 1, x + w, y + h - 1);
                    }
                },
                new EmptyBorder(0, 24, 0, 24)));

        JPanel titleArea = new JPanel(new GridLayout(2, 1));
        titleArea.setOpaque(false);
        titleArea.add(pageTitle);
        titleArea.add(pageSubtitle);
        titleArea.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Right: user avatar + name + role
        // Add vertical padding so right side aligns with the title block.
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setOpaque(false);
        JLabel av = UIUtils.avatar(
                "" + user.getFirstName().charAt(0) + user.getLastName().charAt(0),
                UIColors.roleColor(user.getType()), 32);
        JLabel nameL = new JLabel(user.getFirstName() + " " + user.getLastName());
        nameL.setFont(UIUtils.F_BOLD);
        nameL.setForeground(UIColors.TEXT);
        JLabel roleL = new JLabel(user.getType());
        roleL.setFont(UIUtils.F_SMALL);
        roleL.setForeground(UIColors.TEXT_SECONDARY);
        JPanel nameStack = new JPanel(new GridLayout(2, 1, 0, 0));
        nameStack.setOpaque(false);
        nameStack.add(nameL);
        nameStack.add(roleL);
        right.add(av);
        right.add(nameStack);

        headerUserChrome = right;

        header.add(titleArea, BorderLayout.WEST);
        header.add(right,     BorderLayout.EAST);
        return header;
    }

    /** Rebuilds header and sidebar user block after {@link UserRecord#applyProfileFromForm} mutates the session user. */
    public void refreshUserChrome() {
        refreshHeaderUserChrome();
        rebuildSidebarUserCard();
    }

    private void refreshHeaderUserChrome() {
        if (headerUserChrome == null) {
            return;
        }
        headerUserChrome.removeAll();
        JLabel av = UIUtils.avatar(
                "" + user.getFirstName().charAt(0) + user.getLastName().charAt(0),
                UIColors.roleColor(user.getType()), 32);
        JLabel nameL = new JLabel(user.getFirstName() + " " + user.getLastName());
        nameL.setFont(UIUtils.F_BOLD);
        nameL.setForeground(UIColors.TEXT);
        JLabel roleL = new JLabel(user.getType());
        roleL.setFont(UIUtils.F_SMALL);
        roleL.setForeground(UIColors.TEXT_SECONDARY);
        JPanel nameStack = new JPanel(new GridLayout(2, 1, 0, 0));
        nameStack.setOpaque(false);
        nameStack.add(nameL);
        nameStack.add(roleL);
        headerUserChrome.add(av);
        headerUserChrome.add(nameStack);
        headerUserChrome.revalidate();
        headerUserChrome.repaint();
    }

    private void rebuildSidebarUserCard() {
        if (sidebarTop == null || sidebarUserCard == null) {
            return;
        }
        sidebarTop.remove(sidebarUserCard);
        sidebarUserCard = buildUserCard();
        sidebarTop.add(sidebarUserCard, BorderLayout.SOUTH);
        sidebarTop.revalidate();
        sidebarTop.repaint();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    /** Dashboard “Request” → Borrow Request card + create dialog with item pre-selected. */
    private void openBorrowRequestFromDashboard(ItemRecord item) {
        navigate("borrows");
        String itemId = item.getItemId();
        SwingUtilities.invokeLater(() -> {
            if (borrowsPanelRef != null) {
                borrowsPanelRef.openCreateBorrowDialog(itemId);
            }
        });
    }

    /** Page title/subtitle map keyed by nav key (populated in constructor). */

    public void navigate(String navId) {
        NavItem item = visibleNav.stream().filter(n -> n.id().equals(navId)).findFirst().orElse(null);
        if (item == null) {
            return;
        }
        currentNavId = navId;
        cardLayout.show(contentArea, item.cardKey());

        // Refresh nav button colours
        for (int i = 0; i < navIds.length; i++) {
            navBtns[i].setForeground(navIds[i].equals(navId) ? Color.WHITE : UIColors.SIDEBAR_TEXT);
            navBtns[i].repaint();
        }

        pageTitle.setText(item.label());
        String sub = item.subtitle();
        if ("dashboard".equals(item.cardKey()) && isBorrowerRole(user.getType())) {
            sub = "Welcome back, " + user.getFirstName() + "!";
        }
        pageSubtitle.setText(sub != null ? sub : "");

        if ("dashboard".equals(item.cardKey()) && dashboardPanelRef != null) {
            if (isBorrowerRole(user.getType())) {
                SwingUtilities.invokeLater(() -> dashboardPanelRef.refreshBorrowerDashboard());
            } else if ("Custodian".equals(user.getType())) {
                SwingUtilities.invokeLater(() -> dashboardPanelRef.refreshCustodianDashboard());
            }
        }

        syncCustodianPanelModes(navId);

        if ("activities".equals(item.cardKey()) && activitiesPanelRef != null && isBorrowerRole(user.getType())) {
            activitiesPanelRef.onBorrowerActivitiesNav();
        }
    }

    /** Align toolbar behaviour with the custodian sidebar label (shared cards for items / activities). */
    private void syncCustodianPanelModes(String navId) {
        if (!"Custodian".equals(user.getType())) {
            return;
        }
        if (activitiesPanelRef != null) {
            activitiesPanelRef.applyCustodianNavId(navId);
        }
        if (itemsPanelRef != null) {
            itemsPanelRef.applyCustodianNavId(navId);
        }
    }
}
