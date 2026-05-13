package jdbc.demo;

import com.formdev.flatlaf.FlatIntelliJLaf;
import jdbc.demo.ui.LoginFrame;
import javax.swing.*;
import java.awt.*;

/**
 * If you see this WARNING on Java 21+ when running from IntelliJ:
 *   "java.lang.System::load has been called by com.formdev.flatlaf … Use --enable-native-access=ALL-UNNAMED"
 * Fix: IntelliJ → Run/Debug Configurations → VM options → add:
 *   --enable-native-access=ALL-UNNAMED
 * (When running as a packaged JAR via mvn package, this is already set in MANIFEST.MF.)
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            new LoginFrame().setVisible(true);
        });
    }

    private static void setupLookAndFeel() {
        try {
            FlatIntelliJLaf.setup();
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        // Global FlatLaf token overrides
        UIManager.put("Button.arc",           10);
        UIManager.put("Component.arc",        8);
        UIManager.put("TextComponent.arc",    6);
        UIManager.put("CheckBox.arc",         4);
        UIManager.put("ScrollBar.thumbArc",   999);
        UIManager.put("ScrollBar.width",      8);
        UIManager.put("Table.rowHeight",      38);
        UIManager.put("TableHeader.height",   36);
        UIManager.put("TabbedPane.tabHeight", 36);
        UIManager.put("defaultFont",          new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("PasswordField.showRevealButton", true);
    }
}
