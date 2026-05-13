package jdbc.demo.ui.panels;

import jdbc.demo.Database;
import jdbc.demo.model.*;
import jdbc.demo.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ReportsPanel extends JPanel {

    private record ReportDef(int id, String name, String category, String[] paramLabels, String[] paramTypes) {}

    private static final ReportDef[] REPORTS = {
            new ReportDef(1,  "List all registered users",                   "Basic",       new String[]{},                        new String[]{}),
            new ReportDef(2,  "View all items in the inventory",             "Basic",       new String[]{},                        new String[]{}),
            new ReportDef(3,  "List all available facility rooms",           "Basic",       new String[]{},                        new String[]{}),
            new ReportDef(4,  "Find equipment by availability status",       "Basic",       new String[]{"Availability Status"},   new String[]{"combo:available,borrowed,unavailable"}),
            new ReportDef(5,  "Search for specific userId",                  "Basic",       new String[]{"User ID"},               new String[]{"text"}),
            new ReportDef(6,  "Find items by condition status",              "Basic",       new String[]{"Condition Status"},      new String[]{"combo:working,damaged,under maintenance"}),
            new ReportDef(7,  "Activities scheduled on a date",              "Basic",       new String[]{"Activity Date"},         new String[]{"date"}),
            new ReportDef(8,  "Filter items by type",                        "Basic",       new String[]{"Item Type"},             new String[]{"combo:tool,accessory,peripheral,equipment"}),
            new ReportDef(9,  "Borrowed items and borrower names",           "Equipment",   new String[]{},                        new String[]{}),
            new ReportDef(10, "Items never borrowed",                        "Equipment",   new String[]{},                        new String[]{}),
            new ReportDef(11, "Last person to handle a specific item",       "Equipment",   new String[]{"Item ID"},               new String[]{"text"}),
            new ReportDef(12, "Borrower history on a specific date",         "Equipment",   new String[]{"Date Borrowed"},         new String[]{"date"}),
            new ReportDef(13, "Activities and assigned labs",                "Facility",    new String[]{},                        new String[]{}),
            new ReportDef(14, "Facilities used for a specific activity type","Facility",    new String[]{"Activity Type"},         new String[]{"text"}),
            new ReportDef(15, "Facility hosting a specific activity",        "Facility",    new String[]{"Activity ID"},           new String[]{"text"}),
            new ReportDef(16, "Facilities with no activities yet",           "Facility",    new String[]{},                        new String[]{}),
            new ReportDef(17, "Activities and requesters",                   "Workflow",    new String[]{},                        new String[]{}),
            new ReportDef(18, "Activities and approver",                     "Workflow",    new String[]{},                        new String[]{}),
            new ReportDef(19, "Activities requested by specific user type",  "Workflow",    new String[]{"User Type"},             new String[]{"combo:Student,Professor,Custodian"}),
            new ReportDef(20, "Borrow transactions and custodian",           "Workflow",    new String[]{},                        new String[]{}),
            new ReportDef(21, "Borrowers with incident remarks",             "Analytics",   new String[]{},                        new String[]{}),
            new ReportDef(22, "Count items held per student",                "Analytics",   new String[]{},                        new String[]{}),
            new ReportDef(23, "Most frequently borrowed item",               "Analytics",   new String[]{},                        new String[]{}),
            new ReportDef(24, "Users with no borrow transactions",           "Analytics",   new String[]{},                        new String[]{}),
            new ReportDef(25, "Items out for a specific activity type",      "Analytics",   new String[]{"Activity Type"},         new String[]{"text"}),
            new ReportDef(26, "Borrow log (filter by date range/borrower)",  "Summary",     new String[]{"Borrower Last Name","Start Date","End Date"}, new String[]{"text","date","date"}),
            new ReportDef(27, "Unreturned items and borrower contact",       "Summary",     new String[]{},                        new String[]{}),
            new ReportDef(28, "Items used in a specific facility",           "Summary",     new String[]{"Facility Name"},         new String[]{"text"}),
            new ReportDef(29, "Count activities approved per custodian",     "Summary",     new String[]{},                        new String[]{}),
            new ReportDef(30, "Item count per itemType",                     "Summary",     new String[]{},                        new String[]{}),
    };

    private static final Map<String, Color> CAT_COLORS = new LinkedHashMap<>();
    static {
        CAT_COLORS.put("Basic",     new Color(37,  99,  235));
        CAT_COLORS.put("Equipment", new Color(22,  163,  74));
        CAT_COLORS.put("Facility",  new Color(8,   145, 178));
        CAT_COLORS.put("Workflow",  new Color(126,  34, 206));
        CAT_COLORS.put("Analytics", new Color(217, 119,  6));
        CAT_COLORS.put("Summary",   new Color(100, 116, 139));
    }

    private final Database     db;
    private final boolean      demo;

    private ReportDef          activeReport;
    private final Map<String, JComponent> paramFields = new LinkedHashMap<>();

    private JPanel             reportListPanel;
    private JPanel             runPanel;
    private JPanel             resultPanel;
    private DefaultTableModel  resultModel;
    private JTable             resultTable;
    private JLabel             rowCountLabel;

    public ReportsPanel(Object user, Database db, boolean demo) {
        super(new BorderLayout(12, 0));
        this.db   = db;
        this.demo = demo;
        setBackground(UIColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        build();
    }

    private void build() {
        // Left: report list
        reportListPanel = buildReportList();
        JScrollPane listScroll = UIUtils.scrollPane(reportListPanel);
        listScroll.setPreferredSize(new Dimension(260, 0));
        listScroll.setBorder(new LineBorder(UIColors.BORDER, 1));
        listScroll.getViewport().setBackground(Color.WHITE);

        // Right: run + results
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setBackground(UIColors.BG);
        runPanel    = buildRunPanel();
        resultPanel = buildResultPanel();
        right.add(runPanel,    BorderLayout.NORTH);
        right.add(resultPanel, BorderLayout.CENTER);

        add(listScroll, BorderLayout.WEST);
        add(right,      BorderLayout.CENTER);
    }

    private JPanel buildReportList() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(Color.WHITE);

        JTextField search = UIUtils.field("Search reports…");
        search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        search.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIColors.BORDER, 0, false) { @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { g.setColor(UIColors.BORDER); g.drawLine(x, y + h - 1, x + w, y + h - 1); } },
                new EmptyBorder(6, 10, 6, 10)));
        outer.add(search);

        // Group by category
        Map<String, List<ReportDef>> byCategory = new LinkedHashMap<>();
        for (ReportDef r : REPORTS) byCategory.computeIfAbsent(r.category(), k -> new ArrayList<>()).add(r);

        ButtonGroup grp = new ButtonGroup();

        byCategory.forEach((cat, reports) -> {
            JLabel catLabel = new JLabel("  " + cat.toUpperCase());
            catLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            catLabel.setForeground(CAT_COLORS.getOrDefault(cat, UIColors.TEXT_SECONDARY));
            catLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
            catLabel.setOpaque(true);
            catLabel.setBackground(new Color(248, 250, 252));
            catLabel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIColors.BORDER, 0, false) {
                        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                            g.setColor(UIColors.BORDER); g.drawLine(x, y, x + w, y);
                        }
                    },
                    new EmptyBorder(5, 8, 5, 8)));
            outer.add(catLabel);

            for (ReportDef r : reports) {
                JToggleButton btn = new JToggleButton(r.id() + ".  " + r.name()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        if (isSelected()) g2.setColor(UIColors.TABLE_SELECT);
                        else if (getModel().isRollover()) g2.setColor(new Color(248, 250, 252));
                        else g2.setColor(Color.WHITE);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btn.setForeground(UIColors.TEXT);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setHorizontalAlignment(SwingConstants.LEFT);
                btn.setBorder(new EmptyBorder(7, 12, 7, 12));
                btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                grp.add(btn);
                btn.addActionListener(e -> selectReport(r));
                outer.add(btn);
            }
        });

        // Search filter
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void update() {
                String q = search.getText().toLowerCase();
                for (Component c : outer.getComponents()) {
                    if (c instanceof JToggleButton tb) {
                        tb.setVisible(q.isEmpty() || tb.getText().toLowerCase().contains(q));
                    }
                }
                outer.revalidate(); outer.repaint();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        return outer;
    }

    private JPanel buildRunPanel() {
        JPanel card = UIUtils.card(new BorderLayout(0, 12));
        // No fixed height — panel grows to fit header + param fields + run button

        JLabel hint = UIUtils.muted("← Select a report from the list to run it.");
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(hint, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildResultPanel() {
        JPanel card = UIUtils.card(new BorderLayout(0, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel h = new JLabel("Results");
        h.setFont(UIUtils.F_BOLD); h.setForeground(UIColors.TEXT);
        rowCountLabel = UIUtils.muted("");
        header.add(h, BorderLayout.WEST); header.add(rowCountLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        resultModel = new DefaultTableModel() { @Override public boolean isCellEditable(int r, int c) { return false; } };
        resultTable = new JTable(resultModel);
        UIUtils.styleTable(resultTable);
        card.add(UIUtils.scrollPane(resultTable), BorderLayout.CENTER);
        return card;
    }

    private void selectReport(ReportDef r) {
        this.activeReport = r;
        paramFields.clear();
        runPanel.removeAll();

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(4, 4, 4, 4));

        // Header
        JPanel hdr = new JPanel(new BorderLayout(10, 0));
        hdr.setOpaque(false);
        JPanel badge = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CAT_COLORS.getOrDefault(r.category(), UIColors.PRIMARY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        badge.setOpaque(false); badge.setPreferredSize(new Dimension(40, 40));
        JLabel num = new JLabel(String.valueOf(r.id())); num.setFont(UIUtils.F_BOLD); num.setForeground(Color.WHITE);
        badge.add(num);
        JPanel info = new JPanel(new GridLayout(2, 1)); info.setOpaque(false);
        JLabel nm = new JLabel("Report #" + r.id() + ":  " + r.name()); nm.setFont(UIUtils.F_BOLD); nm.setForeground(UIColors.TEXT);
        JLabel cat = UIUtils.badge(r.category()); cat.setBackground(new Color(CAT_COLORS.getOrDefault(r.category(), UIColors.PRIMARY).getRed(), CAT_COLORS.getOrDefault(r.category(), UIColors.PRIMARY).getGreen(), CAT_COLORS.getOrDefault(r.category(), UIColors.PRIMARY).getBlue(), 30)); cat.setForeground(CAT_COLORS.getOrDefault(r.category(), UIColors.PRIMARY));
        info.add(nm); info.add(cat);
        hdr.add(badge, BorderLayout.WEST); hdr.add(info, BorderLayout.CENTER);
        content.add(hdr, BorderLayout.NORTH);

        // Params
        if (r.paramLabels().length > 0) {
            JPanel params = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
            params.setOpaque(false);
            params.setBorder(new EmptyBorder(4, 0, 4, 0));
            for (int i = 0; i < r.paramLabels().length; i++) {
                JLabel lbl = UIUtils.sectionLabel(r.paramLabels()[i]);
                JComponent field;
                if (r.paramTypes()[i].startsWith("combo:")) {
                    String[] opts = r.paramTypes()[i].substring(6).split(",");
                    JComboBox<String> cb = UIUtils.combo(opts);
                    cb.setPreferredSize(new Dimension(180, 34));
                    field = cb;
                } else if ("date".equals(r.paramTypes()[i])) {
                    JTextField tf = UIUtils.field("YYYY-MM-DD");
                    tf.setPreferredSize(new Dimension(150, 34));
                    field = tf;
                } else {
                    JTextField tf = UIUtils.field("Enter value…");
                    tf.setPreferredSize(new Dimension(180, 34));
                    field = tf;
                }
                paramFields.put(r.paramLabels()[i], field);
                // Each param is a vertical pair: label on top, field below
                JPanel col = new JPanel();
                col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
                col.setOpaque(false);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                field.setAlignmentX(Component.LEFT_ALIGNMENT);
                col.add(lbl);
                col.add(Box.createVerticalStrut(4));
                col.add(field);
                params.add(col);
            }
            content.add(params, BorderLayout.CENTER);
        }

        // Run button
        JButton runBtn = UIUtils.primaryBtn("▶  Run Report");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.add(runBtn);
        content.add(footer, BorderLayout.SOUTH);
        runBtn.addActionListener(e -> runReport(r));

        runPanel.add(content, BorderLayout.CENTER);
        runPanel.revalidate(); runPanel.repaint();
        // Clear results
        resultModel.setColumnCount(0); resultModel.setRowCount(0);
        rowCountLabel.setText("");
    }

    private void runReport(ReportDef r) {
        List<String> params = new ArrayList<>();
        for (JComponent comp : paramFields.values()) {
            if (comp instanceof JComboBox<?> cb) params.add((String) cb.getSelectedItem());
            else if (comp instanceof JTextField tf) params.add(tf.getText().trim());
        }

        try {
            QueryResult qr;
            if (demo) {
                qr = generateMockResult(r.id(), params);
            } else {
                qr = db.executeReport(r.id(), params);
            }
            // Display
            resultModel.setColumnCount(0);
            resultModel.setRowCount(0);
            for (String col : qr.getColumns()) resultModel.addColumn(col);
            for (List<String> row : qr.getRows()) resultModel.addRow(row.toArray());
            UIUtils.setDefaultRenderer(resultTable);
            rowCountLabel.setText(qr.getRows().size() + " row" + (qr.getRows().size() != 1 ? "s" : "") + " returned");
        } catch (Exception ex) { UIUtils.error(this, "Report error: " + ex.getMessage()); }
    }

    private QueryResult generateMockResult(int id, List<String> params) {
        List<UserRecord>     users = MockDataProvider.users();
        List<ItemRecord>     items = MockDataProvider.items();
        List<FacilityRecord> facs  = MockDataProvider.facilities();
        List<ActivityRecord> acts  = MockDataProvider.activities();
        List<BorrowRecord>   bors  = MockDataProvider.borrows();

        return switch (id) {
            case 1  -> qr(List.of("UserID","Name","Email","Type"), users.stream().map(u -> List.of(String.valueOf(u.getUserId()), u.getFirstName()+" "+u.getLastName(), u.getEmail(), u.getType())).toList());
            case 2  -> qr(List.of("ItemID","Name","Type","Condition","Availability"), items.stream().map(i -> List.of(i.getItemId(), i.getItemName(), i.getItemType(), i.getConditionStatus(), i.getAvailabilityStatus())).toList());
            case 3  -> qr(List.of("FacilityID","Facility Name"), facs.stream().map(f -> List.of(f.getFacilityId(), f.getFacilityName())).toList());
            case 4  -> { String p = params.isEmpty() ? "" : params.get(0); yield qr(List.of("ItemID","Name","Availability"), items.stream().filter(i -> p.isEmpty() || i.getAvailabilityStatus().equals(p)).map(i -> List.of(i.getItemId(), i.getItemName(), i.getAvailabilityStatus())).toList()); }
            case 5  -> { String p = params.isEmpty() ? "" : params.get(0); yield qr(List.of("UserID","Name","Type"), users.stream().filter(u -> p.isEmpty() || String.valueOf(u.getUserId()).equals(p)).map(u -> List.of(String.valueOf(u.getUserId()), u.getFirstName()+" "+u.getLastName(), u.getType())).toList()); }
            case 6  -> { String p = params.isEmpty() ? "" : params.get(0); yield qr(List.of("ItemID","Name","Condition"), items.stream().filter(i -> p.isEmpty() || i.getConditionStatus().equals(p)).map(i -> List.of(i.getItemId(), i.getItemName(), i.getConditionStatus())).toList()); }
            case 7  -> { String p = params.isEmpty() ? "" : params.get(0); yield qr(List.of("ActivityID","Name","Date","Status"), acts.stream().filter(a -> p.isEmpty() || a.getActivityDate().equals(p)).map(a -> List.of(a.getActivityId(), a.getActivityName(), a.getActivityDate(), a.getStatus())).toList()); }
            case 8  -> { String p = params.isEmpty() ? "" : params.get(0); yield qr(List.of("ItemID","Name","Type"), items.stream().filter(i -> p.isEmpty() || i.getItemType().equals(p)).map(i -> List.of(i.getItemId(), i.getItemName(), i.getItemType())).toList()); }
            case 9  -> qr(List.of("BorrowID","Item(s)","Borrower"), bors.stream().filter(b -> "borrowed".equals(b.getStatus())).map(b -> List.of(b.getBorrowId(), b.getItemName()==null?"—":b.getItemName(), b.getBorrowerName())).toList());
            case 10 -> { Set<String> borrowed = new HashSet<>(); bors.forEach(b -> { if (b.getItemName()!=null) Arrays.stream(b.getItemName().split(", ")).forEach(borrowed::add); }); yield qr(List.of("ItemID","Name","Type"), items.stream().filter(i -> !borrowed.contains(i.getItemName())).map(i -> List.of(i.getItemId(), i.getItemName(), i.getItemType())).toList()); }
            case 11 -> { String p = params.isEmpty()?"":params.get(0); yield qr(List.of("BorrowID","Borrower","Date"), bors.stream().filter(b -> b.getItemName()!=null && b.getItemName().toLowerCase().contains(p.toLowerCase())).map(b -> List.of(b.getBorrowId(), b.getBorrowerName(), b.getDateBorrowed()==null?"—":b.getDateBorrowed())).toList()); }
            case 12 -> { String p = params.isEmpty()?"":params.get(0); yield qr(List.of("BorrowID","Borrower","Date","Status"), bors.stream().filter(b -> p.isEmpty() || p.equals(b.getDateBorrowed())).map(b -> List.of(b.getBorrowId(), b.getBorrowerName(), b.getDateBorrowed()==null?"—":b.getDateBorrowed(), b.getStatus())).toList()); }
            case 13 -> qr(List.of("ActivityID","Activity Name","Facility"), acts.stream().filter(a -> a.getFacilityName()!=null).map(a -> List.of(a.getActivityId(), a.getActivityName(), a.getFacilityName())).toList());
            case 16 -> { Set<String> used = new HashSet<>(); acts.forEach(a -> { if (a.getFacilityName()!=null) used.add(a.getFacilityName()); }); yield qr(List.of("FacilityID","Facility Name"), facs.stream().filter(f -> !used.contains(f.getFacilityName())).map(f -> List.of(f.getFacilityId(), f.getFacilityName())).toList()); }
            case 17 -> qr(List.of("ActivityID","Activity Name","Requester","Status"), acts.stream().map(a -> List.of(a.getActivityId(), a.getActivityName(), a.getRequesterName(), a.getStatus())).toList());
            case 18 -> qr(List.of("ActivityID","Activity Name","Approver","Status"), acts.stream().filter(a -> a.getApprovedByName()!=null).map(a -> List.of(a.getActivityId(), a.getActivityName(), a.getApprovedByName(), a.getStatus())).toList());
            case 20 -> qr(List.of("BorrowID","Borrower","Custodian","Status"), bors.stream().map(b -> List.of(b.getBorrowId(), b.getBorrowerName(), b.getCustodianName(), b.getStatus())).toList());
            case 21 -> qr(List.of("BorrowID","Borrower","Status","Remarks"), bors.stream().filter(b -> b.getRemarks()!=null && !b.getRemarks().isBlank()).map(b -> List.of(b.getBorrowId(), b.getBorrowerName(), b.getStatus(), b.getRemarks())).toList());
            case 23 -> { Map<String,Long> freq = new HashMap<>(); bors.forEach(b -> { if(b.getItemName()!=null) Arrays.stream(b.getItemName().split(", ")).forEach(i -> freq.merge(i,1L,Long::sum)); }); yield qr(List.of("Item Name","Count"), freq.entrySet().stream().sorted((a,b2) -> Long.compare(b2.getValue(),a.getValue())).map(e -> List.of(e.getKey(), String.valueOf(e.getValue()))).toList()); }
            case 24 -> { Set<String> hb = new HashSet<>(); bors.forEach(b -> hb.add(b.getBorrowerName())); yield qr(List.of("UserID","Name","Type"), users.stream().filter(u -> !hb.contains(u.getFirstName()+" "+u.getLastName())).map(u -> List.of(String.valueOf(u.getUserId()), u.getFirstName()+" "+u.getLastName(), u.getType())).toList()); }
            case 27 -> qr(List.of("BorrowID","Borrower","Item(s)","Date"), bors.stream().filter(b -> "borrowed".equals(b.getStatus())).map(b -> List.of(b.getBorrowId(), b.getBorrowerName(), b.getItemName()==null?"—":b.getItemName(), b.getDateBorrowed()==null?"—":b.getDateBorrowed())).toList());
            case 29 -> { Map<String,Long> cnt = new HashMap<>(); acts.stream().filter(a -> "Approved".equals(a.getStatus()) && a.getApprovedByName()!=null).forEach(a -> cnt.merge(a.getApprovedByName(),1L,Long::sum)); yield qr(List.of("Custodian","Approved Count"), cnt.entrySet().stream().map(e -> List.of(e.getKey(), String.valueOf(e.getValue()))).toList()); }
            case 30 -> { Map<String,Long> cnt = new HashMap<>(); items.forEach(i -> cnt.merge(i.getItemType(),1L,Long::sum)); yield qr(List.of("Item Type","Count"), cnt.entrySet().stream().map(e -> List.of(e.getKey(), String.valueOf(e.getValue()))).toList()); }
            default -> qr(List.of("Info"), List.of(List.of("Report #" + id + " result (connect DB for live data)")));
        };
    }

    private static QueryResult qr(List<String> cols, List<List<String>> rows) {
        return new QueryResult(new ArrayList<>(cols), rows.stream().map(ArrayList::new).map(l -> (List<String>) l).toList());
    }
}