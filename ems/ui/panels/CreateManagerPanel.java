package ems.ui.panels;

import ems.dao.UserDao;
import ems.model.AccountStatus;
import ems.model.Role;
import ems.model.User;
import ems.security.PasswordHasher;
import ems.ui.Ui;

import javax.swing.*;
import java.awt.*;

public class CreateManagerPanel extends JPanel {

    private final UserDao userDao = new UserDao();

    private final JTextField emailField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JTextField fullNameField = new JTextField();

    public CreateManagerPanel() {
        super(new BorderLayout(10, 10));

        JLabel title = new JLabel("Create Manager Account");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Manager Email *"));
        form.add(emailField);
        form.add(new JLabel("Temporary Password *"));
        form.add(passField);
        form.add(new JLabel("Full Name"));
        form.add(fullNameField);

        JButton createBtn = new JButton("Create Manager");
        createBtn.addActionListener(e -> onCreate());

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(createBtn);

        add(top, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void onCreate() {
        try {
            String email = emailField.getText();
            String pass = new String(passField.getPassword());
            String fullName = fullNameField.getText();

            if (email == null || email.isBlank()) { Ui.error(this, "Email is required"); return; }
            if (pass == null || pass.isBlank()) { Ui.error(this, "Temporary password is required"); return; }

            email = email.trim().toLowerCase();

            if (userDao.findByEmail(email).isPresent()) {
                Ui.error(this, "Email already registered");
                return;
            }

            User u = new User();
            u.setEmail(email);
            u.setFullName(fullName == null || fullName.isBlank() ? null : fullName.trim());
            u.setPasswordHash(PasswordHasher.hash(pass));
            u.setRole(Role.MANAGER);
            u.setStatus(AccountStatus.APPROVED);

            userDao.insertUser(u);

            Ui.info(this, "Manager created: " + email);

            emailField.setText("");
            passField.setText("");
            fullNameField.setText("");

        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}