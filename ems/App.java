package ems;

import com.formdev.flatlaf.FlatDarkLaf;
import ems.config.AppConfig;
import ems.tools.SeedAdmin;
import ems.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        AppConfig.loadOnce();

        
        // EASY MODE: auto-create default admin if missing
        SeedAdmin.ensureAdminExists();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (Exception ignored) { }
            new LoginFrame().setVisible(true);
        });
    }
}