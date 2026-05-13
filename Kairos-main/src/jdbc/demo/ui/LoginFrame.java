package jdbc.demo.ui;

import jdbc.demo.Database;
import jdbc.demo.model.UserRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private final JTextField   emailField;
    private final JPasswordField passField;
    private final JLabel       errorLabel;
    private final JButton      loginBtn;

    // Demo accounts — credentials that actually exist in the database
    private static final String[][] DEMOS = {
        {"Admin",     "admin@kairos.edu",      "admin123", "#7C3AED"},
        {"Custodian", "gv140@slu.edu.ph",      "cust123",  "#2563EB"},
        {"Professor", "nr080@slu.edu.ph",      "prof123",  "#0891B2"},
        {"Student",   "sr070@slu.edu.ph",      "stud123",  "#16A34A"},
    };

    public LoginFrame() {
        super("Kairos Borrowing System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 600);
        setMinimumSize(new Dimension(760, 540));
        setLocationRelativeTo(null);
        setResizable(true);

        emailField = UIUtils.field("you@kairos.edu");
        passField  = UIUtils.passwordField();
        errorLabel = new JLabel(" ");
        loginBtn   = UIUtils.primaryBtn("Sign In");

        setContentPane(buildContent());

        ActionListener submit = e -> doLogin();
        emailField.addActionListener(submit);
        passField.addActionListener(submit);
        loginBtn.addActionListener(submit);
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildLeftPanel());
        root.add(buildRightPanel());
        return root;
    }

    /** Dark hero panel with branding */
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        getWidth(), getHeight(), new Color(30, 58, 138));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Decorative circles
                g2.setColor(new Color(37, 99, 235, 25));
                g2.fillOval(-80, -80, 280, 280);
                g2.setColor(new Color(16, 185, 129, 18));
                g2.fillOval(getWidth() - 180, getHeight() - 180, 320, 320);
                g2.setColor(new Color(124, 58, 237, 12));
                g2.fillOval(getWidth() / 2 - 100, getHeight() / 2 - 100, 200, 200);
                g2.dispose();
            }
        };
        p.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.anchor = GridBagConstraints.CENTER;

        // Icon circle
        JPanel iconCircle = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIColors.PRIMARY, getWidth(), getHeight(), UIColors.SUCCESS);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(76, 76));
        JLabel clock = new JLabel("⏱", SwingConstants.CENTER);
        clock.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        clock.setForeground(Color.WHITE);
        iconCircle.add(clock, BorderLayout.CENTER);

        JLabel appName = new JLabel("KAIROS", SwingConstants.CENTER);
        appName.setFont(new Font("Segoe UI", Font.BOLD, 42));
        appName.setForeground(Color.WHITE);

        JLabel subTitle = new JLabel("Borrowing System", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subTitle.setForeground(new Color(147, 197, 253));

        JLabel desc = new JLabel(
                "<html><div style='text-align:center;width:220px;color:#94A3B8;font-size:12px;line-height:1.6'>"
                + "Manage equipment borrowing, activity scheduling, and facility usage in one place."
                + "</div></html>", SwingConstants.CENTER);

        JPanel featurePanel = buildFeatureChips();

        gc.gridy = 0; gc.insets = new Insets(0, 0, 20, 0); p.add(iconCircle, gc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 5, 0);  p.add(appName, gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 16, 0); p.add(subTitle, gc);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 24, 0); p.add(desc, gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 0, 0);  p.add(featurePanel, gc);
        return p;
    }

    private JPanel buildFeatureChips() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        p.setOpaque(false);
        String[] chips = {"Equipment Tracking", "Activity Scheduling", "User Roles", "Reports"};
        for (String c : chips) {
            JLabel chip = new JLabel(c);
            chip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            chip.setForeground(new Color(147, 197, 253));
            chip.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(59, 130, 246, 70), 1, true),
                    new EmptyBorder(4, 12, 4, 12)));
            p.add(chip);
        }
        return p;
    }

    /** White form panel */
    private JPanel buildRightPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(248, 250, 252));

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UIColors.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(32, 40, 32, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;

        // Header
        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcome.setForeground(UIColors.TEXT);

        JLabel sub = new JLabel("Sign in to your account to continue");
        sub.setFont(UIUtils.F_SMALL);
        sub.setForeground(UIColors.TEXT_SECONDARY);

        gc.gridy = 0; gc.insets = new Insets(0, 0, 3, 0); card.add(welcome, gc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 20, 0); card.add(sub, gc);

        // Error label
        errorLabel.setFont(UIUtils.F_SMALL);
        errorLabel.setForeground(UIColors.DANGER);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 6, 0); card.add(errorLabel, gc);

        // Email
        JLabel emailLbl = fieldLabel("EMAIL ADDRESS");
        gc.gridy = 3; gc.insets = new Insets(0, 0, 5, 0); card.add(emailLbl, gc);
        emailField.setPreferredSize(new Dimension(300, 40));
        gc.gridy = 4; gc.insets = new Insets(0, 0, 16, 0); card.add(emailField, gc);

        // Password
        JLabel passLbl = fieldLabel("PASSWORD");
        gc.gridy = 5; gc.insets = new Insets(0, 0, 5, 0); card.add(passLbl, gc);

        // Password row with show/hide toggle
        JPanel passRow = buildPasswordRow();
        gc.gridy = 6; gc.insets = new Insets(0, 0, 22, 0); card.add(passRow, gc);

        // Login button
        loginBtn.setPreferredSize(new Dimension(300, 42));
        gc.gridy = 7; gc.insets = new Insets(0, 0, 20, 0); card.add(loginBtn, gc);

        // Divider with label
        gc.gridy = 8; gc.insets = new Insets(0, 0, 14, 0); card.add(buildDivider("DEMO ACCOUNTS"), gc);

        // Demo buttons — 2×2 grid
        JPanel demoGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        demoGrid.setOpaque(false);
        for (String[] d : DEMOS) {
            demoGrid.add(makeDemoBtn(d[0], d[1], d[2], Color.decode(d[3])));
        }
        gc.gridy = 9; gc.insets = new Insets(0, 0, 6, 0); card.add(demoGrid, gc);

        JLabel hint = UIUtils.muted("Click a role to auto-fill credentials");
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        gc.gridy = 10; gc.insets = new Insets(0, 0, 0, 0); card.add(hint, gc);

        outer.add(card);
        return outer;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(UIColors.TEXT_SECONDARY);
        return l;
    }

    private JPanel buildPasswordRow() {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        passField.setPreferredSize(new Dimension(250, 40));

        JButton toggle = new JButton("👁") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(241, 245, 249) : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(UIColors.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        toggle.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        toggle.setPreferredSize(new Dimension(42, 40));
        toggle.setContentAreaFilled(false);
        toggle.setBorderPainted(false);
        toggle.setFocusPainted(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.setToolTipText("Show/hide password");

        final boolean[] visible = {false};
        toggle.addActionListener(e -> {
            visible[0] = !visible[0];
            passField.setEchoChar(visible[0] ? (char) 0 : '•');
        });

        row.add(passField, BorderLayout.CENTER);
        row.add(toggle,    BorderLayout.EAST);
        return row;
    }

    private JPanel buildDivider(String label) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();

        JSeparator left = new JSeparator();
        left.setForeground(UIColors.BORDER);
        gc.gridx = 0; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        p.add(left, gc);

        JLabel lbl = new JLabel("  " + label + "  ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UIColors.TEXT_MUTED);
        gc.gridx = 1; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        p.add(lbl, gc);

        JSeparator right = new JSeparator();
        right.setForeground(UIColors.BORDER);
        gc.gridx = 2; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        p.add(right, gc);

        return p;
    }

    private JButton makeDemoBtn(String role, String email, String pwd, Color color) {
        Color bgNormal = new Color(color.getRed(), color.getGreen(), color.getBlue(), 15);
        Color bgHover  = new Color(color.getRed(), color.getGreen(), color.getBlue(), 35);
        Color border   = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);

        JButton b = new JButton(role) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bgHover : bgNormal);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(UIUtils.F_BOLD);
        b.setForeground(color);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText(email);
        b.addActionListener(e -> {
            emailField.setText(email);
            passField.setText(pwd);
            errorLabel.setText(" ");
        });
        return b;
    }

    // ── Login logic ──────────────────────────────────────────────────────────
    private void doLogin() {
        String email = emailField.getText().trim();
        String pass  = new String(passField.getPassword());
        if (email.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }
        loginBtn.setEnabled(false);
        loginBtn.setText("Signing in…");
        errorLabel.setText(" ");

        SwingWorker<UserRecord, Void> worker = new SwingWorker<>() {
            Database db;
            String dbError = null;

            @Override protected UserRecord doInBackground() {
                try {
                    db = new Database();
                    return db.login(email, pass);
                } catch (Exception ex) {
                    dbError = ex.getMessage();
                    return null;
                }
            }

            @Override protected void done() {
                loginBtn.setEnabled(true);
                loginBtn.setText("Sign In");
                try {
                    UserRecord user = get();
                    if (dbError != null) {
                        errorLabel.setText("<html>DB error: " + dbError + "</html>");
                        return;
                    }
                    if (user == null) {
                        errorLabel.setText("Invalid email or password.");
                        return;
                    }
                    AppFrame app = new AppFrame(user, db, false);
                    dispose();
                    app.setVisible(true);
                } catch (Exception ex) {
                    errorLabel.setText("Login error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
