package jdbc.demo.ui;

import jdbc.demo.Database;
import jdbc.demo.model.UserRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FirstLoginDialog extends JDialog {

    private boolean passwordChanged = false;

    public FirstLoginDialog(Frame parent, UserRecord user, Database db) {
        super(parent, "Change Your Password", true);
        setSize(420, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // force password change
        setLayout(new BorderLayout(10, 10));

        // ── Message ──────────────────────────────────────────────────────────
        JLabel msg = new JLabel(
                "<html><b>Welcome, " + user.getFirstName() + "!</b><br><br>"
                        + "For your security, please set a new password before continuing.</html>");
        msg.setBorder(new EmptyBorder(16, 20, 0, 20));
        add(msg, BorderLayout.NORTH);

        // ── Fields ───────────────────────────────────────────────────────────
        JPanel fields = new JPanel(new GridLayout(2, 2, 8, 10));
        fields.setBorder(new EmptyBorder(10, 20, 10, 20));
        JPasswordField newPass     = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();
        fields.add(new JLabel("New Password:"));
        fields.add(newPass);
        fields.add(new JLabel("Confirm Password:"));
        fields.add(confirmPass);
        add(fields, BorderLayout.CENTER);

        // ── Button ───────────────────────────────────────────────────────────
        JButton confirmBtn = UIUtils.primaryBtn("Set Password");
        JPanel btnPanel = new JPanel();
        btnPanel.add(confirmBtn);
        add(btnPanel, BorderLayout.SOUTH);

        confirmBtn.addActionListener(e -> {
            String np = new String(newPass.getPassword());
            String cp = new String(confirmPass.getPassword());

            if (np.isBlank()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty.");
                return;
            }
            if (np.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
                return;
            }
            if (!np.equals(cp)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }
            try {
                db.changePassword(user.getUserId(), np);  // saves new hash to DB
                passwordChanged = true;
                JOptionPane.showMessageDialog(this, "Password updated! Please log in again.");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }

    public boolean isPasswordChanged() { return passwordChanged; }
}