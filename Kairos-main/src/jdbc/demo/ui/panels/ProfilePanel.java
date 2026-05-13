package jdbc.demo.ui.panels;

import jdbc.demo.Database;
import jdbc.demo.model.*;
import jdbc.demo.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ProfilePanel extends JPanel {

    private final UserRecord user;
    private final Database   db;
    private final boolean    demo;

    /** Selected borrow row in View History (blue outline, same idea as Borrow Request). */
    private String selectedHistoryBorrowId;

    public ProfilePanel(UserRecord user, Database db, boolean demo) {
        super(new BorderLayout(16, 0));
        this.user = user;
        this.db   = db;
        this.demo = demo;
        setBackground(UIColors.BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        build();
    }

    private void build() {
        List<BorrowRecord>   borrows    = safeBorrows();
        List<BorrowRecord>   myBorrows  = borrows.stream().filter(b -> b.getBorrowerId() == user.getUserId()).toList();

        boolean isBorrower = "Student".equals(user.getType()) || "Professor".equals(user.getType());
        if (isBorrower) {
            buildBorrowerHistory(myBorrows);
            return;
        }

        List<ActivityRecord> activities = safeActivities();
        List<ActivityRecord> myActs     = activities.stream().filter(a -> a.getRequesterId() == user.getUserId()).toList();
        long active = myBorrows.stream().filter(b -> "borrowed".equals(b.getStatus())).count();

        // ── Left: user card + stats ──────────────────────────────────────────
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UIColors.BG);
        left.setPreferredSize(new Dimension(300, 0));

        // Profile card
        JPanel profileCard = UIUtils.card(new BorderLayout(0, 12));
        profileCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        profileCard.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

        // Top-left should match reference: centered square initials, name, role.
        JLabel av = UIUtils.avatar(
                String.valueOf(user.getFirstName().charAt(0)) + user.getLastName().charAt(0),
                UIColors.roleColor(user.getType()), 60);
        av.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameL = new JLabel(user.getFirstName() + " " + user.getLastName(), SwingConstants.CENTER);
        nameL.setFont(new Font("Segoe UI", Font.BOLD, 17));
        nameL.setForeground(UIColors.TEXT);
        nameL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleL = UIUtils.badge(user.getType());
        roleL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = UIUtils.separator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Info rows
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        infoPanel.setOpaque(false);
        infoPanel.add(infoRow("User ID",  String.valueOf(user.getUserId())));
        infoPanel.add(infoRow("Email",    user.getEmail()));
        infoPanel.add(infoRow("Contact",  user.getContactNum() == null || user.getContactNum().isEmpty() ? "—" : user.getContactNum()));
        infoPanel.add(infoRow("Role",     user.getType()));

        profileCard.add(avPanel(av, nameL, roleL, sep, infoPanel), BorderLayout.CENTER);

        // Edit button
        JButton editBtn = UIUtils.secondaryBtn("Edit Profile");
        editBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        editBtn.addActionListener(e -> showEditForm());

        // Stat chips
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 8, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(300, 80));
        statsRow.add(miniStat("Borrows",    String.valueOf(myBorrows.size()),  UIColors.PRIMARY));
        statsRow.add(miniStat("Active",     String.valueOf(active),            UIColors.WARNING));
        statsRow.add(miniStat("Activities", String.valueOf(myActs.size()),     UIColors.SUCCESS));

        left.add(profileCard);
        left.add(Box.createVerticalStrut(10));
        left.add(statsRow);
        left.add(Box.createVerticalStrut(10));
        left.add(editBtn);

        // ── Right: borrow + activity history ────────────────────────────────
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 12));
        right.setBackground(UIColors.BG);
        right.add(borrowHistoryScrollList("My Borrow History", myBorrows.stream()
                .filter(ProfilePanel::isCompletedBorrowRecord)
                .limit(8)
                .toList()));
        right.add(historyList("My Activity Requests", myActs.stream().limit(8)
                .map(a -> new String[]{a.getActivityName(), a.getActivityDate(), a.getStatus()}).toList()));

        add(left,  BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    /** Borrower “View History”: borrow records only (no activity requests or profile editing). */
    private void buildBorrowerHistory(List<BorrowRecord> myBorrows) {
        JPanel wrap = new JPanel(new BorderLayout(0, 12));
        wrap.setOpaque(false);

        JLabel title = new JLabel("Borrow history");
        title.setFont(UIUtils.F_BOLD);
        title.setForeground(UIColors.TEXT);
        JLabel hint = UIUtils.muted("Returned and completed borrows. Double-click a row for full details.");
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(hint, BorderLayout.SOUTH);

        List<BorrowRecord> completed = myBorrows.stream()
                .filter(ProfilePanel::isCompletedBorrowRecord)
                .toList();

        wrap.add(header, BorderLayout.NORTH);
        wrap.add(borrowHistoryScrollList("Records", completed), BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
    }

    private JPanel avPanel(JLabel av, JLabel name, JLabel role, JSeparator sep, JPanel info) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(Box.createVerticalStrut(2));
        p.add(av);
        p.add(Box.createVerticalStrut(10));
        p.add(name);
        p.add(Box.createVerticalStrut(4));
        p.add(role);
        p.add(Box.createVerticalStrut(12));
        p.add(sep);
        p.add(Box.createVerticalStrut(12));
        p.add(info);
        return p;
    }

    private JPanel infoRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.F_LABEL);
        lbl.setForeground(UIColors.TEXT_SECONDARY);
        JLabel val = new JLabel(value);
        val.setFont(UIUtils.F_BODY);
        val.setForeground(UIColors.TEXT);
        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.EAST);
        return p;
    }

    private JPanel miniStat(String label, String value, Color color) {
        JPanel card = UIUtils.card(new GridLayout(2, 1, 0, 2));
        JLabel v = new JLabel(value, SwingConstants.CENTER);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(color);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(UIUtils.F_SMALL);
        l.setForeground(UIColors.TEXT_SECONDARY);
        card.add(v); card.add(l);
        return card;
    }

    private JPanel historyList(String title, List<String[]> rows) {
        JPanel card = UIUtils.card(new BorderLayout(0, 8));
        JLabel h = new JLabel(title);
        h.setFont(UIUtils.F_BOLD);
        h.setForeground(UIColors.TEXT);
        h.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(h, BorderLayout.NORTH);

        if (rows.isEmpty()) {
            JLabel empty = UIUtils.muted("No records yet.");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            card.add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.PAGE_AXIS));
            list.setOpaque(false);
            for (int i = 0; i < rows.size(); i++) {
                JPanel row = historyRecordRow(rows.get(i));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                int rh = row.getPreferredSize().height;
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rh));
                list.add(row);
                if (i < rows.size() - 1) {
                    list.add(Box.createVerticalStrut(8));
                }
            }
            JScrollPane sp = UIUtils.scrollPane(list);
            sp.setBorder(null);
            sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.setPreferredSize(new Dimension(0, 360));
            card.add(sp, BorderLayout.CENTER);
        }
        return card;
    }

    /** Finished borrows belong in View History; active {@code borrowed} rows stay on Borrow Request. */
    private static boolean isCompletedBorrowRecord(BorrowRecord b) {
        return !"borrowed".equals(b.getStatus());
    }

    /** Borrow history list with double-click → borrow details (same fields as Borrow Request panel). */
    private JPanel borrowHistoryScrollList(String title, List<BorrowRecord> borrows) {
        JPanel card = UIUtils.card(new BorderLayout(0, 8));
        JLabel h = new JLabel(title);
        h.setFont(UIUtils.F_BOLD);
        h.setForeground(UIColors.TEXT);
        h.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(h, BorderLayout.NORTH);

        if (borrows.isEmpty()) {
            JLabel empty = UIUtils.muted("No completed borrow records yet.");
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            card.add(empty, BorderLayout.CENTER);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.PAGE_AXIS));
            list.setOpaque(false);
            for (int i = 0; i < borrows.size(); i++) {
                BorrowRecord br = borrows.get(i);
                JPanel inner = historyRecordRow(borrowHistoryRowStrings(br));
                JPanel wrap = wrapBorrowHistoryRow(br.getBorrowId(), inner);
                attachBorrowHistoryRowInteractions(wrap, inner, br, list);
                wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
                int rh = wrap.getPreferredSize().height;
                wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, rh));
                list.add(wrap);
                if (i < borrows.size() - 1) {
                    list.add(Box.createVerticalStrut(8));
                }
            }
            JScrollPane sp = UIUtils.scrollPane(list);
            sp.setBorder(null);
            sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar().setUnitIncrement(16);
            sp.setPreferredSize(new Dimension(0, 360));
            card.add(sp, BorderLayout.CENTER);
        }
        return card;
    }

    private JPanel wrapBorrowHistoryRow(String borrowId, JPanel inner) {
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setOpaque(false);
        wrap.putClientProperty("borrowId", borrowId);
        applyBorrowHistoryOutline(wrap, borrowId.equals(selectedHistoryBorrowId));
        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    private static void applyBorrowHistoryOutline(JPanel wrap, boolean selected) {
        wrap.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(selected ? UIColors.PRIMARY : UIColors.BORDER, selected ? 2 : 1, true),
                new EmptyBorder(1, 1, 1, 1)));
    }

    private void refreshBorrowHistorySelectionHighlights(JPanel listHost) {
        for (Component c : listHost.getComponents()) {
            if (!(c instanceof JPanel wrap)) {
                continue;
            }
            Object id = wrap.getClientProperty("borrowId");
            if (id == null) {
                continue;
            }
            applyBorrowHistoryOutline(wrap, id.equals(selectedHistoryBorrowId));
        }
        listHost.revalidate();
        listHost.repaint();
    }

    private void attachBorrowHistoryRowInteractions(JPanel wrap, JPanel inner, BorrowRecord b, JPanel listHost) {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectedHistoryBorrowId = b.getBorrowId();
                refreshBorrowHistorySelectionHighlights(listHost);
                if (e.getClickCount() == 2) {
                    showBorrowDetailDialog(b);
                }
            }
        };
        // Single traversal — avoids registering the same adapter twice on inner (was opening multiple detail dialogs).
        addMouseListenerDeep(wrap, ma);
    }

    private static void addMouseListenerDeep(Component root, MouseAdapter a) {
        root.addMouseListener(a);
        if (root instanceof Container co) {
            for (Component ch : co.getComponents()) {
                addMouseListenerDeep(ch, a);
            }
        }
    }

    private void showBorrowDetailDialog(BorrowRecord b) {
        String act = b.getActivityName() == null ? "—" : b.getActivityName();
        String msg = "Borrow ID: "     + b.getBorrowId()
                + "\nBorrower: "       + b.getBorrowerName()
                + "\nCustodian: "      + b.getCustodianName()
                + "\nActivity: "       + act
                + "\nItems: "          + (b.getItemName() == null ? "—" : b.getItemName())
                + "\nDate Borrowed: "  + (b.getDateBorrowed() == null ? "—" : b.getDateBorrowed() + " " + nullToEmpty(b.getTimeBorrowed()))
                + "\nDate Returned: "  + (b.getDateReturned() == null ? "—" : b.getDateReturned() + " " + nullToEmpty(b.getTimeReturned()))
                + "\nStatus: "         + b.getStatus()
                + "\nRemarks: "        + (b.getRemarks() == null || b.getRemarks().isEmpty() ? "—" : b.getRemarks());
        JOptionPane.showMessageDialog(this, msg, "Borrow Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String nullToEmpty(String s) {
        return s == null || s.isBlank() ? "" : s.trim();
    }

    /**
     * Borrow rows: {@code [0]} item, {@code [1]} date borrowed, {@code [2]} date returned, {@code [3]} activity (may be blank), {@code [4]} status.
     * Activity rows (legacy): {@code [0]} name, {@code [1]} detail line, {@code [2]} status.
     */
    private static String[] borrowHistoryRowStrings(BorrowRecord b) {
        String item = b.getItemName() == null || b.getItemName().isBlank() ? "No items listed" : b.getItemName();
        String act = b.getActivityName() == null ? "" : b.getActivityName();
        return new String[]{
                item,
                dashIfBlank(b.getDateBorrowed()),
                dashIfBlank(b.getDateReturned()),
                act,
                b.getStatus()
        };
    }

    private static String dashIfBlank(String s) {
        return s == null || s.isBlank() ? "—" : s.trim();
    }

    /** One borrow/activity history row: compact text + badge that does not stretch vertically. */
    private JPanel historyRecordRow(String[] r) {
        JPanel row = UIUtils.card(new BorderLayout(10, 0));
        row.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.PAGE_AXIS));
        info.setOpaque(false);

        String title = r[0].length() > 40 ? r[0].substring(0, 38) + "…" : r[0];
        JLabel main = new JLabel(title);
        main.setFont(UIUtils.F_BODY);
        main.setForeground(UIColors.TEXT);
        info.add(main);
        info.add(Box.createVerticalStrut(2));

        String status;
        if (r.length >= 5) {
            JLabel dates = new JLabel("Borrowed: " + r[1] + "  ·  Returned: " + r[2]);
            dates.setFont(UIUtils.F_SMALL);
            dates.setForeground(UIColors.TEXT_MUTED);
            info.add(dates);
            if (r[3] != null && !r[3].isBlank()) {
                info.add(Box.createVerticalStrut(2));
                JLabel act = new JLabel(r[3]);
                act.setFont(UIUtils.F_SMALL);
                act.setForeground(UIColors.TEXT_MUTED);
                info.add(act);
            }
            status = r[4];
        } else {
            JLabel sub = new JLabel(r[1]);
            sub.setFont(UIUtils.F_SMALL);
            sub.setForeground(UIColors.TEXT_MUTED);
            info.add(sub);
            status = r[2];
        }

        JPanel textCol = new JPanel(new BorderLayout());
        textCol.setOpaque(false);
        textCol.add(info, BorderLayout.NORTH);

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(UIUtils.badge(status));

        row.add(textCol, BorderLayout.CENTER);
        row.add(badgeWrap, BorderLayout.EAST);
        return row;
    }

    private void showEditForm() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit Profile", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = UIUtils.dialogPanel("Edit Profile", UIColors.roleColor(user.getType()));
        JPanel form  = UIUtils.formPanel();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 4, 7, 4);

        JTextField fFirst   = UIUtils.field("First name");   fFirst.setPreferredSize(new Dimension(210, 34)); fFirst.setText(user.getFirstName());
        JTextField fLast    = UIUtils.field("Last name");    fLast.setPreferredSize(new Dimension(210, 34));  fLast.setText(user.getLastName());
        JTextField fContact = UIUtils.field("639XXXXXXXXX"); fContact.setPreferredSize(new Dimension(210, 34)); fContact.setText(user.getContactNum() == null ? "" : user.getContactNum());

        UIUtils.formRow(form, gc, "First Name", fFirst,   0);
        UIUtils.formRow(form, gc, "Last Name",  fLast,    1);
        UIUtils.formRow(form, gc, "Contact",    fContact, 2);

        JButton save   = UIUtils.primaryBtn("Save Changes");
        JButton cancel = UIUtils.secondaryBtn("Cancel");

        outer.add(form, BorderLayout.CENTER);
        outer.add(UIUtils.buttonBar(cancel, save), BorderLayout.SOUTH);
        dlg.setContentPane(outer);

        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String first = fFirst.getText().trim();
            String last  = fLast.getText().trim();
            String contact = fContact.getText().trim();
            if (first.isEmpty() || last.isEmpty()) {
                UIUtils.error(dlg, "First name and last name are required.");
                return;
            }
            try {
                String contactVal = contact.isEmpty() ? null : contact;
                if (!demo) {
                    db.updateUser(user.getUserId(), first, last, user.getEmail(), contactVal, user.getType());
                }
                user.applyProfileFromForm(first, last, contactVal);
                dlg.dispose();
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w instanceof AppFrame appFrame) {
                    appFrame.refreshUserChrome();
                }
                removeAll();
                build();
                revalidate();
                repaint();
                UIUtils.info(this, demo
                        ? "Profile updated for this session (demo mode — not written to MySQL)."
                        : "Profile saved to the database.");
            } catch (Exception ex) {
                UIUtils.error(dlg, ex.getMessage());
            }
        });
        dlg.setVisible(true);
    }

    private List<BorrowRecord>   safeBorrows()    { try { return demo ? MockDataProvider.borrows()    : db.getAllBorrowRecords(); } catch (Exception e) { return MockDataProvider.borrows();    } }
    private List<ActivityRecord> safeActivities() { try { return demo ? MockDataProvider.activities() : db.getAllActivities();    } catch (Exception e) { return MockDataProvider.activities(); } }
}
