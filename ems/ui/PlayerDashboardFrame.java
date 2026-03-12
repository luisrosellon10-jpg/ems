package ems.ui;

import ems.model.User;
import ems.ui.panels.PlayerDashboardPanel;

import javax.swing.*;
import java.awt.*;

public class PlayerDashboardFrame extends JFrame {

    public PlayerDashboardFrame(User user) {
        setTitle("EMS - Player Dashboard (" + user.getEmail() + ")");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(new PlayerDashboardPanel(user), BorderLayout.CENTER);
    }
}