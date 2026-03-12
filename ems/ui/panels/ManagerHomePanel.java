package ems.ui.panels;

import ems.model.User;

import javax.swing.*;
import java.awt.*;

public class ManagerHomePanel extends JPanel {

    public ManagerHomePanel(User user) {
        super(new BorderLayout(10, 10));

        JLabel title = new JLabel("Manager Home");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JTextArea info = new JTextArea(
                "Logged in as: " + user.getEmail() + "\n\n" +
                "Go to the Tournaments tab to manage tournaments.\n" +
                "From there you can manage Teams, Players, Matches, Standings, and Brackets."
        );
        info.setEditable(false);
        info.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.NORTH);
        top.add(info, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
    }
}