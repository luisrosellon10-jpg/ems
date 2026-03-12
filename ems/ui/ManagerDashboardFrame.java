package ems.ui;

import ems.model.User;

import javax.swing.*;
import java.awt.*;

public class ManagerDashboardFrame extends JFrame {
    public ManagerDashboardFrame(User user) {
        setTitle("EMS - Manager Dashboard (" + user.getEmail() + ")");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JButton tournamentsBtn = new JButton("Open Manager Workspace");
        tournamentsBtn.addActionListener(e -> {
            RoleRouter.openWorkspace(user);
            
            dispose(); // close this window so you don’t have multi windows
        });

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(tournamentsBtn);
        setContentPane(p);
    }
}