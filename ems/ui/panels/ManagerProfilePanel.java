package ems.ui.panels;

import ems.model.User;

import javax.swing.*;
import java.awt.*;

public class ManagerProfilePanel extends JPanel {

    public ManagerProfilePanel(User user) {
        super(new BorderLayout(10, 10));

        JLabel title = new JLabel("Manager Profile");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setOpaque(false);
        info.setText(
                "Email: " + nv(user.getEmail()) + "\n" +
                "Full Name: " + nv(user.getFullName()) + "\n" +
                "Role: " + user.getRole() + "\n" +
                "Status: " + user.getStatus()
        );

        add(title, BorderLayout.NORTH);
        add(info, BorderLayout.CENTER);
    }

    private static String nv(String s) { return s == null ? "" : s; }
}