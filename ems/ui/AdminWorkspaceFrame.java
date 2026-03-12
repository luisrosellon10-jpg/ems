package ems.ui;

import ems.model.User;
import ems.ui.panels.AnnouncementAdminPanel;
import ems.ui.panels.AnnouncementListPanel;
import ems.ui.panels.CreateManagerPanel;

import javax.swing.*;
import java.awt.*;

public class AdminWorkspaceFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    public AdminWorkspaceFrame(User user) {
        setTitle("EMS - Admin Workspace (" + user.getEmail() + ")");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        JPanel sidebar = buildSidebar(user);

        // Pages
        content.add(new ems.ui.panels.EditableProfilePanel(user), "profile");
        content.add(new CreateManagerPanel(), "create_manager");
        content.add(new AnnouncementListPanel(user), "announcements");
        content.add(new AnnouncementAdminPanel(user), "announcements_admin");

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        cardLayout.show(content, "profile");
    }

    private JPanel buildSidebar(User user) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        sidebar.setPreferredSize(new Dimension(220, 10));

        JLabel brand = new JLabel("EMS - Admin");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 16f));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLine = new JLabel("<html><b>" + user.getEmail() + "</b><br/>Role: " + user.getRole() + "</html>");
        userLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton profileBtn = new JButton("Profile");
        JButton createManagerBtn = new JButton("Create Manager");
        JButton announcementsBtn = new JButton("Announcements");
        JButton postAnnouncementBtn = new JButton("Post Announcement");
        JButton logoutBtn = new JButton("Logout");

        for (JButton b : new JButton[]{profileBtn, createManagerBtn, announcementsBtn, postAnnouncementBtn, logoutBtn}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }

        profileBtn.addActionListener(e -> cardLayout.show(content, "profile"));
        createManagerBtn.addActionListener(e -> cardLayout.show(content, "create_manager"));
        announcementsBtn.addActionListener(e -> cardLayout.show(content, "announcements"));
        postAnnouncementBtn.addActionListener(e -> cardLayout.show(content, "announcements_admin"));
        logoutBtn.addActionListener(e -> {
            if (!Ui.confirm(this, "Logout?")) return;
            dispose();
            new LoginFrame().setVisible(true);
        });

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(userLine);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(profileBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(createManagerBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(announcementsBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(postAnnouncementBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(logoutBtn);

        return sidebar;
    }
}