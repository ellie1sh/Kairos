package jdbc.demo.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public final class UIUtils {

    // ── Fonts ───────────────────────────────────────────────────────────────
    public static final Font F_BODY     = new Font("Segoe UI", Font.PLAIN,  13);
    public static final Font F_SMALL    = new Font("Segoe UI", Font.PLAIN,  11);
    public static final Font F_BOLD     = new Font("Segoe UI", Font.BOLD,   13);
    public static final Font F_LABEL    = new Font("Segoe UI", Font.BOLD,   11);
    public static final Font F_TITLE    = new Font("Segoe UI", Font.BOLD,   18);
    public static final Font F_HEADING  = new Font("Segoe UI", Font.BOLD,   24);
    public static final Font F_MONO     = new Font("Consolas",  Font.PLAIN,  12);

    private UIUtils() {}

    // ── Buttons ─────────────────────────────────────────────────────────────
    public static JButton primaryBtn(String text) {
        return colorBtn(text, UIColors.PRIMARY, UIColors.PRIMARY_DARK, Color.WHITE);
    }
    public static JButton successBtn(String text) {
        return colorBtn(text, UIColors.SUCCESS, new Color(15, 118, 55), Color.WHITE);
    }
    public static JButton dangerBtn(String text) {
        return colorBtn(text, UIColors.DANGER, new Color(185, 28, 28), Color.WHITE);
    }
    public static JButton secondaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setBackground(UIColors.CARD_BG);
        b.setForeground(UIColors.TEXT);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIColors.BORDER, 1, true),
                new EmptyBorder(5, 14, 5, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    public static JButton warningBtn(String text) {
        return colorBtn(text, UIColors.WARNING, new Color(180, 83, 9), Color.WHITE);
    }

    private static JButton colorBtn(String text, Color bg, Color hover, Color fg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? hover
                        : getModel().isRollover() ? hover : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(F_BOLD);
        b.setForeground(fg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 16, 6, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── Input fields ─────────────────────────────────────────────────────────
    public static JTextField field(String placeholder) {
        JTextField f = new JTextField(20);
        f.setFont(F_BODY);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        styleBorder(f);
        return f;
    }
    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField(20);
        f.setFont(F_BODY);
        styleBorder(f);
        return f;
    }
    public static JTextArea textArea(int rows) {
        JTextArea ta = new JTextArea(rows, 20);
        ta.setFont(F_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        styleBorder(ta);
        return ta;
    }
    public static JComboBox<String> combo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(F_BODY);
        c.setBackground(UIColors.CARD_BG);
        return c;
    }
    private static void styleBorder(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIColors.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
    }

    // ── Labels ───────────────────────────────────────────────────────────────
    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_TITLE);
        l.setForeground(UIColors.TEXT);
        return l;
    }
    public static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(F_LABEL);
        l.setForeground(UIColors.TEXT_SECONDARY);
        return l;
    }
    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_SMALL);
        l.setForeground(UIColors.TEXT_MUTED);
        return l;
    }

    // ── Badge label ──────────────────────────────────────────────────────────
    public static JLabel badge(String text) {
        JLabel l = new JLabel(text == null ? "" : text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(F_LABEL);
        l.setOpaque(false);
        String t = text == null ? "" : text;
        l.setForeground(UIColors.badgeFg(t));
        l.setBackground(UIColors.badgeBg(t));
        l.setBorder(new EmptyBorder(2, 8, 2, 8));
        return l;
    }

    // ── Card panel ──────────────────────────────────────────────────────────
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIColors.CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UIColors.BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        return p;
    }

    /**
     * White rounded panel with a light drop shadow (dashboard-style tiles).
     */
    public static JPanel elevatedCard(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                int arc = 14;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42, 24));
                g2.fillRoundRect(2, 4, w - 4, h - 4, arc, arc);
                g2.setColor(new Color(15, 23, 42, 12));
                g2.fillRoundRect(1, 2, w - 2, h - 2, arc, arc);
                g2.setColor(UIColors.CARD_BG);
                g2.fillRoundRect(0, 0, w - 2, h - 2, arc, arc);
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, w - 2, h - 2, arc, arc);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        return p;
    }

    // ── Separator ────────────────────────────────────────────────────────────
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UIColors.BORDER);
        return sep;
    }

    // ── Scroll pane ──────────────────────────────────────────────────────────
    public static JScrollPane scrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(UIColors.BORDER, 1));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        return sp;
    }

    // ── Table setup ──────────────────────────────────────────────────────────
    public static void styleTable(JTable t) {
        t.setFont(F_BODY);
        t.setRowHeight(38);
        t.setGridColor(UIColors.BORDER);
        t.setShowGrid(true);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(UIColors.TABLE_SELECT);
        t.setSelectionForeground(UIColors.TEXT);
        t.setFocusable(false);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        var header = t.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(UIColors.TABLE_HEADER);
        header.setForeground(UIColors.TEXT_SECONDARY);
        header.setBorder(new LineBorder(UIColors.BORDER, 1));
        header.setReorderingAllowed(false);
        // Default alternating renderer for all columns
        setDefaultRenderer(t);
    }

    public static void setDefaultRenderer(JTable t) {
        var renderer = altRenderer();
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    public static DefaultTableCellRenderer altRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
                setBorder(new EmptyBorder(4, 12, 4, 12));
                setFont(F_BODY);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                    setForeground(UIColors.TEXT);
                }
                return this;
            }
        };
    }

    public static DefaultTableCellRenderer badgeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean focus, int row, int col) {
                String text = val == null ? "" : val.toString();
                JLabel lbl = new JLabel(text) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lbl.setOpaque(false);
                lbl.setFont(F_LABEL);
                lbl.setHorizontalAlignment(CENTER);
                if (sel) {
                    lbl.setBackground(tbl.getSelectionBackground());
                    lbl.setForeground(tbl.getSelectionForeground());
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                    // Small inner badge
                    JPanel wrapper = new JPanel(new GridBagLayout());
                    wrapper.setBackground(row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW);
                    JLabel badge = new JLabel(text) {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(getBackground());
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                            g2.dispose();
                            super.paintComponent(g);
                        }
                    };
                    badge.setOpaque(false);
                    badge.setFont(F_LABEL);
                    badge.setForeground(UIColors.badgeFg(text));
                    badge.setBackground(UIColors.badgeBg(text));
                    badge.setBorder(new EmptyBorder(2, 8, 2, 8));
                    wrapper.add(badge);
                    return wrapper;
                }
                return lbl;
            }
        };
    }

    public static DefaultTableCellRenderer monoRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, focus, row, col);
                setFont(new Font("Consolas", Font.BOLD, 12));
                setForeground(sel ? tbl.getSelectionForeground() : UIColors.TEXT_SECONDARY);
                setBackground(sel ? tbl.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : UIColors.TABLE_ALT_ROW));
                setBorder(new EmptyBorder(4, 12, 4, 12));
                return this;
            }
        };
    }

    // ── Form grid row helper ─────────────────────────────────────────────────
    public static void formRow(JPanel p, GridBagConstraints gbc, String label, JComponent comp, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(F_BOLD);
        lbl.setForeground(UIColors.TEXT_SECONDARY);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    // ── Message helpers ──────────────────────────────────────────────────────
    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Styled dialog panel ──────────────────────────────────────────────────
    /**
     * Returns a white content panel with an accent header bar at the top.
     * Use as the dialog's content pane. The returned outer panel uses BorderLayout;
     * add your form to the CENTER and a button row to the SOUTH.
     */
    public static JPanel dialogPanel(String title, Color accent) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Color.WHITE);

        // Accent header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, accent,
                        getWidth(), 0, accent.darker());
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        header.add(titleLbl, BorderLayout.CENTER);

        outer.add(header, BorderLayout.NORTH);
        return outer;
    }

    /**
     * Returns a form panel (GridBagLayout) with standard padding, white background.
     * Attach rows with formRow(...).
     */
    public static JPanel formPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 8, 24));
        return p;
    }

    /** Returns a right-aligned button bar (white background, standard padding). */
    public static JPanel buttonBar(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(8, 20, 16, 20));
        for (JButton b : buttons) p.add(b);
        return p;
    }

    // ── Avatar label ─────────────────────────────────────────────────────────
    public static JLabel avatar(String initials, Color bg, int size) {
        return avatar(initials, bg, size, size); // full circle
    }

    public static JLabel avatar(String initials, Color bg, int size, int arc) {
        JLabel l = new JLabel(initials, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Segoe UI", Font.BOLD, size / 3));
        l.setForeground(Color.WHITE);
        l.setOpaque(false);
        l.setPreferredSize(new Dimension(size, size));
        l.setMinimumSize(new Dimension(size, size));
        l.setMaximumSize(new Dimension(size, size));
        return l;
    }
}
