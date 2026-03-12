package ems.ui;

import ems.model.User;
import ems.ui.panels.PlayerDashboardPanel;
import ems.ui.panels.PlayerProfilePanel;

import javax.swing.*;
import java.awt.*;

public class PlayerWorkspaceFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    public PlayerWorkspaceFrame(User user) {
        setTitle("EMS - Player Workspace (" + user.getEmail() + ")");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        JPanel sidebar = buildSidebar(user);

        content.add(new ems.ui.panels.EditableProfilePanel(user), "profile");
        content.add(new PlayerDashboardPanel(user), "dashboard");

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        cardLayout.show(content, "dashboard");
    }

    private JPanel buildSidebar(User user) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        sidebar.setPreferredSize(new Dimension(220, 10));

        JLabel brand = new JLabel("EMS - Player");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 16f));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLine = new JLabel("<html><b>" + user.getEmail() + "</b><br/>Role: " + user.getRole() + "</html>");
        userLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton dashboardBtn = new JButton("Dashboard");
        JButton profileBtn = new JButton("Profile");
        JButton logoutBtn = new JButton("Logout");

        for (JButton b : new JButton[]{dashboardBtn, profileBtn, logoutBtn}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }

        dashboardBtn.addActionListener(e -> cardLayout.show(content, "dashboard"));
        profileBtn.addActionListener(e -> cardLayout.show(content, "profile"));

        logoutBtn.addActionListener(e -> {
            if (!Ui.confirm(this, "Logout?")) return;
            dispose();
            new LoginFrame().setVisible(true);
        });

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(userLine);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(profileBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(logoutBtn);

        return sidebar;
    }
}