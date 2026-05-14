package jdbc.demo.ui.panels;

import jdbc.demo.Database;
import jdbc.demo.model.UserRecord;
import jdbc.demo.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class UsersPanel extends JPanel {

    /** {@code user} table in {@code kairosact4_may11.sql} (utf8mb4 character counts). */
    private static final int SQL_USER_FIRST_MAX = 20;
    private static final int SQL_USER_LAST_MAX = 20;
    private static final int SQL_USER_EMAIL_MAX = 50;
    private static final int SQL_USER_CONTACT_MAX = 20;
    /** Sample data uses Philippine mobile style {@code 639…} (12 digits). */
    private static final Pattern SQL_CONTACT_FORMAT = Pattern.compile("^639\\d{9}$");
    private static final Pattern SQL_EMAIL_SHAPE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final String[] COLS  = {"ID", "First Name", "Last Name", "Email", "Contact", "Role", "Borrows"};
    private static final String[] TYPES = {"Student", "Professor", "Custodian", "Admin"};

    private static final String[] BORROWER_TYPES = {"Student", "Professor"};
    /** Characters for auto-generated initial passwords (ambiguous 0/O/1/l omitted). */

    private final UserRecord currentUser;
    private final Database   db;
    private final boolean    demo;
    private final boolean    isAdmin;
    /** Custodian “View Users” lists borrowers only (no Admin / Custodian accounts). */
    private final boolean    custodianBorrowersOnly;

    private List<UserRecord>  data = new ArrayList<>();
    private DefaultTableModel model;
    private JTable            table;
    private JTextField        searchF;
    private JComboBox<String> filterRole;

    public UsersPanel(UserRecord user, Database db, boolean demo) {
        super(new BorderLayout());
        this.currentUser = user;
        this.db   = db;
        this.demo = demo;
        this.isAdmin = "Admin".equals(user.getType());
        this.custodianBorrowersOnly = "Custodian".equals(user.getType());
        setBackground(UIColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        build();
        refresh();
    }

    private void build() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));

        searchF    = UIUtils.field("Search by name or email…");
        searchF.setPreferredSize(new Dimension(240, 34));
        filterRole = UIUtils.combo(custodianBorrowersOnly
                ? prepend("All borrowers", BORROWER_TYPES)
                : prepend("All Roles", TYPES));

        searchF.addActionListener(e -> applyFilter());
        filterRole.addActionListener(e -> applyFilter());

        toolbar.add(searchF); toolbar.add(filterRole);

        JButton addBtn  = UIUtils.primaryBtn("+ Add User");
        JButton editBtn = UIUtils.secondaryBtn("Edit");
        JButton delBtn  = UIUtils.dangerBtn("Delete");
        JButton refBtn  = UIUtils.secondaryBtn("Refresh");

        if (isAdmin) {
            toolbar.add(Box.createHorizontalStrut(8));
            toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(delBtn);
        }
        toolbar.add(Box.createHorizontalStrut(4)); toolbar.add(refBtn);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setCellRenderer(UIUtils.monoRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(UIUtils.badgeRenderer());
        int[] widths = {50, 120, 130, 200, 130, 100, 70};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        add(toolbar, BorderLayout.NORTH);
        add(UIUtils.scrollPane(table), BorderLayout.CENTER);

        if (isAdmin) {
            addBtn.addActionListener(e -> showForm(null));
            editBtn.addActionListener(e -> { UserRecord sel = selected(); if (sel == null) { UIUtils.error(this, "Select a user."); return; } showForm(sel); });
            delBtn.addActionListener(e -> {
                UserRecord sel = selected();
                if (sel == null) { UIUtils.error(this, "Select a user."); return; }
                if (sel.getUserId() == currentUser.getUserId()) { UIUtils.error(this, "You cannot delete your own account."); return; }
                if (!UIUtils.confirm(this, "Delete user '" + sel.getFirstName() + " " + sel.getLastName() + "'?")) return;
                try {
                    if (!demo) db.deleteUser(sel.getUserId());
                    data.remove(sel); applyFilter();
                } catch (Exception ex) { UIUtils.error(this, ex.getMessage()); }
            });
        }
        refBtn.addActionListener(e -> refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                if (ev.getClickCount() == 2 && isAdmin) { UserRecord sel = selected(); if (sel != null) showForm(sel); }
            }
        });
    }

    private void showForm(UserRecord user) {
        boolean isNew = (user == null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "Add User" : "Edit User", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = UIUtils.dialogPanel(isNew ? "Add New User" : "Edit User", UIColors.PURPLE);
        JPanel form  = UIUtils.formPanel();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);

        JTextField fId      = UIUtils.field("e.g. 2250150");      fId.setPreferredSize(new Dimension(240, 34));
        JTextField fFirst   = UIUtils.field("First name");        fFirst.setPreferredSize(new Dimension(240, 34));
        JTextField fLast    = UIUtils.field("Last name");         fLast.setPreferredSize(new Dimension(240, 34));
        JTextField fEmail   = UIUtils.field("user@slu.edu.ph");   fEmail.setPreferredSize(new Dimension(240, 34));
        JTextField fContact = UIUtils.field("639XXXXXXXXX");      fContact.setPreferredSize(new Dimension(240, 34));
        JComboBox<String> fType = UIUtils.combo(TYPES);

        if (!isNew) {
            fId.setText(String.valueOf(user.getUserId())); fId.setEditable(false);
            fFirst.setText(user.getFirstName()); fLast.setText(user.getLastName());
            fEmail.setText(user.getEmail()); fContact.setText(user.getContactNum());
            fType.setSelectedItem(user.getType());
        }

        UIUtils.formRow(form, gc, "User ID *",    fId,      0);
        UIUtils.formRow(form, gc, "First Name *", fFirst,   1);
        UIUtils.formRow(form, gc, "Last Name *",  fLast,    2);
        UIUtils.formRow(form, gc, "Email *",      fEmail,   3);
        UIUtils.formRow(form, gc, "Contact",      fContact, 4);
        UIUtils.formRow(form, gc, "Role",         fType,    5);

        JButton save   = UIUtils.primaryBtn(isNew ? "Add User" : "Update");
        JButton cancel = UIUtils.secondaryBtn("Cancel");

        outer.add(form, BorderLayout.CENTER);
        outer.add(UIUtils.buttonBar(cancel, save), BorderLayout.SOUTH);
        dlg.setContentPane(outer);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String first = fFirst.getText().trim(), last = fLast.getText().trim(), email = fEmail.getText().trim();
            String contact = fContact.getText().trim();
            String type = (String) fType.getSelectedItem();
            int uid;
            if (isNew) {
                try {
                    uid = Integer.parseInt(fId.getText().trim());
                } catch (NumberFormatException ex) {
                    UIUtils.error(dlg, "User ID must be a whole number (no letters or spaces).");
                    return;
                }
                if (uid < 1) {
                    UIUtils.error(dlg, "User ID must be a positive number.");
                    return;
                }
            } else {
                uid = user.getUserId();
            }
            String err = validateUserAgainstSchema(first, last, email, contact, type);
            if (err != null) {
                UIUtils.error(dlg, err);
                return;
            }
            try {
                if (isNew && userIdTaken(uid)) {
                    UIUtils.error(dlg, "That user ID is already in use. Choose a different ID.");
                    return;
                }
                if (emailInUse(email, isNew ? null : uid)) {
                    UIUtils.error(dlg, "That email is already used by another account.");
                    return;
                }
                if (!contact.isEmpty() && contactInUse(contact, isNew ? null : uid)) {
                    UIUtils.error(dlg, "That contact number is already used by another account.");
                    return;
                }
                String generatedPassword = null;
                if (isNew) {
                    generatedPassword = email.split("@")[0];   // e.g. "ej0160" from "ej0160@slu.edu.ph"
                    if (!demo) {
                        db.addUser(uid, first, last, email, contact, type, generatedPassword);
                    }
                    data.add(new UserRecord(uid, first, last, email, contact, type, generatedPassword));
                } else {
                    if (!demo) db.updateUser(uid, first, last, email, contact, type);
                    data.replaceAll(u -> u.getUserId() == uid ? new UserRecord(uid, first, last, email, contact, type, u.getPassword()) : u);
                }
                applyFilter();
                dlg.dispose();
                if (generatedPassword != null) {
                    final String pwdCopy = generatedPassword;
                    final String emailCopy = email;
                    SwingUtilities.invokeLater(() -> showCreatedUserPasswordNotice(this, emailCopy, pwdCopy));
                }
            } catch (Exception ex) { UIUtils.error(dlg, ex.getMessage()); }
        });
        dlg.setVisible(true);
    }



    private static void showCreatedUserPasswordNotice(Component parent, String email, String password) {
        String text = "Account created successfully.\n\n"
                + "Email: " + email + "\n\n"
                + "Temporary password (same as email username):\n"
                + password + "\n\n"
                + "The user will be prompted to change their password on first login.";
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(0);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(440, 200));
        JOptionPane.showMessageDialog(parent, sp, "User created", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refresh() {
        try { data = new ArrayList<>(demo ? MockDataProvider.users() : db.getAllUsers()); }
        catch (Exception e) { data = new ArrayList<>(MockDataProvider.users()); }
        if (custodianBorrowersOnly) {
            data.removeIf(u -> !isBorrowerUserType(u.getType()));
        }
        applyFilter();
    }

    private static boolean isBorrowerUserType(String type) {
        return "Student".equals(type) || "Professor".equals(type);
    }

    private void applyFilter() {
        String q    = searchF.getText().toLowerCase();
        String role = selectedOrEmpty(filterRole);
        model.setRowCount(0);
        data.stream()
            .filter(u -> (role.isEmpty() || role.equals(u.getType()))
                      && (q.isEmpty()    || (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(q)
                                         || u.getEmail().toLowerCase().contains(q)))
            .forEach(u -> {
                int borrows = 0;
                try { if (!demo) borrows = db.getUserBorrowCount(u.getUserId()); }
                catch (Exception ignored) {}
                model.addRow(new Object[]{u.getUserId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getContactNum(), u.getType(), borrows});
            });
    }

    private UserRecord selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return data.stream().filter(u -> u.getUserId() == id).findFirst().orElse(null);
    }

    private String selectedOrEmpty(JComboBox<String> cb) {
        String s = (String) cb.getSelectedItem();
        return s != null && s.startsWith("All") ? "" : s == null ? "" : s;
    }
    private String[] prepend(String f, String[] r) {
        String[] a = new String[r.length + 1]; a[0] = f; System.arraycopy(r, 0, a, 1, r.length); return a;
    }

    private boolean userIdTaken(int userId) throws SQLException {
        if (demo) {
            return data.stream().anyMatch(u -> u.getUserId() == userId);
        }
        return db.getUserById(userId) != null;
    }

    private boolean emailInUse(String email, Integer editingUserId) throws SQLException {
        int skipId = editingUserId == null ? Integer.MIN_VALUE : editingUserId;
        if (demo) {
            return data.stream().anyMatch(u ->
                    u.getEmail().equalsIgnoreCase(email) && u.getUserId() != skipId);
        }
        for (UserRecord u : db.getAllUsers()) {
            if (u.getUserId() != skipId && u.getEmail() != null && u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    private boolean contactInUse(String contact, Integer editingUserId) throws SQLException {
        if (contact.isEmpty()) {
            return false;
        }
        int skipId = editingUserId == null ? Integer.MIN_VALUE : editingUserId;
        if (demo) {
            return data.stream().anyMatch(u ->
                    contact.equals(u.getContactNum()) && u.getUserId() != skipId);
        }
        for (UserRecord u : db.getAllUsers()) {
            if (u.getUserId() != skipId && contact.equals(u.getContactNum())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Aligns with {@code user} columns and {@code user_type_chk} in {@code kairosact4_may11.sql}.
     */
    private static String validateUserAgainstSchema(String first, String last, String email,
                                                    String contact, String type) {
        if (first.isEmpty() || last.isEmpty() || email.isEmpty()) {
            return "First name, last name, and email are required.";
        }
        if (containsControlChars(first) || containsControlChars(last) || containsControlChars(email)
                || containsControlChars(contact)) {
            return "Remove unusual hidden characters from the fields and try again.";
        }
        if (sqlUtf8mb4CharLen(first) > SQL_USER_FIRST_MAX) {
            return "First name is too long (use at most " + SQL_USER_FIRST_MAX + " characters).";
        }
        if (sqlUtf8mb4CharLen(last) > SQL_USER_LAST_MAX) {
            return "Last name is too long (use at most " + SQL_USER_LAST_MAX + " characters).";
        }
        if (sqlUtf8mb4CharLen(email) > SQL_USER_EMAIL_MAX) {
            return "Email is too long (use at most " + SQL_USER_EMAIL_MAX + " characters).";
        }
        if (!SQL_EMAIL_SHAPE.matcher(email).matches()) {
            return "Enter a valid email address (for example user@slu.edu.ph).";
        }
        if (type == null || !Arrays.asList(TYPES).contains(type)) {
            return "Choose a role: Student, Professor, Custodian, or Admin.";
        }
        if (!contact.isEmpty()) {
            if (sqlUtf8mb4CharLen(contact) > SQL_USER_CONTACT_MAX) {
                return "Contact number is too long (use at most " + SQL_USER_CONTACT_MAX + " characters).";
            }
            if (!SQL_CONTACT_FORMAT.matcher(contact).matches()) {
                return "Contact must be 12 digits starting with 639 (for example 639491234567). Leave blank if none.";
            }
        }
        return null;
    }

    private static int sqlUtf8mb4CharLen(String s) {
        return (int) s.codePoints().count();
    }

    private static boolean containsControlChars(String s) {
        return s != null && s.codePoints().anyMatch(Character::isISOControl);
    }
}
