package jdbc.demo.ui.panels;

import jdbc.demo.Database;
import jdbc.demo.model.FacilityRecord;
import jdbc.demo.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FacilitiesPanel extends JPanel {

    private static final String[] COLS = {"Facility ID", "Facility Name"};
    private static final int SQL_FACILITY_ID_MAX = 6;
    private static final int SQL_FACILITY_NAME_MAX = 25;
    private static final Pattern SQL_FACILITY_ID_FORMAT = Pattern.compile("^F\\d{3}$");

    private final Database db;
    private final boolean  demo;

    private List<FacilityRecord> data = new ArrayList<>();
    private DefaultTableModel    model;
    private JTable               table;
    private JTextField           searchF;

    public FacilitiesPanel(Object user, Database db, boolean demo) {
        super(new BorderLayout());
        this.db   = db;
        this.demo = demo;
        setBackground(UIColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        build();
        refresh();
    }

    private void build() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));

        searchF = UIUtils.field("Search…"); searchF.setPreferredSize(new Dimension(220, 34));
        searchF.addActionListener(e -> applyFilter());

        JButton addBtn = UIUtils.primaryBtn("+ Add Facility");
        JButton editBtn = UIUtils.secondaryBtn("Edit");
        JButton delBtn  = UIUtils.dangerBtn("Delete");
        JButton refBtn  = UIUtils.secondaryBtn("Refresh");

        toolbar.add(searchF); toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(delBtn);
        toolbar.add(Box.createHorizontalStrut(4)); toolbar.add(refBtn);

        model = new DefaultTableModel(COLS, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setCellRenderer(UIUtils.monoRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);

        add(toolbar, BorderLayout.NORTH);
        add(UIUtils.scrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> showForm(null));
        editBtn.addActionListener(e -> { FacilityRecord s = selected(); if (s == null) { UIUtils.error(this,"Select a facility."); return; } showForm(s); });
        delBtn.addActionListener(e -> {
            FacilityRecord s = selected();
            if (s == null) { UIUtils.error(this,"Select a facility."); return; }
            if (!UIUtils.confirm(this,"Delete facility '" + s.getFacilityName() + "'?")) return;
            try {
                if (!demo) db.deleteFacility(s.getFacilityId());
                data.removeIf(f -> f.getFacilityId().equals(s.getFacilityId())); applyFilter();
            } catch (Exception ex) { UIUtils.error(this, ex.getMessage()); }
        });
        refBtn.addActionListener(e -> refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                if (ev.getClickCount() == 2) { FacilityRecord s = selected(); if (s != null) showForm(s); }
            }
        });
    }

    private void showForm(FacilityRecord fac) {
        boolean isNew = (fac == null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                isNew ? "Add Facility" : "Edit Facility", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = UIUtils.dialogPanel(isNew ? "Add Facility" : "Edit Facility", UIColors.CYAN);
        JPanel form  = UIUtils.formPanel();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 4, 7, 4);

        JTextField fId   = UIUtils.field("e.g. F011"); fId.setPreferredSize(new Dimension(220, 34));
        JTextField fName = UIUtils.field("Facility name"); fName.setPreferredSize(new Dimension(220, 34));

        if (!isNew) { fId.setText(fac.getFacilityId()); fId.setEditable(false); fName.setText(fac.getFacilityName()); }

        UIUtils.formRow(form, gc, "Facility ID *",   fId,   0);
        UIUtils.formRow(form, gc, "Facility Name *", fName, 1);

        JButton save   = UIUtils.primaryBtn(isNew ? "Add Facility" : "Update");
        JButton cancel = UIUtils.secondaryBtn("Cancel");

        outer.add(form, BorderLayout.CENTER);
        outer.add(UIUtils.buttonBar(cancel, save), BorderLayout.SOUTH);
        dlg.setContentPane(outer);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String id = fId.getText().trim().toUpperCase();
            String name = fName.getText().trim();
            String err = validateFacilityAgainstSchema(id, name);
            if (err != null) {
                UIUtils.error(dlg, err);
                return;
            }
            try {
                if (isNew) {
                    if (!demo) db.addFacility(id, name);
                    data.add(new FacilityRecord(id, name));
                } else {
                    if (!demo) db.updateFacility(id, name);
                    data.replaceAll(f -> f.getFacilityId().equals(id) ? new FacilityRecord(id, name) : f);
                }
                applyFilter(); dlg.dispose();
            } catch (Exception ex) { UIUtils.error(dlg, ex.getMessage()); }
        });
        dlg.setVisible(true);
    }

    private static String validateFacilityAgainstSchema(String id, String name) {
        if (id.isEmpty() || name.isEmpty()) {
            return "Both fields are required.";
        }
        if (containsControlChars(id) || containsControlChars(name)) {
            return "Remove hidden/special control characters.";
        }
        if (!SQL_FACILITY_ID_FORMAT.matcher(id).matches()) {
            return "Facility ID format must be F followed by 3 digits (e.g., F011).";
        }
        if (sqlUtf8mb4CharLen(id) > SQL_FACILITY_ID_MAX) {
            return "Facility ID must be at most " + SQL_FACILITY_ID_MAX + " characters.";
        }
        if (sqlUtf8mb4CharLen(name) > SQL_FACILITY_NAME_MAX) {
            return "Facility Name must be at most " + SQL_FACILITY_NAME_MAX + " characters.";
        }
        return null;
    }

    private static int sqlUtf8mb4CharLen(String s) {
        return (int) s.codePoints().count();
    }

    private static boolean containsControlChars(String s) {
        return s != null && s.codePoints().anyMatch(Character::isISOControl);
    }

    private void refresh() {
        try { data = new ArrayList<>(demo ? MockDataProvider.facilities() : db.getAllFacilities()); }
        catch (Exception e) { data = new ArrayList<>(MockDataProvider.facilities()); }
        applyFilter();
    }

    private void applyFilter() {
        String q = searchF.getText().toLowerCase();
        model.setRowCount(0);
        data.stream()
            .filter(f -> q.isEmpty() || f.getFacilityName().toLowerCase().contains(q) || f.getFacilityId().toLowerCase().contains(q))
            .forEach(f -> model.addRow(new Object[]{f.getFacilityId(), f.getFacilityName()}));
    }

    private FacilityRecord selected() {
        int row = table.getSelectedRow(); if (row < 0) return null;
        String id = (String) model.getValueAt(row, 0);
        return data.stream().filter(f -> f.getFacilityId().equals(id)).findFirst().orElse(null);
    }
}
