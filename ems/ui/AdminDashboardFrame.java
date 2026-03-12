package ems.ui;

import ems.model.Role;
import ems.model.User;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {

    public AdminDashboardFrame(User user) {
        if (user == null) throw new IllegalArgumentException("User is required");
        if (user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Access denied: Admins only");
        }

        setTitle("EMS - Admin Dashboard (" + user.getEmail() + ")");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JButton createManagerBtn = new JButton("Create Manager Account");
        createManagerBtn.addActionListener(e -> new CreateManagerDialog(this).setVisible(true));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(createManagerBtn);

        setContentPane(p);
    }
}