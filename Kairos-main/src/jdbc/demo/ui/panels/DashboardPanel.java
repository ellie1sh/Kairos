package jdbc.demo.ui.panels;

import jdbc.demo.Database;
import jdbc.demo.model.*;
import jdbc.demo.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DashboardPanel extends JPanel {

    private static final int RECENT_LIST_SCROLL_HEIGHT = 240;
    /** Centered metric tiles (reference dashboard). */
    private static final int STAT_TILE_ICON = 52;
    private static final int STAT_TILE_SYMBOL_PT = 21;
    private static final int STAT_TILE_VALUE_PT = 28;

    private final UserRecord user;
    private final Database   db;
    private final boolean    demo;
    /** When non-null (borrower), quick actions navigate sidebar keys: {@code borrows}, {@code profile}. */
    private final Consumer<String> borrowerNavigate;
    /** When non-null (borrower), equipment row “Request” opens borrow flow with this item. */
    private final Consumer<ItemRecord> borrowerOpenBorrowRequest;

    /** Borrower dashboard wrap — EAST reminders replaced by {@link #refreshBorrowerDashboard()}. */
    private JPanel borrowerDashboardWrap;
    private JPanel borrowerEastReminders;

    public DashboardPanel(UserRecord user, Database db, boolean demo) {
        this(user, db, demo, null, null);
    }

    public DashboardPanel(UserRecord user, Database db, boolean demo, Consumer<String> borrowerNavigate) {
        this(user, db, demo, borrowerNavigate, null);
    }

    public DashboardPanel(UserRecord user, Database db, boolean demo,
                          Consumer<String> borrowerNavigate,
                          Consumer<ItemRecord> borrowerOpenBorrowRequest) {
        super(new BorderLayout());
        this.user = user;
        this.db   = db;
        this.demo = demo;
        this.borrowerNavigate = borrowerNavigate;
        this.borrowerOpenBorrowRequest = borrowerOpenBorrowRequest;
        setBackground(UIColors.BG);
        setBorder(new EmptyBorder(16, 24, 16, 24));
        build();
    }

    private void build() {
        List<ItemRecord>     items      = safeItems();
        boolean isBorrower   = "Student".equals(user.getType()) || "Professor".equals(user.getType());

        if (isBorrower) {
            buildBorrowerDashboard(items);
            return;
        }

        List<ActivityRecord> activities = safeActivities();
        List<BorrowRecord>   borrows    = safeBorrows();

        if ("Custodian".equals(user.getType())) {
            buildCustodianDashboard(items, activities, borrows);
            return;
        }

        long available  = items.stream().filter(i -> "available".equals(i.getAvailabilityStatus())).count();
        long pending    = activities.stream().filter(a -> "Pending".equals(a.getStatus())).count();
        long active     = borrows.stream().filter(b -> "borrowed".equals(b.getStatus())).count();

        boolean isAdmin      = "Admin".equals(user.getType());

        // ── Stat cards (always 4) ──────────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);

        if (isAdmin) {
            statsRow.add(statCard("Total Equipment",  String.valueOf(items.size()),     UIColors.PRIMARY, "▣",
                    "All equipment records registered in the system."));
            statsRow.add(statCard("Available",        String.valueOf(available),         UIColors.SUCCESS, "✓",
                    "Items currently marked available to borrow."));
            statsRow.add(statCard("Active Borrows",   String.valueOf(active),            UIColors.WARNING, "⇄",
                    "Borrow transactions still marked as borrowed."));
            statsRow.add(statCard("Pending Requests", String.valueOf(pending),           UIColors.PURPLE,  "⏳",
                    "Activity requests awaiting approval."));
        }

        // ── Recent panels side by side ──────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 2, 16, 0));
        cards.setOpaque(false);
        boolean expandRecent = isAdmin;
        cards.add(recentList("Recent Activity Requests",
                "Newest first — scroll to see more.",
                activities.stream().limit(80)
                        .map(a -> new String[]{a.getActivityName(), a.getActivityDate(), a.getStatus()}).toList(),
                expandRecent));
        cards.add(recentList("Recent Borrow Records",
                "Newest first — scroll to see more.",
                borrows.stream().limit(80)
                        .map(b -> new String[]{b.getBorrowerName(), b.getItemName() == null ? "—" : b.getItemName(), b.getStatus()}).toList(),
                expandRecent));

        if (isAdmin) {
            JPanel northBlock = new JPanel();
            northBlock.setLayout(new BoxLayout(northBlock, BoxLayout.PAGE_AXIS));
            northBlock.setOpaque(false);
            statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            northBlock.add(statsRow);
            add(northBlock, BorderLayout.NORTH);
            add(cards, BorderLayout.CENTER);
        } else {
            JPanel content = new JPanel(new GridLayout(2, 1, 0, 18));
            content.setOpaque(false);
            content.add(statsRow);
            content.add(cards);
            add(content, BorderLayout.NORTH);
        }
    }

    /**
     * Custodian overview: KPI tiles, borrow/activity status analytics, and recent lists (live data on navigate / refresh).
     */
    private void buildCustodianDashboard(List<ItemRecord> items, List<ActivityRecord> activities, List<BorrowRecord> borrows) {
        long availableItems = items.stream().filter(i -> "available".equals(i.getAvailabilityStatus())).count();
        long activeBorrows = borrows.stream().filter(b -> "borrowed".equals(b.getStatus())).count();
        long pendingActs = activities.stream().filter(a -> "Pending".equals(a.getStatus())).count();
        long approvedActs = activities.stream().filter(a -> "Approved".equals(a.getStatus())).count();

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.add(statCard("Active borrows", String.valueOf(activeBorrows), UIColors.WARNING, "⇄",
                "Loan transactions currently marked borrowed."));
        statsRow.add(statCard("Pending activities", String.valueOf(pendingActs), UIColors.PURPLE, "⏳",
                "Requests awaiting review."));
        statsRow.add(statCard("Approved activities", String.valueOf(approvedActs), UIColors.SUCCESS, "✓",
                "Approved activities borrowers may use."));
        statsRow.add(statCard("Available equipment", String.valueOf(availableItems), UIColors.PRIMARY, "▣",
                "Units marked available to borrow."));

        JPanel analyticsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        analyticsRow.setOpaque(false);
        analyticsRow.add(buildCustodianBorrowAnalyticsCard(borrows));
        analyticsRow.add(buildCustodianActivityAnalyticsCard(activities));

        JPanel recentRow = new JPanel(new GridLayout(1, 2, 16, 0));
        recentRow.setOpaque(false);
        recentRow.add(recentList("Recent activity requests",
                "Newest first — scroll to see more.",
                activities.stream().limit(80)
                        .map(a -> new String[]{a.getActivityName(), a.getActivityDate(), a.getStatus()}).toList(),
                true));
        recentRow.add(recentList("Recent borrow records",
                "Newest first — scroll to see more.",
                borrows.stream().limit(80)
                        .map(b -> new String[]{b.getBorrowerName(), b.getItemName() == null ? "—" : b.getItemName(), b.getStatus()}).toList(),
                true));

        JPanel northBlock = new JPanel();
        northBlock.setLayout(new BoxLayout(northBlock, BoxLayout.PAGE_AXIS));
        northBlock.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        analyticsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        northBlock.add(statsRow);
        northBlock.add(Box.createVerticalStrut(18));
        northBlock.add(analyticsRow);

        add(northBlock, BorderLayout.NORTH);
        add(recentRow, BorderLayout.CENTER);
    }

    private JPanel buildCustodianBorrowAnalyticsCard(List<BorrowRecord> borrows) {
        JPanel card = UIUtils.elevatedCard(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 18, 18));

        JLabel h = new JLabel("Borrow analytics");
        h.setFont(UIUtils.F_BOLD);
        h.setForeground(UIColors.TEXT);
        JLabel hint = UIUtils.muted("Distribution by loan status across all records.");
        JPanel head = new JPanel(new BorderLayout(0, 4));
        head.setOpaque(false);
        head.add(h, BorderLayout.NORTH);
        head.add(hint, BorderLayout.SOUTH);

        Map<String, Long> byStatus = borrows.stream()
                .collect(Collectors.groupingBy(
                        b -> (b.getStatus() == null || b.getStatus().isBlank()) ? "—" : b.getStatus(),
                        Collectors.counting()));

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.PAGE_AXIS));
        rows.setOpaque(false);

        String[] preferredOrder = {"borrowed", "returned", "returned with damage", "rejected"};
        List<String> keys = new ArrayList<>();
        for (String s : preferredOrder) {
            if (byStatus.containsKey(s)) {
                keys.add(s);
            }
        }
        TreeSet<String> rest = new TreeSet<>(byStatus.keySet());
        rest.removeAll(keys);
        keys.addAll(rest);

        long total = borrows.size();
        if (keys.isEmpty()) {
            rows.add(UIUtils.muted("No borrow records yet."));
        } else {
            for (int i = 0; i < keys.size(); i++) {
                String status = keys.get(i);
                long c = byStatus.getOrDefault(status, 0L);
                rows.add(custodianAnalyticsMetricRow(status.equals("—") ? "Unknown status" : status, c, total));
                if (i < keys.size() - 1) {
                    rows.add(Box.createVerticalStrut(8));
                }
            }
        }

        card.add(head, BorderLayout.NORTH);
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildCustodianActivityAnalyticsCard(List<ActivityRecord> activities) {
        JPanel card = UIUtils.elevatedCard(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(16, 18, 18, 18));

        JLabel h = new JLabel("Activity analytics");
        h.setFont(UIUtils.F_BOLD);
        h.setForeground(UIColors.TEXT);
        JLabel hint = UIUtils.muted("Request pipeline by approval status.");
        JPanel head = new JPanel(new BorderLayout(0, 4));
        head.setOpaque(false);
        head.add(h, BorderLayout.NORTH);
        head.add(hint, BorderLayout.SOUTH);

        Map<String, Long> byStatus = activities.stream()
                .collect(Collectors.groupingBy(
                        a -> (a.getStatus() == null || a.getStatus().isBlank()) ? "—" : a.getStatus(),
                        Collectors.counting()));

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.PAGE_AXIS));
        rows.setOpaque(false);

        String[] preferredOrder = {"Pending", "Approved", "Rejected"};
        List<String> keys = new ArrayList<>();
        for (String s : preferredOrder) {
            if (byStatus.containsKey(s)) {
                keys.add(s);
            }
        }
        TreeSet<String> rest = new TreeSet<>(byStatus.keySet());
        rest.removeAll(keys);
        keys.addAll(rest);

        long total = activities.size();
        if (keys.isEmpty()) {
            rows.add(UIUtils.muted("No activity requests yet."));
        } else {
            for (int i = 0; i < keys.size(); i++) {
                String status = keys.get(i);
                long c = byStatus.getOrDefault(status, 0L);
                rows.add(custodianAnalyticsMetricRow(status.equals("—") ? "Unknown status" : status, c, total));
                if (i < keys.size() - 1) {
                    rows.add(Box.createVerticalStrut(8));
                }
            }
        }

        card.add(head, BorderLayout.NORTH);
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel custodianAnalyticsMetricRow(String label, long count, long total) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel left = new JLabel(label);
        left.setFont(UIUtils.F_BODY);
        left.setForeground(UIColors.TEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JLabel val = new JLabel(String.valueOf(count));
        val.setFont(UIUtils.F_BOLD);
        val.setForeground(UIColors.TEXT);
        long pct = total > 0 ? Math.round(100.0 * count / total) : 0;
        right.add(val);
        right.add(UIUtils.muted(pct + "%"));

        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    /** Reloads custodian KPI and analytics from the database (or demo provider). */
    public void refreshCustodianDashboard() {
        if (!"Custodian".equals(user.getType())) {
            return;
        }
        removeAll();
        buildCustodianDashboard(safeItems(), safeActivities(), safeBorrows());
        revalidate();
        repaint();
    }

    /**
     * Borrower dashboard modeled on the reference layout: quick actions, filterable equipment cards (no images),
     * and upcoming return reminders. Item thumbnails are intentionally omitted.
     */
    private void buildBorrowerDashboard(List<ItemRecord> items) {
        List<BorrowRecord> borrows = safeBorrows();
        List<ItemRecord> allItems = List.copyOf(items);

        JPanel mainColumn = new JPanel(new BorderLayout(0, 18));
        mainColumn.setOpaque(false);
        mainColumn.add(buildBorrowerQuickActionsBar(), BorderLayout.NORTH);
        mainColumn.add(buildBorrowerEquipmentBrowser(allItems), BorderLayout.CENTER);

        JPanel east = buildBorrowerRemindersPanel(borrows);
        east.setOpaque(false);
        east.setPreferredSize(new Dimension(300, 0));
        east.setMinimumSize(new Dimension(260, 120));

        JPanel wrap = new JPanel(new BorderLayout(20, 0));
        wrap.setOpaque(false);
        wrap.add(mainColumn, BorderLayout.CENTER);
        wrap.add(east, BorderLayout.EAST);

        borrowerDashboardWrap = wrap;
        borrowerEastReminders = east;

        add(wrap, BorderLayout.CENTER);
    }

    /**
     * Reloads “Upcoming Return Reminders” from the database (or demo provider) so new borrows appear
     * without restarting the app.
     */
    public void refreshBorrowerDashboard() {
        boolean isBorrower = "Student".equals(user.getType()) || "Professor".equals(user.getType());
        if (!isBorrower || borrowerDashboardWrap == null || borrowerEastReminders == null) {
            return;
        }
        borrowerDashboardWrap.remove(borrowerEastReminders);
        JPanel east = buildBorrowerRemindersPanel(safeBorrows());
        east.setOpaque(false);
        east.setPreferredSize(new Dimension(300, 0));
        east.setMinimumSize(new Dimension(260, 120));
        borrowerEastReminders = east;
        borrowerDashboardWrap.add(east, BorderLayout.EAST);
        borrowerDashboardWrap.revalidate();
        borrowerDashboardWrap.repaint();
    }

    private JPanel buildBorrowerQuickActionsBar() {
        JPanel wrap = UIUtils.elevatedCard(new BorderLayout(12, 12));
        wrap.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel qa = new JLabel("Quick Actions");
        qa.setFont(UIUtils.F_BOLD);
        qa.setForeground(UIColors.TEXT);
        JLabel status = new JLabel("  System Status: Optimal  ✓");
        status.setFont(UIUtils.F_SMALL);
        status.setForeground(UIColors.SUCCESS);
        top.add(qa, BorderLayout.WEST);
        top.add(status, BorderLayout.EAST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        JButton newBorrow = UIUtils.primaryBtn("+ Start a New Borrow Request");
        newBorrow.addActionListener(e -> navigateBorrower("borrows"));

        JButton history = UIUtils.secondaryBtn("See My History");
        history.addActionListener(e -> navigateBorrower("profile"));

        btnRow.add(newBorrow);
        btnRow.add(history);

        wrap.add(top, BorderLayout.NORTH);
        wrap.add(btnRow, BorderLayout.SOUTH);
        return wrap;
    }

    private void navigateBorrower(String navId) {
        if (borrowerNavigate != null) {
            borrowerNavigate.accept(navId);
        }
    }

    private void requestBorrowForItem(ItemRecord item) {
        if (borrowerOpenBorrowRequest != null) {
            borrowerOpenBorrowRequest.accept(item);
        }
    }

    private JPanel buildBorrowerEquipmentBrowser(List<ItemRecord> allItems) {
        JPanel shell = UIUtils.elevatedCard(new BorderLayout(0, 14));
        shell.setBorder(new EmptyBorder(16, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Available Equipment");
        title.setFont(UIUtils.F_BOLD);
        title.setForeground(UIColors.TEXT);
        JLabel sub = UIUtils.muted("Names of items you may borrow — inventory marked available.");
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);

        JTextField searchF = UIUtils.field("Search by name, ID, or description…");

        String[] typeLabels = {"All Types", "Cables & accessories", "Computing", "Peripherals", "Tools"};
        String[] typeValues = {null, "accessory", "equipment", "peripheral", "tool"};
        JComboBox<String> typeCombo = UIUtils.combo(typeLabels);

        JPanel filterBar = new JPanel(new BorderLayout(10, 8));
        filterBar.setOpaque(false);
        JPanel searchGrow = new JPanel(new BorderLayout(0, 0));
        searchGrow.setOpaque(false);
        searchGrow.add(searchF, BorderLayout.CENTER);
        JPanel typePick = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        typePick.setOpaque(false);
        JLabel fil = new JLabel("Filter by:");
        fil.setFont(UIUtils.F_LABEL);
        fil.setForeground(UIColors.TEXT_SECONDARY);
        typePick.add(fil);
        typePick.add(typeCombo);
        filterBar.add(searchGrow, BorderLayout.CENTER);
        filterBar.add(typePick, BorderLayout.EAST);

        JPanel listHost = new JPanel();
        listHost.setLayout(new BoxLayout(listHost, BoxLayout.PAGE_AXIS));
        listHost.setOpaque(false);

        Runnable rebuild = () -> {
            listHost.removeAll();
            String q = searchF.getText().trim().toLowerCase(Locale.ROOT);
            int ti = typeCombo.getSelectedIndex();
            String typeVal = ti >= 0 && ti < typeValues.length ? typeValues[ti] : null;

            List<ItemRecord> filtered = allItems.stream()
                    .filter(DashboardPanel::isBorrowerCatalogItem)
                    .filter(i -> typeVal == null || typeVal.equalsIgnoreCase(i.getItemType()))
                    .filter(i -> matchesBorrowerItemSearch(i, q))
                    .sorted(borrowerCatalogOrder())
                    .toList();

            if (filtered.isEmpty()) {
                JLabel empty = UIUtils.muted("No items match your filters.");
                empty.setBorder(new EmptyBorder(24, 12, 24, 12));
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                listHost.add(empty);
            } else {
                for (ItemRecord i : filtered) {
                    JPanel row = borrowerEquipmentListRow(i);
                    row.setAlignmentX(Component.LEFT_ALIGNMENT);
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
                    listHost.add(row);
                    listHost.add(Box.createVerticalStrut(10));
                }
            }
            listHost.revalidate();
            listHost.repaint();
        };

        searchF.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { rebuild.run(); }
            @Override public void removeUpdate(DocumentEvent e) { rebuild.run(); }
            @Override public void changedUpdate(DocumentEvent e) { rebuild.run(); }
        });
        typeCombo.addActionListener(e -> rebuild.run());

        JScrollPane sp = UIUtils.scrollPane(listHost);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.setPreferredSize(new Dimension(0, 420));

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(filterBar, BorderLayout.NORTH);
        body.add(sp, BorderLayout.CENTER);

        shell.add(header, BorderLayout.NORTH);
        shell.add(body, BorderLayout.CENTER);

        rebuild.run();
        return shell;
    }

    /** Inventory rows the borrower can request: on shelf and not in maintenance. */
    private static boolean isBorrowerCatalogItem(ItemRecord i) {
        if (!"available".equals(i.getAvailabilityStatus())) {
            return false;
        }
        return !"under maintenance".equalsIgnoreCase(i.getConditionStatus());
    }

    private static boolean matchesBorrowerItemSearch(ItemRecord i, String q) {
        if (q.isEmpty()) {
            return true;
        }
        return (i.getItemName() != null && i.getItemName().toLowerCase(Locale.ROOT).contains(q))
                || (i.getItemId() != null && i.getItemId().toLowerCase(Locale.ROOT).contains(q))
                || (i.getDescription() != null && i.getDescription().toLowerCase(Locale.ROOT).contains(q))
                || (i.getModel() != null && i.getModel().toLowerCase(Locale.ROOT).contains(q));
    }

    private static Comparator<ItemRecord> borrowerCatalogOrder() {
        return Comparator.comparing(ItemRecord::getItemName, String.CASE_INSENSITIVE_ORDER);
    }

    /** Full-width row: title/description + Request → Borrow Request panel + create dialog. */
    private JPanel borrowerEquipmentListRow(ItemRecord item) {
        JPanel row = UIUtils.card(new BorderLayout(16, 0));
        row.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel name = new JLabel(item.getItemName());
        name.setFont(UIUtils.F_BOLD);
        name.setForeground(UIColors.TEXT);

        String desc = item.getDescription();
        if (desc == null || desc.isBlank() || "—".equals(desc)) {
            desc = item.getModel() != null && !item.getModel().isBlank() && !"—".equals(item.getModel())
                    ? item.getModel()
                    : "No description.";
        }
        JLabel descL = new JLabel(desc.length() > 120 ? desc.substring(0, 118) + "…" : desc);
        descL.setFont(UIUtils.F_SMALL);
        descL.setForeground(UIColors.TEXT_SECONDARY);

        JPanel leftBlock = new JPanel(new GridLayout(2, 1, 2, 0));
        leftBlock.setOpaque(false);
        leftBlock.add(name);
        leftBlock.add(descL);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        east.setOpaque(false);
        JButton request = UIUtils.primaryBtn("Request");
        request.addActionListener(e -> requestBorrowForItem(item));
        east.add(request);

        row.add(leftBlock, BorderLayout.CENTER);
        row.add(east, BorderLayout.EAST);
        return row;
    }

    private JPanel buildBorrowerRemindersPanel(List<BorrowRecord> borrows) {
        JPanel card = UIUtils.elevatedCard(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel h = new JLabel("Upcoming Return Reminders");
        h.setFont(UIUtils.F_BOLD);
        h.setForeground(UIColors.TEXT);
        JLabel hint = UIUtils.muted("Items you still have on loan.");
        JPanel head = new JPanel(new BorderLayout(0, 4));
        head.setOpaque(false);
        head.add(h, BorderLayout.NORTH);
        head.add(hint, BorderLayout.SOUTH);

        List<BorrowRecord> mine = borrows.stream()
                .filter(b -> b.getBorrowerId() == user.getUserId())
                .filter(b -> "borrowed".equals(b.getStatus()))
                .sorted(Comparator.comparing(BorrowRecord::getDateBorrowed, Comparator.nullsLast(String::compareTo)))
                .limit(12)
                .toList();

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.PAGE_AXIS));
        list.setOpaque(false);

        if (mine.isEmpty()) {
            list.add(UIUtils.muted("No active borrows."));
        } else {
            for (int i = 0; i < mine.size(); i++) {
                BorrowRecord b = mine.get(i);
                String main = b.getItemName() == null || b.getItemName().isBlank() ? "Borrow " + b.getBorrowId() : b.getItemName();
                String sub = (b.getDateBorrowed() != null ? b.getDateBorrowed() : "—")
                        + " · " + (b.getActivityName() != null ? b.getActivityName() : "");

                JLabel m = new JLabel(main.length() > 42 ? main.substring(0, 40) + "…" : main);
                m.setFont(UIUtils.F_BODY);
                m.setForeground(UIColors.TEXT);
                m.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel s = new JLabel(sub.length() > 48 ? sub.substring(0, 46) + "…" : sub);
                s.setFont(UIUtils.F_SMALL);
                s.setForeground(UIColors.TEXT_MUTED);
                s.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel info = new JPanel();
                info.setLayout(new BoxLayout(info, BoxLayout.PAGE_AXIS));
                info.setOpaque(false);
                info.add(m);
                info.add(Box.createVerticalStrut(2));
                info.add(s);

                JPanel textCol = new JPanel(new BorderLayout());
                textCol.setOpaque(false);
                textCol.add(info, BorderLayout.NORTH);

                JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                badgeWrap.setOpaque(false);
                badgeWrap.add(UIUtils.badge("borrowed"));

                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setOpaque(true);
                row.setBackground(UIColors.TABLE_ALT_ROW);
                row.setBorder(new EmptyBorder(6, 10, 6, 10));
                row.add(textCol, BorderLayout.CENTER);
                row.add(badgeWrap, BorderLayout.EAST);

                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                int rh = row.getPreferredSize().height;
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rh));
                list.add(row);
                if (i < mine.size() - 1) {
                    list.add(Box.createVerticalStrut(6));
                }
            }
        }

        JScrollPane sp = UIUtils.scrollPane(list);
        sp.setBorder(null);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setPreferredSize(new Dimension(280, 360));

        card.add(head, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel statCard(String title, String value, Color color, String symbol, String tooltip) {
        JPanel card = UIUtils.elevatedCard(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 18, 30, 18));
        card.setToolTipText(tooltip);

        JLabel sym = new JLabel(symbol, SwingConstants.CENTER);
        sym.setFont(new Font("Segoe UI Symbol", Font.BOLD, STAT_TILE_SYMBOL_PT));
        sym.setForeground(Color.WHITE);
        sym.setToolTipText(tooltip);

        JPanel iconBox = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        iconBox.setOpaque(false);
        Dimension iconDim = new Dimension(STAT_TILE_ICON, STAT_TILE_ICON);
        iconBox.setPreferredSize(iconDim);
        iconBox.setMinimumSize(iconDim);
        iconBox.setMaximumSize(iconDim);
        iconBox.add(sym);

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        GridBagConstraints igc = new GridBagConstraints();
        igc.gridx = 0;
        igc.gridy = 0;
        igc.anchor = GridBagConstraints.CENTER;
        igc.weightx = 0;
        igc.weighty = 0;
        iconWrap.add(iconBox, igc);

        JLabel titleL = new JLabel("<html><div style='text-align:center'>" + escapeHtml(title) + "</div></html>");
        titleL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleL.setForeground(UIColors.TEXT_SECONDARY);

        JLabel valL = new JLabel(value);
        valL.setFont(new Font("Segoe UI", Font.BOLD, STAT_TILE_VALUE_PT));
        valL.setForeground(color);
        valL.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.PAGE_AXIS));
        iconWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(iconWrap);
        body.add(Box.createVerticalStrut(12));
        titleL.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(titleL);
        body.add(Box.createVerticalStrut(10));
        valL.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(valL);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Recent activity/borrow lists. When {@code fillViewportHeight} is true, lists expand with the dashboard
     * (admin / custodian); otherwise a fixed scroll height is used.
     */
    private JPanel recentList(String title, String subtitleHint, List<String[]> rows, boolean fillViewportHeight) {
        JPanel card = UIUtils.elevatedCard(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 16, 16));

        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setOpaque(false);
        JLabel h = new JLabel(title);
        h.setFont(UIUtils.F_BOLD);
        h.setForeground(UIColors.TEXT);
        JLabel hint = UIUtils.muted(subtitleHint);
        hint.setHorizontalAlignment(SwingConstants.TRAILING);
        header.add(h, BorderLayout.WEST);
        header.add(hint, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.PAGE_AXIS));
        list.setOpaque(false);
        if (rows.isEmpty()) {
            JPanel wrap = new JPanel(new GridBagLayout());
            wrap.setOpaque(false);
            JLabel empty = UIUtils.muted("No records to show.");
            wrap.add(empty);
            list.add(wrap);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                String[] row = rows.get(i);
                JPanel r = recentRow(row[0], row[1], row[2], i % 2 == 1);
                r.setAlignmentX(Component.LEFT_ALIGNMENT);
                int hgt = r.getPreferredSize().height;
                r.setMaximumSize(new Dimension(Integer.MAX_VALUE, hgt));
                list.add(r);
                if (i < rows.size() - 1) {
                    list.add(Box.createVerticalStrut(2));
                }
            }
        }

        JScrollPane sp = UIUtils.scrollPane(list);
        if (fillViewportHeight) {
            sp.setMinimumSize(new Dimension(0, 120));
        } else {
            sp.setPreferredSize(new Dimension(0, RECENT_LIST_SCROLL_HEIGHT));
            sp.setMinimumSize(new Dimension(0, 120));
        }
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getAccessibleContext().setAccessibleName(title + " — " + subtitleHint);
        sp.getAccessibleContext().setAccessibleDescription(
                "Use mouse wheel or scrollbar to browse entries.");
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JPanel recentRow(String main, String sub, String status, boolean altStripe) {
        JPanel p = new JPanel(new BorderLayout(8, 0)) {
            @Override protected void paintComponent(Graphics g) {
                if (altStripe) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(UIColors.TABLE_ALT_ROW);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 8, 6, 8));

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setOpaque(false);
        JLabel m = new JLabel(main.length() > 38 ? main.substring(0, 36) + "…" : main);
        m.setFont(UIUtils.F_BODY);
        m.setForeground(UIColors.TEXT);
        JLabel s = new JLabel(sub.length() > 30 ? sub.substring(0, 28) + "…" : sub);
        s.setFont(UIUtils.F_SMALL);
        s.setForeground(UIColors.TEXT_MUTED);
        info.add(m);
        info.add(s);

        JLabel badge = UIUtils.badge(status);
        p.add(info, BorderLayout.CENTER);
        p.add(badge, BorderLayout.EAST);
        return p;
    }

    private JPanel equipmentBar(List<ItemRecord> items) {
        JPanel card = UIUtils.card(new BorderLayout(0, 10));
        JLabel h = new JLabel("Equipment Overview");
        h.setFont(UIUtils.F_BOLD);
        h.setForeground(UIColors.TEXT);
        card.add(h, BorderLayout.NORTH);

        int total = items.size();
        JPanel bars = new JPanel(new GridLayout(4, 1, 0, 8));
        bars.setOpaque(false);
        bars.setBorder(new EmptyBorder(4, 0, 0, 0));
        String[][] rows = {
            {"Available",       String.valueOf(items.stream().filter(i -> "available".equals(i.getAvailabilityStatus())).count()),  "#16A34A"},
            {"Borrowed",        String.valueOf(items.stream().filter(i -> "borrowed".equals(i.getAvailabilityStatus())).count()),   "#2563EB"},
            {"Unavailable",     String.valueOf(items.stream().filter(i -> "unavailable".equals(i.getAvailabilityStatus())).count()),  "#64748B"},
            {"Damaged/Maint.",  String.valueOf(items.stream().filter(i -> !"working".equals(i.getConditionStatus())).count()),       "#DC2626"},
        };
        for (String[] r : rows) {
            int cnt = Integer.parseInt(r[1]);
            int pct = total == 0 ? 0 : (int)((cnt * 100.0) / total);
            bars.add(barRow(r[0], cnt, pct, Color.decode(r[2])));
        }
        card.add(bars, BorderLayout.CENTER);
        return card;
    }

    private JPanel barRow(String label, int count, int pct, Color color) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(UIColors.TEXT_SECONDARY);
        JLabel cnt = new JLabel(count + "  (" + pct + "%)");
        cnt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cnt.setForeground(UIColors.TEXT_MUTED);
        header.add(l, BorderLayout.WEST);
        header.add(cnt, BorderLayout.EAST);
        JPanel track = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                int filled = (int)((pct / 100.0) * getWidth());
                g2.setColor(color);
                g2.fillRoundRect(0, 0, Math.max(filled, 4), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        track.setPreferredSize(new Dimension(0, 8));
        p.add(header, BorderLayout.NORTH);
        p.add(track, BorderLayout.SOUTH);
        return p;
    }

    private JPanel alertsCard(List<ItemRecord> items) {
        JPanel card = UIUtils.card(new BorderLayout(0, 8));
        JLabel h = new JLabel("⚠  Attention Required");
        h.setFont(UIUtils.F_BOLD);
        h.setForeground(UIColors.WARNING);
        card.add(h, BorderLayout.NORTH);

        JPanel list = new JPanel(new GridLayout(0, 1, 0, 4));
        list.setOpaque(false);
        items.stream().filter(i -> !"working".equals(i.getConditionStatus())).limit(5).forEach(i -> {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(new Color(254, 243, 199));
            row.setBorder(new EmptyBorder(6, 10, 6, 10));
            JPanel info = new JPanel(new GridLayout(2, 1));
            info.setOpaque(false);
            JLabel n = new JLabel(i.getItemName());
            n.setFont(UIUtils.F_BOLD);
            n.setForeground(UIColors.TEXT);
            JLabel id = new JLabel(i.getItemId());
            id.setFont(UIUtils.F_SMALL);
            id.setForeground(UIColors.TEXT_MUTED);
            info.add(n); info.add(id);
            JLabel badge = UIUtils.badge(i.getConditionStatus());
            row.add(info, BorderLayout.CENTER);
            row.add(badge, BorderLayout.EAST);
            list.add(row);
        });
        card.add(new JScrollPane(list) {{ setBorder(null); getViewport().setBackground(Color.WHITE); }}, BorderLayout.CENTER);
        return card;
    }

    // ── Safe data loaders ─────────────────────────────────────────────────────
    private List<ItemRecord>     safeItems()      { try { return demo ? MockDataProvider.items()      : db.getAllItems();            } catch (Exception e) { return MockDataProvider.items();      } }
    private List<ActivityRecord> safeActivities() { try { return demo ? MockDataProvider.activities() : db.getAllActivities();       } catch (Exception e) { return MockDataProvider.activities(); } }
    private List<BorrowRecord>   safeBorrows()    { try { return demo ? MockDataProvider.borrows()    : db.getAllBorrowRecords();    } catch (Exception e) { return MockDataProvider.borrows();    } }
}
