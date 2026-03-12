package ems.ui;

import ems.model.User;
import ems.ui.panels.TournamentListPanel;
import ems.ui.panels.TournamentOverviewPanel;
import ems.ui.panels.TournamentSelectionListener;

import javax.swing.*;
import java.awt.*;

public class ViewerWorkspaceFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    private final TournamentOverviewPanel overviewPanel = new TournamentOverviewPanel(true);

    public ViewerWorkspaceFrame(User user) {
        setTitle("EMS - Viewer Workspace (" + user.getEmail() + ")");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        JPanel sidebar = buildSidebar(user);

        TournamentSelectionListener listener = (id, name) -> {
            overviewPanel.setTournament(id, name);
            cardLayout.show(content, "overview");
        };

        // pages
        content.add(overviewPanel, "overview");
        content.add(new TournamentListPanel(listener, true), "tournaments"); // ✅ only this one

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        cardLayout.show(content, "tournaments");
    }

    private JPanel buildSidebar(User user) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        sidebar.setPreferredSize(new Dimension(220, 10));

        JLabel brand = new JLabel("EMS - Viewer");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 16f));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLine = new JLabel("<html><b>" + user.getEmail() + "</b><br/>Role: " + user.getRole() + "</html>");
        userLine.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton overviewBtn = new JButton("Overview");
        JButton tournamentsBtn = new JButton("Tournaments");
        JButton logoutBtn = new JButton("Logout");

        for (JButton b : new JButton[]{overviewBtn, tournamentsBtn, logoutBtn}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }

        overviewBtn.addActionListener(e -> cardLayout.show(content, "overview"));
        tournamentsBtn.addActionListener(e -> cardLayout.show(content, "tournaments"));

        logoutBtn.addActionListener(e -> {
            if (!Ui.confirm(this, "Logout?")) return;
            dispose();
            new LoginFrame().setVisible(true);
        });

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(userLine);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(overviewBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(tournamentsBtn);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(new JSeparator());
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(logoutBtn);

        return sidebar;
    }
}