package ems.ui;

import ems.dao.UserDao;
import ems.model.AccountStatus;
import ems.model.Role;
import ems.model.User;
import ems.security.PasswordHasher;

import javax.swing.*;
import java.awt.*;

public class CreateManagerDialog extends JDialog {

    private final JTextField emailField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JTextField fullNameField = new JTextField();

    private boolean created = false;

    private final UserDao userDao = new UserDao();

    public CreateManagerDialog(Frame owner) {
        super(owner, "Create Manager Account", true);

        setSize(460, 260);
        setLocationRelativeTo(owner);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Manager Email *"));
        form.add(emailField);
        form.add(new JLabel("Temporary Password *"));
        form.add(passField);
        form.add(new JLabel("Full Name"));
        form.add(fullNameField);

        JButton cancelBtn = new JButton("Cancel");
        JButton createBtn = new JButton("Create");

        cancelBtn.addActionListener(e -> dispose());
        createBtn.addActionListener(e -> onCreate());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(cancelBtn);
        actions.add(createBtn);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
    }

    public boolean isCreated() {
        return created;
    }

    private void onCreate() {
        try {
            String email = emailField.getText();
            String pass = new String(passField.getPassword());
            String fullName = fullNameField.getText();

            if (email == null || email.isBlank()) {
                Ui.error(this, "Email is required");
                return;
            }
            if (pass == null || pass.isBlank()) {
                Ui.error(this, "Temporary password is required");
                return;
            }

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

            created = true;
            Ui.info(this, "Manager created: " + email);
            dispose();

        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}