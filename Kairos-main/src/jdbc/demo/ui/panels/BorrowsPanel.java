package jdbc.demo.ui.panels;

import jdbc.demo.Database;
import jdbc.demo.model.*;
import jdbc.demo.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BorrowsPanel extends JPanel {

    private final UserRecord user;
    private final Database   db;
    private final boolean    demo;
    private final boolean    isAdminUser;
    private final boolean    canManage;

    private List<BorrowRecord>   data          = new ArrayList<>();
    private List<ItemRecord>     allItems      = new ArrayList<>();
    private List<UserRecord>     allUsers      = new ArrayList<>();
    private List<ActivityRecord> allActivities = new ArrayList<>();

    private JPanel            listHost;
    private JTextField        searchF;
    private JComboBox<String> filterStatus;
    /** Selected row for custodian Return / Delete actions. */
    private String            selectedBorrowId;

    /** When set (borrower flow), invoked after borrows list changes so the dashboard reminders stay in sync. */
    private final Runnable onBorrowRecordsChanged;

    public BorrowsPanel(UserRecord user, Database db, boolean demo) {
        this(user, db, demo, null);
    }

    public BorrowsPanel(UserRecord user, Database db, boolean demo, Runnable onBorrowRecordsChanged) {
        super(new BorderLayout());
        this.user      = user;
        this.db        = db;
        this.demo      = demo;
        this.onBorrowRecordsChanged = onBorrowRecordsChanged;
        this.isAdminUser = "Admin".equals(user.getType());
        this.canManage = isAdminUser || "Custodian".equals(user.getType());
        setBackground(UIColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        build();
        refresh();
    }

    private void notifyBorrowRecordsChanged() {
        if (onBorrowRecordsChanged != null) {
            SwingUtilities.invokeLater(onBorrowRecordsChanged);
        }
    }

    // ── Auto-generate next Borrow ID ─────────────────────────────────────────
    private String generateBorrowId() {
        return data.stream()
                .map(BorrowRecord::getBorrowId)
                .filter(id -> id != null && id.matches("B\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(1)))
                .max()
                .stream()
                .mapToObj(max -> String.format("B%03d", max + 1))
                .findFirst()
                .orElse("B001");
    }

    private void build() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));

        searchF      = UIUtils.field("Search borrower or items…"); searchF.setPreferredSize(new Dimension(220, 34));
        searchF.addActionListener(e -> applyFilter());
        filterStatus = UIUtils.combo(new String[]{
                "All Statuses", "borrowed", "returned", "returned with damage", "rejected"
        });
        filterStatus.addActionListener(e -> applyFilter());

        JButton newBtn    = UIUtils.primaryBtn("+ New Borrow");
        JButton returnBtn = UIUtils.successBtn("Record Return");
        JButton delBtn    = UIUtils.dangerBtn("Delete");
        JButton refBtn    = UIUtils.secondaryBtn("Refresh");

        toolbar.add(searchF);
        toolbar.add(filterStatus);
        toolbar.add(Box.createHorizontalStrut(8));
        if (!isAdminUser) {
            toolbar.add(newBtn);
        }
        if (canManage) {
            if (!isAdminUser) {
                toolbar.add(returnBtn);
            }
            toolbar.add(delBtn);
        }
        toolbar.add(Box.createHorizontalStrut(4)); toolbar.add(refBtn);

        listHost = new JPanel();
        listHost.setLayout(new BoxLayout(listHost, BoxLayout.PAGE_AXIS));
        listHost.setOpaque(false);

        searchF.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        JScrollPane sp = UIUtils.scrollPane(listHost);
        sp.setBorder(null);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(20);

        add(toolbar, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        newBtn.addActionListener(e -> showCreateForm(null));
        if (canManage) {
            returnBtn.addActionListener(e -> {
                BorrowRecord s = selected();
                if (s == null) { UIUtils.error(this, "Select a borrow record."); return; }
                if (!"borrowed".equals(s.getStatus())) { UIUtils.error(this, "Only active borrows can be returned."); return; }
                showReturnForm(s);
            });
            delBtn.addActionListener(e -> {
                BorrowRecord s = selected();
                if (s == null) { UIUtils.error(this,"Select a record."); return; }
                if (!UIUtils.confirm(this,"Delete borrow record '" + s.getBorrowId() + "'?")) return;
                    try {
                    if (!demo) db.deleteBorrow(s.getBorrowId(), user.getUserId());
                    else MockDataProvider.removeDemoBorrow(s.getBorrowId());
                    data.removeIf(b -> b.getBorrowId().equals(s.getBorrowId()));
                    if (s.getBorrowId().equals(selectedBorrowId)) {
                        selectedBorrowId = null;
                    }
                    applyFilter();
                    notifyBorrowRecordsChanged();
                } catch (Exception ex) { UIUtils.error(this, ex.getMessage()); }
            });
        }
        refBtn.addActionListener(e -> refresh());
    }

    /** Opens the create-borrow dialog; optionally pre-selects one item (dashboard Request). */
    public void openCreateBorrowDialog(String itemId) {
        refresh();
        showCreateForm(itemId);
    }

    private void showCreateForm(String preselectItemId) {
        // Generate ID before opening the dialog
        String generatedId = generateBorrowId();

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Create Borrow Record", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(560, 640);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = UIUtils.dialogPanel("Create Borrow Record", UIColors.SUCCESS);
        JPanel form  = UIUtils.formPanel();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);

        // Auto-generated ID shown as a read-only label
        JLabel fIdDisplay = new JLabel(generatedId);
        fIdDisplay.setFont(UIUtils.F_MONO);
        fIdDisplay.setForeground(UIColors.TEXT_SECONDARY);

        JTextField fDate = UIUtils.field("YYYY-MM-DD"); fDate.setText(LocalDate.now().toString()); fDate.setPreferredSize(new Dimension(230, 32));
        JTextField fTime = UIUtils.field("HH:MM:SS");  fTime.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))); fTime.setPreferredSize(new Dimension(230, 32));
        JTextArea  fRem  = UIUtils.textArea(2);

        String[] borrowers  = allUsers.stream().filter(u -> "Student".equals(u.getType()) || "Professor".equals(u.getType()))
                .map(u -> u.getUserId() + " – " + u.getFirstName() + " " + u.getLastName()).toArray(String[]::new);
        String[] custodians = allUsers.stream()
                .filter(u -> "Custodian".equals(u.getType()) || "Admin".equals(u.getType()))
                .map(u -> u.getUserId() + " – " + u.getFirstName() + " " + u.getLastName()).toArray(String[]::new);
        String[] activities = allActivities.stream().filter(a -> "Approved".equals(a.getStatus()))
                .map(a -> a.getActivityId() + " – " + a.getActivityName()).toArray(String[]::new);
        String[] items = allItems.stream().filter(i -> "available".equals(i.getAvailabilityStatus()))
                .map(i -> i.getItemId() + " – " + i.getItemName()).toArray(String[]::new);

        JComboBox<String> fBorrower  = UIUtils.combo(borrowers.length  > 0 ? borrowers  : new String[]{"No borrowers"});
        JComboBox<String> fCustodian = UIUtils.combo(custodians.length > 0 ? custodians : new String[]{"No custodians"});
        JComboBox<String> fActivity  = UIUtils.combo(activities.length > 0 ? activities : new String[]{"No approved activities"});
        JList<String>     itemList   = new JList<>(items);
        itemList.setFont(UIUtils.F_BODY);
        itemList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        if (preselectItemId != null && !preselectItemId.isBlank()) {
            String prefix = preselectItemId.trim() + " – ";
            for (int i = 0; i < items.length; i++) {
                if (items[i].startsWith(prefix)) {
                    itemList.setSelectedIndex(i);
                    itemList.ensureIndexIsVisible(i);
                    break;
                }
            }
        }

        // Pre-select current user if they are a borrower
        if ("Student".equals(user.getType()) || "Professor".equals(user.getType())) {
            for (int i = 0; i < borrowers.length; i++) {
                if (borrowers[i].startsWith(user.getUserId() + " – ")) { fBorrower.setSelectedIndex(i); break; }
            }
        }
        // Pre-select current user if they are a custodian
        if ("Custodian".equals(user.getType())) {
            for (int i = 0; i < custodians.length; i++) {
                if (custodians[i].startsWith(user.getUserId() + " – ")) { fCustodian.setSelectedIndex(i); break; }
            }
        }

        UIUtils.formRow(form, gc, "Borrow ID",     fIdDisplay, 0);
        UIUtils.formRow(form, gc, "Borrower *",    fBorrower,  1);
        UIUtils.formRow(form, gc, "Custodian",     fCustodian, 2);
        UIUtils.formRow(form, gc, "Activity *",    fActivity,  3);
        UIUtils.formRow(form, gc, "Date Borrowed", fDate,      4);
        UIUtils.formRow(form, gc, "Time",          fTime,      5);
        JScrollPane itemScroll = new JScrollPane(itemList);
        itemScroll.setPreferredSize(new Dimension(240, 140));
        UIUtils.formRow(form, gc, "Items (multi)", itemScroll, 6);
        UIUtils.formRow(form, gc, "Remarks",       new JScrollPane(fRem) {{ setPreferredSize(new Dimension(240, 80)); setBorder(null); }}, 7);

        JButton save   = UIUtils.primaryBtn("Create Borrow");
        JButton cancel = UIUtils.secondaryBtn("Cancel");

        outer.add(form, BorderLayout.CENTER);
        outer.add(UIUtils.buttonBar(cancel, save), BorderLayout.SOUTH);
        dlg.setContentPane(outer);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            try {
                String borStr = (String) fBorrower.getSelectedItem();
                String cusStr = (String) fCustodian.getSelectedItem();
                String actStr = (String) fActivity.getSelectedItem();
                if (borStr == null || borStr.startsWith("No") || actStr == null || actStr.startsWith("No")) {
                    UIUtils.error(dlg, "Select a valid borrower and an approved activity."); return;
                }
                int borId = Integer.parseInt(borStr.split(" – ")[0].trim());
                int cusId = cusStr != null && !cusStr.startsWith("No")
                        ? Integer.parseInt(cusStr.split(" – ")[0].trim())
                        : user.getUserId();
                String actId   = actStr.split(" – ")[0].trim();
                String date    = fDate.getText().trim();
                String time    = fTime.getText().trim();
                String remarks = fRem.getText().trim();

                List<String> selItems = itemList.getSelectedValuesList();
                String itemNames = selItems.stream()
                        .map(s -> s.split(" – ")[1])
                        .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);

                if (!demo) {
                    db.createBorrow(generatedId, borId, cusId, date, time, actId, remarks, user.getUserId());
                    for (String si : selItems) { db.addItemToBorrow(generatedId, si.split(" – ")[0].trim(), user.getUserId()); }
                }

                UserRecord     bor = allUsers.stream().filter(u -> u.getUserId() == borId).findFirst().orElse(null);
                UserRecord     cus = allUsers.stream().filter(u -> u.getUserId() == cusId).findFirst().orElse(null);
                ActivityRecord act = allActivities.stream().filter(a -> a.getActivityId().equals(actId)).findFirst().orElse(null);

                BorrowRecord created = new BorrowRecord(
                        generatedId,
                        borId,
                        bor != null ? bor.getFirstName() + " " + bor.getLastName() : "?",
                        cus != null ? cus.getFirstName() + " " + cus.getLastName() : "?",
                        act != null ? act.getActivityName() : actId,
                        itemNames.isEmpty() ? null : itemNames,
                        date, time, null, null, "borrowed", remarks);
                data.add(created);
                if (demo) {
                    MockDataProvider.addDemoBorrow(created);
                }
                applyFilter();
                dlg.dispose();
                notifyBorrowRecordsChanged();
            } catch (Exception ex) { UIUtils.error(dlg, ex.getMessage()); }
        });
        dlg.pack();
        dlg.setSize(560, Math.max(dlg.getHeight(), 640));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void showReturnForm(BorrowRecord borrow) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Record Return", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 380);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = UIUtils.dialogPanel("Record Return", UIColors.SUCCESS);
        JPanel form  = UIUtils.formPanel();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.setBackground(new Color(240, 253, 244));
        info.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel n  = new JLabel(borrow.getBorrowerName()); n.setFont(UIUtils.F_BOLD); n.setForeground(UIColors.SUCCESS);
        JLabel it = new JLabel(borrow.getItemName() == null ? "No items" : borrow.getItemName()); it.setFont(UIUtils.F_SMALL); it.setForeground(UIColors.TEXT_SECONDARY);
        info.add(n); info.add(it);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(0, 0, 14, 0);
        form.add(info, gc);
        gc.gridwidth = 1; gc.insets = new Insets(6, 4, 6, 4);

        JTextField fRetDate = UIUtils.field("YYYY-MM-DD"); fRetDate.setText(LocalDate.now().toString()); fRetDate.setPreferredSize(new Dimension(210, 34));
        JTextField fRetTime = UIUtils.field("HH:MM:SS");   fRetTime.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))); fRetTime.setPreferredSize(new Dimension(210, 34));
        JCheckBox  fDamaged = new JCheckBox("Returned with damage"); fDamaged.setFont(UIUtils.F_BODY); fDamaged.setOpaque(false);
        JTextArea  fRem     = UIUtils.textArea(2);

        UIUtils.formRow(form, gc, "Date Returned", fRetDate, 1);
        UIUtils.formRow(form, gc, "Time Returned", fRetTime, 2);
        gc.gridx = 1; gc.gridy = 3; form.add(fDamaged, gc);
        UIUtils.formRow(form, gc, "Remarks", new JScrollPane(fRem) {{ setPreferredSize(new Dimension(210, 50)); setBorder(null); }}, 4);

        JButton save   = UIUtils.successBtn("Record Return");
        JButton cancel = UIUtils.secondaryBtn("Cancel");

        outer.add(form, BorderLayout.CENTER);
        outer.add(UIUtils.buttonBar(cancel, save), BorderLayout.SOUTH);
        dlg.setContentPane(outer);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String retDate = fRetDate.getText().trim(), retTime = fRetTime.getText().trim(), rem = fRem.getText().trim();
            boolean damaged = fDamaged.isSelected();
            try {
                if (!demo) db.returnBorrow(borrow.getBorrowId(), retDate, retTime, damaged, rem, user.getUserId());
                String newStatus = damaged ? "returned with damage" : "returned";
                BorrowRecord updated = new BorrowRecord(borrow.getBorrowId(), borrow.getBorrowerId(), borrow.getBorrowerName(), borrow.getCustodianName(),
                        borrow.getActivityName(), borrow.getItemName(), borrow.getDateBorrowed(), borrow.getTimeBorrowed(),
                        retDate, retTime, newStatus, rem.isEmpty() ? borrow.getRemarks() : rem);
                data.replaceAll(b -> b.getBorrowId().equals(borrow.getBorrowId()) ? updated : b);
                if (demo) {
                    MockDataProvider.replaceDemoBorrow(updated);
                }
                applyFilter(); dlg.dispose();
                notifyBorrowRecordsChanged();
                UIUtils.info(this, "Return recorded: " + newStatus + ".");
            } catch (Exception ex) { UIUtils.error(dlg, ex.getMessage()); }
        });
        dlg.setVisible(true);
    }

    private void showDetail(BorrowRecord b) {
        String msg = "Borrow ID: "     + b.getBorrowId()
                + "\nBorrower: "       + b.getBorrowerName()
                + "\nCustodian: "      + b.getCustodianName()
                + "\nActivity: "       + (b.getActivityName() == null ? "—" : b.getActivityName())
                + "\nItems: "          + (b.getItemName() == null ? "—" : b.getItemName())
                + "\nDate Borrowed: "  + (b.getDateBorrowed() == null ? "—" : b.getDateBorrowed() + " " + b.getTimeBorrowed())
                + "\nDate Returned: "  + (b.getDateReturned() == null ? "—" : b.getDateReturned() + " " + b.getTimeReturned())
                + "\nStatus: "         + b.getStatus()
                + "\nRemarks: "        + (b.getRemarks() == null || b.getRemarks().isEmpty() ? "—" : b.getRemarks());
        JOptionPane.showMessageDialog(this, msg, "Borrow Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refresh() {
        try { data          = new ArrayList<>(demo ? MockDataProvider.borrows()    : db.getAllBorrowRecords()); } catch (Exception e) { data          = new ArrayList<>(MockDataProvider.borrows());    }
        try { allItems      = new ArrayList<>(demo ? MockDataProvider.items()      : db.getAllItems());          } catch (Exception e) { allItems      = new ArrayList<>(MockDataProvider.items());      }
        try { allUsers      = new ArrayList<>(demo ? MockDataProvider.users()      : db.getAllUsers());          } catch (Exception e) { allUsers      = new ArrayList<>(MockDataProvider.users());      }
        try { allActivities = new ArrayList<>(demo ? MockDataProvider.activities() : db.getAllActivities());     } catch (Exception e) { allActivities = new ArrayList<>(MockDataProvider.activities()); }
        if (selectedBorrowId != null && data.stream().noneMatch(b -> selectedBorrowId.equals(b.getBorrowId()))) {
            selectedBorrowId = null;
        }
        applyFilter();
    }

    private void applyFilter() {
        if (listHost == null) {
            return;
        }
        String q  = searchF.getText().toLowerCase(Locale.ROOT);
        boolean isBorrower = !canManage;
        listHost.removeAll();

        String selectedStatus = selectedStatus();
        List<BorrowRecord> filtered = data.stream()
                .filter(b -> selectedStatus.isEmpty() || selectedStatus.equalsIgnoreCase(b.getStatus()))
                .filter(b -> (!isBorrower || b.getBorrowerId() == user.getUserId())
                        && (q.isEmpty() || b.getBorrowerName().toLowerCase(Locale.ROOT).contains(q)
                        || (b.getItemName() != null && b.getItemName().toLowerCase(Locale.ROOT).contains(q))
                        || (b.getBorrowId() != null && b.getBorrowId().toLowerCase(Locale.ROOT).contains(q))))
                .toList();

        if (filtered.isEmpty()) {
            JLabel empty = UIUtils.muted("No borrow records match your filters.");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            empty.setBorder(new EmptyBorder(16, 8, 16, 8));
            listHost.add(empty);
        } else {
            for (int i = 0; i < filtered.size(); i++) {
                BorrowRecord b = filtered.get(i);
                JPanel card = borrowRecordCard(b);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                int h = card.getPreferredSize().height;
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
                listHost.add(card);
                if (i < filtered.size() - 1) {
                    listHost.add(Box.createVerticalStrut(10));
                }
            }
        }
        listHost.revalidate();
        listHost.repaint();
    }

    private JPanel borrowRecordCard(BorrowRecord b) {
        boolean sel = b.getBorrowId().equals(selectedBorrowId);
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(sel ? UIColors.PRIMARY : UIColors.BORDER, sel ? 2 : 1, true),
                new EmptyBorder(1, 1, 1, 1)));

        JPanel inner = UIUtils.card(new BorderLayout(0, 8));
        inner.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        JLabel idLab = new JLabel(b.getBorrowId());
        idLab.setFont(UIUtils.F_MONO);
        idLab.setForeground(UIColors.TEXT);
        JPanel badgeFlow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeFlow.setOpaque(false);
        badgeFlow.add(UIUtils.badge(b.getStatus()));
        top.add(idLab, BorderLayout.WEST);
        top.add(badgeFlow, BorderLayout.EAST);

        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.PAGE_AXIS));
        details.setOpaque(false);
        details.add(metaRow("Borrower", b.getBorrowerName()));
        details.add(Box.createVerticalStrut(4));
        details.add(metaRow("Custodian", b.getCustodianName()));
        details.add(Box.createVerticalStrut(4));
        details.add(metaRow("Activity", truncate(b.getActivityName(), 56)));
        details.add(Box.createVerticalStrut(4));
        details.add(metaRow("Item(s)", truncate(b.getItemName(), 56)));
        details.add(Box.createVerticalStrut(4));
        details.add(metaRow("Borrowed", fmtDateTime(b.getDateBorrowed(), b.getTimeBorrowed())));
        details.add(Box.createVerticalStrut(4));
        details.add(metaRow("Returned", fmtDateTime(b.getDateReturned(), b.getTimeReturned())));

        inner.add(top, BorderLayout.NORTH);
        inner.add(details, BorderLayout.CENTER);

        wrap.add(inner, BorderLayout.CENTER);

        MouseAdapter cardMouse = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedBorrowId = b.getBorrowId();
                applyFilter();
                if (e.getClickCount() == 2) {
                    showDetail(b);
                }
            }
        };
        wrap.addMouseListener(cardMouse);
        inner.addMouseListener(cardMouse);
        return wrap;
    }

    private static JPanel metaRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(UIUtils.F_LABEL);
        l.setForeground(UIColors.TEXT_SECONDARY);
        JLabel v = new JLabel(value == null || value.isBlank() ? "—" : value);
        v.setFont(UIUtils.F_BODY);
        v.setForeground(UIColors.TEXT);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private static String truncate(String s, int max) {
        if (s == null || s.isBlank()) {
            return "—";
        }
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private static String fmtDateTime(String date, String time) {
        if (date == null || date.isBlank()) {
            return "—";
        }
        if (time == null || time.isBlank()) {
            return date;
        }
        return date + " · " + time;
    }

    private BorrowRecord selected() {
        if (selectedBorrowId == null) {
            return null;
        }
        return data.stream().filter(b -> selectedBorrowId.equals(b.getBorrowId())).findFirst().orElse(null);
    }

    private String selectedStatus() {
        if (filterStatus == null) {
            return "";
        }
        String s = (String) filterStatus.getSelectedItem();
        return (s == null || s.startsWith("All")) ? "" : s.trim();
    }
}