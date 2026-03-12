package ems.ui;

import ems.model.User;
import ems.ui.panels.TournamentListPanel;
import ems.ui.panels.TournamentOverviewPanel;
import ems.ui.panels.TournamentSelectionListener;
import ems.ui.panels.UserApprovalsPanel;

import javax.swing.*;
import java.awt.*;

public class ManagerWorkspaceFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    private final TournamentOverviewPanel overviewPanel = new TournamentOverviewPanel();

    public ManagerWorkspaceFrame(User user) {
        setTitle("EMS - Manager Workspace (" + user.getEmail() + ")");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        JPanel sidebar = buildSidebar(user);

        TournamentSelectionListener listener = (id, name) -> {
            overviewPanel.setTournament(id, name);
            cardLayout.show(content, "overview");
        };

        content.add(new ems.ui.panels.EditableProfilePanel(user), "profile");
        content.add(overviewPanel, "overview");
        content.add(new TournamentListPanel(listener), "tournaments");

        // ✅ NEW: approvals (pending PLAYER only, because we filtered in UserDao.listPendingUsers)
        content.add(new UserApprovalsPanel(), "approvals");

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

        JLabel brand = new JLabel("EMS - Manager");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 16f));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLine = new JLabel("<html><b>" + user.getEmail() + "</b><br/>Role: " + user.getRole() + "</html>");
        userLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton profileBtn = new JButton("Profile");
        JButton overviewBtn = new JButton("Overview");
        JButton tournamentsBtn = new JButton("Tournaments");
        JButton approvalsBtn = new JButton("Approvals"); // ✅ NEW
        JButton logoutBtn = new JButton("Logout");

        for (JButton b : new JButton[]{profileBtn, overviewBtn, tournamentsBtn, approvalsBtn, logoutBtn}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }

        profileBtn.addActionListener(e -> cardLayout.show(content, "profile"));
        overviewBtn.addActionListener(e -> cardLayout.show(content, "overview"));
        tournamentsBtn.addActionListener(e -> cardLayout.show(content, "tournaments"));
        approvalsBtn.addActionListener(e -> cardLayout.show(content, "approvals"));

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
        sidebar.add(overviewBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(tournamentsBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(approvalsBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(logoutBtn);

        return sidebar;
    }
}