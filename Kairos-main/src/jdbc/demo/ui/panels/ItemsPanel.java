package jdbc.demo.ui.panels;

import jdbc.demo.Database;
import jdbc.demo.model.ItemRecord;
import jdbc.demo.model.UserRecord;
import jdbc.demo.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ItemsPanel extends JPanel {

    /** Matches {@code item} / {@code AddItem} in {@code kairosact4_may11.sql} (utf8mb4 character counts). */
    private static final int SQL_ITEM_ID_MAX = 15;
    private static final int SQL_ITEM_NAME_MAX = 20;
    private static final int SQL_ITEM_DESC_MAX = 50;
    private static final int SQL_ITEM_MODEL_MAX = 100;
    /**
     * Kairos {@code item.itemId} convention from {@code kairosact4_may11.sql} seed data and examples
     * ({@code I-CT-01}, {@code I-HDMI-01}, comment {@code I-LT-03}): {@code I-}, uppercase category letters,
     * {@code -}, then a 2–4 digit sequence. Total length must fit {@code varchar(15)}.
     */
    private static final Pattern SQL_ITEM_ID_FORMAT = Pattern.compile("^I-[A-Z]{2,8}-\\d{2,4}$");

    private static final String[] COLS = {"Item ID", "Name", "Type", "Model", "Condition", "Availability", "Acquired"};
    private static final String[] TYPES  = {"equipment","peripheral","accessory","tool"};
    private static final String[] CONDS  = {"working","damaged","under maintenance"};
    private static final String[] AVAILS = {"available","borrowed","unavailable"};

    private enum CustodianItemsMode { NA, ADD_ONLY, MANAGE }

    private final Database db;
    private final boolean  demo;
    private final boolean  isCustodian;
    private CustodianItemsMode custodianItemsMode = CustodianItemsMode.NA;

    private JButton addBtn;
    private JButton editBtn;
    private JButton delBtn;

    private List<ItemRecord>  data    = new ArrayList<>();
    private DefaultTableModel model;
    private JTable            table;
    private JTextField        searchF;
    private JComboBox<String> filterAvail, filterType, filterCond;

    public ItemsPanel(Object user, Database db, boolean demo) {
        super(new BorderLayout());
        this.db   = db;
        this.demo = demo;
        this.isCustodian = user instanceof UserRecord ur && "Custodian".equals(ur.getType());
        if (isCustodian) {
            custodianItemsMode = CustodianItemsMode.MANAGE;
        }
        setBackground(UIColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        build();
        refresh();
        if (isCustodian) {
            applyCustodianItemsMode(custodianItemsMode);
        }
    }

    /** Custodian: Add Equipment vs Manage Equipment share one panel — toggle toolbar from {@link jdbc.demo.ui.AppFrame}. */
    public void applyCustodianNavId(String navId) {
        if (!isCustodian) {
            return;
        }
        switch (navId) {
            case "cu-items-add" -> applyCustodianItemsMode(CustodianItemsMode.ADD_ONLY);
            case "cu-items-man" -> applyCustodianItemsMode(CustodianItemsMode.MANAGE);
            default -> { }
        }
    }

    private void applyCustodianItemsMode(CustodianItemsMode mode) {
        custodianItemsMode = mode;
        boolean addOnly = (mode == CustodianItemsMode.ADD_ONLY);
        editBtn.setVisible(!addOnly);
        delBtn.setVisible(!addOnly);
        addBtn.setVisible(true);
        revalidate();
        repaint();
    }

    private boolean custodianManageEquipmentActions() {
        return !isCustodian || custodianItemsMode == CustodianItemsMode.MANAGE;
    }

    private void build() {
        // ── Toolbar ──────────────────────────────────────────────────────────
        // Build a single-row toolbar where search flexes and combos never disappear.
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        searchF     = UIUtils.field("Search…");
        searchF.setPreferredSize(new Dimension(180, 34));
        filterAvail = UIUtils.combo(prepend("All Availability", AVAILS));
        filterType  = UIUtils.combo(prepend("All Types", TYPES));
        filterCond  = UIUtils.combo(prepend("All Conditions", CONDS));

        searchF.addActionListener(e -> applyFilter());
        filterAvail.addActionListener(e -> applyFilter());
        filterType.addActionListener(e -> applyFilter());
        filterCond.addActionListener(e -> applyFilter());

        addBtn   = UIUtils.primaryBtn("+ Add Equipment");
        editBtn  = UIUtils.secondaryBtn("Edit");
        delBtn   = UIUtils.dangerBtn("Delete");
        JButton refBtn   = UIUtils.secondaryBtn("Refresh");

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.weightx = 1.0;
        left.add(searchF, gc);
        gc.gridx = 1; gc.weightx = 0;
        left.add(filterAvail, gc);
        gc.gridx = 2;
        left.add(filterType, gc);
        gc.gridx = 3; gc.insets = new Insets(0, 0, 0, 0);
        left.add(filterCond, gc);

        right.add(addBtn);
        right.add(editBtn);
        right.add(delBtn);
        right.add(refBtn);

        toolbar.add(left, BorderLayout.CENTER);
        toolbar.add(right, BorderLayout.EAST);

        // ── Table ─────────────────────────────────────────────────────────────
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UIUtils.styleTable(table);
        // Badge renderers for status columns
        table.getColumnModel().getColumn(4).setCellRenderer(UIUtils.badgeRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(UIUtils.badgeRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(UIUtils.badgeRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(UIUtils.monoRenderer());
        // Column widths
        int[] widths = {100, 180, 100, 160, 130, 120, 100};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = UIUtils.scrollPane(table);

        // ── Wrap in card ─────────────────────────────────────────────────────
        add(toolbar, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        // ── Button actions ────────────────────────────────────────────────────
        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> {
            if (!custodianManageEquipmentActions()) return;
            ItemRecord sel = selected();
            if (sel == null) { UIUtils.error(this, "Select an item first."); return; }
            showForm(sel);
        });
        delBtn.addActionListener(e -> {
            if (!custodianManageEquipmentActions()) return;
            ItemRecord sel = selected();
            if (sel == null) { UIUtils.error(this, "Select an item first."); return; }
            if (!UIUtils.confirm(this, "Delete '" + sel.getItemName() + "'?\nBorrowed items cannot be deleted until returned.")) return;
            try {
                if (!demo) {
                    db.deleteItem(sel.getItemId());
                    refresh();
                } else {
                    data.remove(sel);
                    applyFilter();
                }
            } catch (Exception ex) { UIUtils.error(this, ex.getMessage()); }
        });
        refBtn.addActionListener(e -> refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                if (ev.getClickCount() != 2) return;
                if (!custodianManageEquipmentActions()) return;
                ItemRecord sel = selected();
                if (sel != null) showForm(sel);
            }
        });
    }

    // ── Form dialog ───────────────────────────────────────────────────────────
    private void showForm(ItemRecord item) {
        boolean isNew = (item == null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "Add Equipment" : "Edit Equipment", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(500, 550);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = UIUtils.dialogPanel(isNew ? "Add Equipment" : "Edit Equipment", UIColors.WARNING);
        JPanel form  = UIUtils.formPanel();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);

        JTextField fId    = UIUtils.field("e.g. I-LT-03");   fId.setPreferredSize(new Dimension(240, 34));
        JTextField fName  = UIUtils.field("Item name");       fName.setPreferredSize(new Dimension(240, 34));
        JTextField fModel = UIUtils.field("Model number");    fModel.setPreferredSize(new Dimension(240, 34));
        JTextField fAcq   = UIUtils.field("YYYY-MM-DD");      fAcq.setPreferredSize(new Dimension(240, 34));
        JTextArea  fDesc  = UIUtils.textArea(2);
        JComboBox<String> fType  = UIUtils.combo(TYPES);
        JComboBox<String> fCond  = UIUtils.combo(CONDS);
        JComboBox<String> fAvail = UIUtils.combo(AVAILS);

        if (!isNew) {
            fId.setText(item.getItemId()); fId.setEditable(false);
            fName.setText(item.getItemName()); fModel.setText(item.getModel());
            fDesc.setText(item.getDescription()); fAcq.setText(item.getDateAcquired());
            fType.setSelectedItem(item.getItemType());
            fCond.setSelectedItem(item.getConditionStatus());
            fAvail.setSelectedItem(item.getAvailabilityStatus());
        } else {
            fAcq.setText(LocalDate.now().toString());
        }

        UIUtils.formRow(form, gc, "Item ID *",    fId,    0);
        UIUtils.formRow(form, gc, "Name *",       fName,  1);
        UIUtils.formRow(form, gc, "Type",         fType,  2);
        UIUtils.formRow(form, gc, "Model",        fModel, 3);
        UIUtils.formRow(form, gc, "Condition",    fCond,  4);
        UIUtils.formRow(form, gc, "Availability", fAvail, 5);
        UIUtils.formRow(form, gc, "Acquired",     fAcq,   6);
        UIUtils.formRow(form, gc, "Description",  new JScrollPane(fDesc) {{ setPreferredSize(new Dimension(240, 56)); setBorder(null); }}, 7);

        JButton save   = UIUtils.primaryBtn(isNew ? "Add Equipment" : "Update");
        JButton cancel = UIUtils.secondaryBtn("Cancel");

        outer.add(form, BorderLayout.CENTER);
        outer.add(UIUtils.buttonBar(cancel, save), BorderLayout.SOUTH);
        dlg.setContentPane(outer);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            // Normalize to uppercase I-CODE-NN so input matches DB seed style (utf8mb4_unicode_ci compares case-insensitively).
            String id = fId.getText().trim().toUpperCase();
            String name = fName.getText().trim();
            String type = (String) fType.getSelectedItem();
            String cond = (String) fCond.getSelectedItem();
            String avail = (String) fAvail.getSelectedItem();
            String model2 = fModel.getText().trim();
            String desc = fDesc.getText().trim();
            String acq = fAcq.getText().trim();
            String err = validateItemAgainstSchema(id, name, type, cond, avail, model2, desc, acq);
            if (err != null) {
                UIUtils.error(dlg, err);
                return;
            }
            try {
                if (isNew && itemIdTaken(id)) {
                    UIUtils.error(dlg, "Item ID already exists.");
                    return;
                }
                String nameDb = name;
                String descDb = desc.isEmpty() ? "—" : desc;
                String modelDb = model2.isEmpty() ? "—" : model2;
                String idDb = id;
                if (isNew) {
                    if (!demo) {
                        db.addItem(idDb, nameDb, type, descDb, modelDb, cond, avail, acq);
                        refresh();
                    } else {
                        data.add(new ItemRecord(idDb, nameDb, type, descDb, modelDb, cond, avail, acq));
                        applyFilter();
                    }
                } else {
                    if (!demo) {
                        // Schema: UpdateItem (name/type/desc/model/date) + UpdateItemStatus (condition/availability)
                        db.updateItem(idDb, nameDb, type, descDb, modelDb, acq);
                        db.updateItemStatus(idDb, cond, avail);
                        refresh();
                    } else {
                        data.replaceAll(i -> i.getItemId().equals(idDb)
                                ? new ItemRecord(idDb, nameDb, type, descDb, modelDb, cond, avail, acq) : i);
                        applyFilter();
                    }
                }
                dlg.dispose();
            } catch (Exception ex) { UIUtils.error(dlg, ex.getMessage()); }
        });
        dlg.setVisible(true);
    }

    // ── Data / filter ─────────────────────────────────────────────────────────
    private void refresh() {
        try { data = new ArrayList<>(demo ? MockDataProvider.items() : db.getAllItems()); }
        catch (Exception e) { data = new ArrayList<>(MockDataProvider.items()); }
        applyFilter();
    }

    private void applyFilter() {
        String q     = searchF.getText().toLowerCase();
        String avail = selectedOrEmpty(filterAvail);
        String type  = selectedOrEmpty(filterType);
        String cond  = selectedOrEmpty(filterCond);

        model.setRowCount(0);
        data.stream()
            .filter(i -> (avail.isEmpty() || avail.equals(i.getAvailabilityStatus()))
                      && (type.isEmpty()  || type.equals(i.getItemType()))
                      && (cond.isEmpty()  || cond.equals(i.getConditionStatus()))
                      && (q.isEmpty()     || i.getItemName().toLowerCase().contains(q)
                                          || i.getItemId().toLowerCase().contains(q)
                                          || (i.getModel() != null && i.getModel().toLowerCase().contains(q))))
            .forEach(i -> model.addRow(new Object[]{
                i.getItemId(), i.getItemName(), i.getItemType(), i.getModel(),
                i.getConditionStatus(), i.getAvailabilityStatus(), i.getDateAcquired()
            }));
    }

    private ItemRecord selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        String id = (String) model.getValueAt(row, 0);
        return data.stream().filter(i -> i.getItemId().equals(id)).findFirst().orElse(null);
    }

    private String selectedOrEmpty(JComboBox<String> cb) {
        String s = (String) cb.getSelectedItem();
        return s != null && s.startsWith("All") ? "" : s == null ? "" : s;
    }

    private String[] prepend(String first, String[] rest) {
        String[] r = new String[rest.length + 1];
        r[0] = first; System.arraycopy(rest, 0, r, 1, rest.length);
        return r;
    }

    private boolean itemIdTaken(String itemId) throws SQLException {
        if (demo) {
            return data.stream().anyMatch(i -> i.getItemId().equalsIgnoreCase(itemId));
        }
        return db.getItemById(itemId) != null;
    }

    /**
     * Validates against {@code item} column types, lengths, and CHECK constraints
     * ({@code item_type_chk}, {@code item_cond_chk}, {@code item_avail_chk}) and {@code DATE} for {@code dateAcquired}.
     *
     * @return an error message, or {@code null} if valid
     */
    private static String validateItemAgainstSchema(String id, String name, String itemType,
                                                    String conditionStatus, String availabilityStatus,
                                                    String model, String description, String dateAcquired) {
        if (id.isEmpty() || name.isEmpty()) {
            return "Item ID and Name are required.";
        }
        if (containsControlChars(id) || containsControlChars(name) || containsControlChars(model)
                || containsControlChars(description)) {
            return "Remove control characters from the form.";
        }
        if (!SQL_ITEM_ID_FORMAT.matcher(id).matches()) {
            return "That equipment ID doesn’t look right. Use the same style as the examples: "
                    + "start with I, then a dash, a short type code in CAPITAL letters (like LT or HDMI), "
                    + "another dash, then a number with at least two digits (like 03 or 01). "
                    + "Examples: I-LT-03 or I-HDMI-01. Don’t use spaces.";
        }
        if (sqlUtf8mb4CharLen(id) > SQL_ITEM_ID_MAX) {
            return "That equipment ID is too long. Use a shorter middle part or fewer digits at the end "
                    + "(the whole ID can’t be longer than " + SQL_ITEM_ID_MAX + " characters).";
        }
        if (sqlUtf8mb4CharLen(name) > SQL_ITEM_NAME_MAX) {
            return "Name must be at most " + SQL_ITEM_NAME_MAX + " characters (MySQL varchar(" + SQL_ITEM_NAME_MAX + ")).";
        }
        if (itemType == null || !Arrays.asList(TYPES).contains(itemType)) {
            return "Type must be one of: equipment, peripheral, accessory, tool.";
        }
        if (conditionStatus == null || !Arrays.asList(CONDS).contains(conditionStatus)) {
            return "Condition must be one of: working, damaged, under maintenance.";
        }
        if (availabilityStatus == null || !Arrays.asList(AVAILS).contains(availabilityStatus)) {
            return "Availability must be one of: available, borrowed, unavailable.";
        }
        String descStored = description.isEmpty() ? "—" : description;
        String modelStored = model.isEmpty() ? "—" : model;
        if (sqlUtf8mb4CharLen(descStored) > SQL_ITEM_DESC_MAX) {
            return "Description (or placeholder) must be at most " + SQL_ITEM_DESC_MAX + " characters (MySQL varchar(" + SQL_ITEM_DESC_MAX + ")).";
        }
        if (sqlUtf8mb4CharLen(modelStored) > SQL_ITEM_MODEL_MAX) {
            return "Model (or placeholder) must be at most " + SQL_ITEM_MODEL_MAX + " characters (MySQL varchar(" + SQL_ITEM_MODEL_MAX + ")).";
        }
        LocalDate acquired;
        try {
            acquired = LocalDate.parse(dateAcquired.trim());
        } catch (DateTimeParseException e) {
            return "Acquired date must be a valid calendar date in YYYY-MM-DD format.";
        }
        if (acquired.isBefore(LocalDate.of(1000, 1, 1)) || acquired.isAfter(LocalDate.of(9999, 12, 31))) {
            return "Acquired date is outside the range supported by MySQL DATE.";
        }
        return null;
    }

    private static int sqlUtf8mb4CharLen(String s) {
        return (int) s.codePoints().count();
    }

    private static boolean containsControlChars(String s) {
        return s.codePoints().anyMatch(Character::isISOControl);
    }
}
