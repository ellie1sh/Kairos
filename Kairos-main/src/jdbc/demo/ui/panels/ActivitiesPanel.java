package jdbc.demo.ui.panels;

import jdbc.demo.Database;
import jdbc.demo.model.ActivityRecord;
import jdbc.demo.model.FacilityRecord;
import jdbc.demo.model.UserRecord;
import jdbc.demo.ui.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivitiesPanel extends JPanel {

    private static final String[] COLS   = {"ID", "Activity Name", "Type", "Requester", "Date", "Facility", "Status"};
    private static final String[] TYPES  = {"Class Activity","Seminar","Meeting","Workshop","Conference","Other"};
    private static final String[] STATUS = {"Pending","Approved","Rejected","Cancelled"};

    private final UserRecord user;
    private final Database   db;
    private final boolean    demo;
    private final boolean    canManage;
    private final boolean    isCustodian;

    private List<ActivityRecord>  data          = new ArrayList<>();
    private List<FacilityRecord>  allFacilities = new ArrayList<>();
    private DefaultTableModel     model;
    private JTable               table;
    private JTextField           searchF;
    private JComboBox<String>    filterStatus, filterType;
    /** Borrowers (Student / Professor) submit new activity requests here; custodian approves in the same module. */
    private JButton               submitRequestBtn;
    private JButton               editBtn;

    public ActivitiesPanel(UserRecord user, Database db, boolean demo) {
        super(new BorderLayout());
        this.user      = user;
        this.db        = db;
        this.demo      = demo;
        this.canManage = "Admin".equals(user.getType()) || "Custodian".equals(user.getType());
        this.isCustodian = "Custodian".equals(user.getType());
        setBackground(UIColors.BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        build();
        refresh();
        if (isCustodian) {
            editBtn.setVisible(true);
            filterStatus.setSelectedIndex(0);
            applyFilter();
        }
    }

    /** Called from {@link jdbc.demo.ui.AppFrame} when the custodian opens the unified Activities sidebar item. */
    public void applyCustodianNavId(String navId) {
        if (!isCustodian || !"cu-activities".equals(navId)) {
            return;
        }
        loadActivityDataFromDatabase();
        editBtn.setVisible(true);
        filterStatus.setSelectedIndex(0);
        applyFilter();
    }

    /** When a borrower opens Activity Requests, reload so approval status matches the database. */
    public void onBorrowerActivitiesNav() {
        if (canManage) {
            return;
        }
        refresh();
    }

    // ── Auto-generate next Activity ID ───────────────────────────────────────
    private String generateActivityId() {
        return data.stream()
                .map(ActivityRecord::getActivityId)
                .filter(id -> id != null && id.matches("A\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(1)))
                .max()
                .stream()
                .mapToObj(max -> String.format("A%03d", max + 1))
                .findFirst()
                .orElse("A001");
    }

    private void build() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        searchF      = UIUtils.field("Search activities…"); searchF.setPreferredSize(new Dimension(180, 34));
        filterStatus = UIUtils.combo(prepend("All Statuses", STATUS));
        filterType   = UIUtils.combo(prepend("All Types", TYPES));
        searchF.addActionListener(e -> applyFilter());
        filterStatus.addActionListener(e -> applyFilter());
        filterType.addActionListener(e -> applyFilter());

        submitRequestBtn   = UIUtils.primaryBtn("+ Submit Request");
        editBtn            = UIUtils.secondaryBtn("Edit");
        JButton approveBtn = UIUtils.successBtn("Approve");
        JButton rejectBtn  = UIUtils.dangerBtn("Reject");
        JButton deleteBtn  = UIUtils.dangerBtn("Delete");
        JButton refBtn     = UIUtils.secondaryBtn("Refresh");

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.weightx = 1.0;
        left.add(searchF, gc);
        gc.gridx = 1; gc.weightx = 0;
        left.add(filterStatus, gc);
        gc.gridx = 2; gc.insets = new Insets(0, 0, 0, 0);
        left.add(filterType, gc);

        if (!canManage) {
            right.add(submitRequestBtn);
        }
        right.add(editBtn);
        if (canManage) { right.add(approveBtn); right.add(rejectBtn); right.add(deleteBtn); }
        right.add(refBtn);

        toolbar.add(left, BorderLayout.CENTER);
        toolbar.add(right, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setCellRenderer(UIUtils.monoRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(UIUtils.badgeRenderer());
        // Stretch all columns proportionally as the window resizes
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // ID | Activity Name | Type | Requester | Date | Facility | Status
        int[] widths = {60, 280, 140, 180, 110, 180, 110};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        add(toolbar, BorderLayout.NORTH);
        add(UIUtils.scrollPane(table), BorderLayout.CENTER);

        if (!canManage) {
            submitRequestBtn.addActionListener(e -> showForm(null));
        }
        editBtn.addActionListener(e -> {
            ActivityRecord s = selected();
            if (s == null) { UIUtils.error(this,"Select an activity."); return; }
            if (!canOpenEditForm(s)) {
                return;
            }
            showForm(s);
        });
        approveBtn.addActionListener(e -> {
            ActivityRecord s = selected();
            if (s == null) { UIUtils.error(this,"Select an activity."); return; }
            if (!"Pending".equals(s.getStatus())) { UIUtils.error(this,"Only Pending activities can be approved."); return; }
            try {
                if (!demo) db.approveActivity(s.getActivityId(), user.getUserId(), "Approved via GUI");
                replaceStatus(s.getActivityId(), "Approved");
                applyFilter();
            } catch (Exception ex) { UIUtils.error(this, ex.getMessage()); }
        });
        rejectBtn.addActionListener(e -> {
            ActivityRecord s = selected();
            if (s == null) { UIUtils.error(this,"Select an activity."); return; }
            if (!"Pending".equals(s.getStatus())) { UIUtils.error(this,"Only Pending activities can be rejected."); return; }
            try {
                if (!demo) db.rejectActivity(s.getActivityId(), user.getUserId(), "Rejected via GUI");
                replaceStatus(s.getActivityId(), "Rejected");
                applyFilter();
            } catch (Exception ex) { UIUtils.error(this, ex.getMessage()); }
        });
        deleteBtn.addActionListener(e -> {
            ActivityRecord s = selected();
            if (s == null) { UIUtils.error(this,"Select an activity."); return; }
            if (!UIUtils.confirm(this,"Delete activity '" + s.getActivityName() + "'?")) return;
            try {
                if (!demo) db.deleteActivity(s.getActivityId(), user.getUserId());
                data.removeIf(a -> a.getActivityId().equals(s.getActivityId())); applyFilter();
            } catch (Exception ex) { UIUtils.error(this, ex.getMessage()); }
        });
        refBtn.addActionListener(e -> refresh());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                if (ev.getClickCount() != 2) return;
                ActivityRecord s = selected();
                if (s != null && canOpenEditForm(s)) {
                    showForm(s);
                }
            }
        });
    }

    /**
     * Borrowers may only revise their own requests while status is Pending (custodian has not decided yet).
     */
    private boolean canOpenEditForm(ActivityRecord act) {
        if (canManage) {
            return true;
        }
        if (act.getRequesterId() != user.getUserId()) {
            UIUtils.error(this, "You can only edit your own activity requests.");
            return false;
        }
        if (!"Pending".equals(act.getStatus())) {
            UIUtils.error(this, "Only pending requests can be edited. Contact the custodian if you need changes.");
            return false;
        }
        return true;
    }

    private void showForm(ActivityRecord act) {
        boolean isNew = (act == null);

        // Generate ID before opening the dialog (only needed for new records)
        String generatedId = isNew ? generateActivityId() : act.getActivityId();

        String dialogHeading = isNew ? "Submit Activity Request" : "Edit Activity";
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                dialogHeading, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 540);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = UIUtils.dialogPanel(dialogHeading, UIColors.PRIMARY);

        JPanel form = UIUtils.formPanel();
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);

        // Auto-generated ID shown as read-only label for new; non-editable field for edit
        JLabel fIdDisplay = new JLabel(generatedId);
        fIdDisplay.setFont(UIUtils.F_MONO);
        fIdDisplay.setForeground(UIColors.TEXT_SECONDARY);

        JTextField fName    = UIUtils.field("Activity name"); fName.setPreferredSize(new Dimension(240, 34));
        JTextField fReqDate = UIUtils.field("YYYY-MM-DD");    fReqDate.setPreferredSize(new Dimension(240, 34));
        JTextField fActDate = UIUtils.field("YYYY-MM-DD");    fActDate.setPreferredSize(new Dimension(240, 34));
        // Build facility dropdown: "— None —" + all facility names + "Other (type below)"
        String[] facilityOptions = new String[allFacilities.size() + 2];
        facilityOptions[0] = "— None —";
        for (int i = 0; i < allFacilities.size(); i++) {
            facilityOptions[i + 1] = allFacilities.get(i).getFacilityName();
        }
        facilityOptions[facilityOptions.length - 1] = "Other (type below)";

        JComboBox<String> fFacility  = UIUtils.combo(facilityOptions);
        fFacility.setPreferredSize(new Dimension(240, 34));
        JTextField fFacilityOther = UIUtils.field("Type facility name…");
        fFacilityOther.setPreferredSize(new Dimension(240, 34));
        fFacilityOther.setVisible(false); // hidden unless "Other" is selected

        fFacility.addActionListener(e -> {
            String sel = (String) fFacility.getSelectedItem();
            boolean isOther = "Other (type below)".equals(sel);
            fFacilityOther.setVisible(isOther);
            dlg.revalidate();
            dlg.repaint();
        });
        JTextArea  fRemarks = UIUtils.textArea(2);
        JComboBox<String> fType = UIUtils.combo(TYPES);

        if (!isNew) {
            fName.setText(act.getActivityName());
            fType.setSelectedItem(act.getActivityType());
            fReqDate.setText(act.getRequestDate());
            fActDate.setText(act.getActivityDate());
            // Pre-select matching facility name, fall back to "Other (type below)"
            if (act.getFacilityName() != null) {
                boolean found = false;
                for (int i = 1; i < facilityOptions.length - 1; i++) {
                    if (facilityOptions[i].equals(act.getFacilityName())) {
                        fFacility.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    fFacility.setSelectedItem("Other (type below)");
                    fFacilityOther.setText(act.getFacilityName());
                    fFacilityOther.setVisible(true);
                }
            }
            fRemarks.setText(act.getRemarks() == null ? "" : act.getRemarks());
        } else {
            fReqDate.setText(LocalDate.now().toString());
        }

        UIUtils.formRow(form, gc, "Activity ID",     fIdDisplay,   0);
        UIUtils.formRow(form, gc, "Activity Name *", fName,        1);
        UIUtils.formRow(form, gc, "Type",            fType,        2);
        UIUtils.formRow(form, gc, "Request Date",    fReqDate,     3);
        UIUtils.formRow(form, gc, "Activity Date *", fActDate,     4);
        UIUtils.formRow(form, gc, "Facility",        fFacility,    5);
        UIUtils.formRow(form, gc, "",                fFacilityOther, 6);
        UIUtils.formRow(form, gc, "Remarks",         new JScrollPane(fRemarks) {{ setPreferredSize(new Dimension(240, 56)); setBorder(null); }}, 7);

        JButton save   = UIUtils.primaryBtn(isNew ? "Submit Request" : "Update");
        JButton cancel = UIUtils.secondaryBtn("Cancel");

        outer.add(form, BorderLayout.CENTER);
        outer.add(UIUtils.buttonBar(cancel, save), BorderLayout.SOUTH);

        dlg.setContentPane(outer);
        cancel.addActionListener(e -> dlg.dispose());
        save.addActionListener(e -> {
            String name    = fName.getText().trim();
            String actDate = fActDate.getText().trim();
            if (name.isEmpty() || actDate.isEmpty()) {
                UIUtils.error(dlg, "Activity name and activity date are required."); return;
            }
            try {
                String type    = (String) fType.getSelectedItem();
                String reqDate = fReqDate.getText().trim();
                String facSel  = (String) fFacility.getSelectedItem();
                String fac;
                if ("Other (type below)".equals(facSel)) {
                    fac = fFacilityOther.getText().trim();
                } else {
                    fac = (facSel == null || facSel.startsWith("—")) ? "" : facSel;
                }
                String remarks = fRemarks.getText().trim();

                if (isNew) {
                    if (!demo) {
                        db.submitActivityRequest(generatedId, user.getUserId(), reqDate, name, type, actDate, remarks);
                        linkFacilityAfterSubmit(generatedId, fac);
                        refresh();
                    } else {
                        data.add(new ActivityRecord(generatedId,
                                user.getUserId(),
                                user.getFirstName() + " " + user.getLastName(),
                                null, name, type, reqDate, actDate, "Pending",
                                fac.isEmpty() ? null : fac, remarks));
                        applyFilter();
                    }
                } else {
                    if (!demo) {
                        db.updateActivity(generatedId, name, type, actDate, remarks, user.getUserId());
                        relinkFacilityOnEdit(generatedId, fac);
                    }
                    data.replaceAll(a -> a.getActivityId().equals(generatedId)
                            ? new ActivityRecord(generatedId, a.getRequesterId(), a.getRequesterName(), a.getApprovedByName(),
                            name, type, a.getRequestDate(), actDate, a.getStatus(),
                            fac.isEmpty() ? null : fac, remarks) : a);
                    applyFilter();
                }
                dlg.dispose();
            } catch (Exception ex) { UIUtils.error(dlg, ex.getMessage()); }
        });
        dlg.pack();
        dlg.setSize(480, Math.max(dlg.getHeight(), 540));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    /** Loads rows from MySQL (or mock when demo / fallback). Does not run filters. */
    private void loadActivityDataFromDatabase() {
        try { data = new ArrayList<>(demo ? MockDataProvider.activities() : db.getAllActivities()); }
        catch (Exception e) { data = new ArrayList<>(MockDataProvider.activities()); }
        try { allFacilities = new ArrayList<>(demo ? MockDataProvider.facilities() : db.getAllFacilities()); }
        catch (Exception e) { allFacilities = new ArrayList<>(MockDataProvider.facilities()); }
    }

    /** After SubmitActivityRequest, attach a known facility so the row appears with a facility in custodian views. */
    private void linkFacilityAfterSubmit(String activityId, String chosenFacilityName) {
        if (demo || chosenFacilityName == null || chosenFacilityName.isBlank()) {
            return;
        }
        for (FacilityRecord fr : allFacilities) {
            if (fr.getFacilityName().equals(chosenFacilityName)) {
                try {
                    db.addFacilityToActivity(activityId, fr.getFacilityId(), user.getUserId());
                } catch (Exception ignored) {
                    /* duplicate link or DB constraint — activity row still valid */
                }
                return;
            }
        }
    }

    /** Keep activitydetails in sync with the selected facility during Edit Activity. */
    private void relinkFacilityOnEdit(String activityId, String chosenFacilityName) {
        if (demo) {
            return;
        }
        // Remove existing links first (idempotent; ignore "not linked" errors).
        for (FacilityRecord fr : allFacilities) {
            try {
                db.removeFacilityFromActivity(activityId, fr.getFacilityId(), user.getUserId());
            } catch (Exception ignored) {
                // Not linked or constraint message; safe to continue.
            }
        }
        // Link the selected known facility, if any.
        if (chosenFacilityName == null || chosenFacilityName.isBlank()) {
            return;
        }
        for (FacilityRecord fr : allFacilities) {
            if (fr.getFacilityName().equals(chosenFacilityName)) {
                try {
                    db.addFacilityToActivity(activityId, fr.getFacilityId(), user.getUserId());
                } catch (Exception ignored) {
                    // Duplicate/constraint edge case; edited activity is still valid.
                }
                return;
            }
        }
    }

    private void refresh() {
        loadActivityDataFromDatabase();
        applyFilter();
    }

    private void applyFilter() {
        String q      = searchF.getText().toLowerCase();
        String status = selectedOrEmpty(filterStatus);
        String type   = selectedOrEmpty(filterType);
        boolean isBorrower = !canManage;
        model.setRowCount(0);
        data.stream()
                .filter(a -> (!isBorrower || a.getRequesterId() == user.getUserId())
                        && statusMatchesFilter(a.getStatus(), status)
                        && (type.isEmpty()   || type.equals(a.getActivityType()))
                        && (q.isEmpty()      || a.getActivityName().toLowerCase().contains(q)
                        || a.getRequesterName().toLowerCase().contains(q)))
                .forEach(a -> model.addRow(new Object[]{
                        a.getActivityId(), a.getActivityName(), a.getActivityType(),
                        a.getRequesterName(), a.getActivityDate(),
                        a.getFacilityName() == null ? "—" : a.getFacilityName(),
                        a.getStatus()
                }));
    }

    private void replaceStatus(String id, String newStatus) {
        data.replaceAll(a -> a.getActivityId().equals(id)
                ? new ActivityRecord(a.getActivityId(), a.getRequesterId(), a.getRequesterName(),
                user.getFirstName() + " " + user.getLastName(),
                a.getActivityName(), a.getActivityType(), a.getRequestDate(), a.getActivityDate(),
                newStatus, a.getFacilityName(), a.getRemarks()) : a);
    }

    private ActivityRecord selected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        String id = (String) model.getValueAt(row, 0);
        return data.stream().filter(a -> a.getActivityId().equals(id)).findFirst().orElse(null);
    }

    private boolean statusMatchesFilter(String rowStatusRaw, String selectedStatus) {
        String row = rowStatusRaw == null ? "" : rowStatusRaw.trim();
        if (selectedStatus.isEmpty()) {
            return true;
        }
        return row.equals(selectedStatus.trim());
    }

    private String selectedOrEmpty(JComboBox<String> cb) {
        String s = (String) cb.getSelectedItem();
        return s != null && s.startsWith("All") ? "" : s == null ? "" : s;
    }
    private String[] prepend(String f, String[] r) {
        String[] a = new String[r.length + 1]; a[0] = f; System.arraycopy(r, 0, a, 1, r.length); return a;
    }
}