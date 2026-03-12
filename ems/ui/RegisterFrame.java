package ems.ui;

import com.toedter.calendar.JDateChooser;
import ems.model.Role;
import ems.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

public class RegisterFrame extends JFrame {

    private final JTextField emailField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final JPasswordField confirmPassField = new JPasswordField();

    private final JTextField fullNameField = new JTextField();
    private final JTextField gamerTagField = new JTextField();
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});

    private final JDateChooser dobChooser = new JDateChooser();
    private final JTextField addressField = new JTextField();

    // ✅ Only PLAYER and VIEWER can self-register
    private final JComboBox<Role> roleBox = new JComboBox<>(new Role[]{Role.PLAYER, Role.VIEWER});

    private final AuthService authService = new AuthService();

    public RegisterFrame() {
        setTitle("EMS - Register");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(520, 580);
        setLocationRelativeTo(null);

        dobChooser.setDateFormatString("yyyy-MM-dd");

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));

        form.add(new JLabel("Email *"));
        form.add(emailField);

        form.add(new JLabel("Password *"));
        form.add(passField);

        form.add(new JLabel("Confirm Password *"));
        form.add(confirmPassField);

        form.add(new JLabel("Full Name"));
        form.add(fullNameField);

        form.add(new JLabel("Gamer Tag / IGN"));
        form.add(gamerTagField);

        form.add(new JLabel("Gender"));
        form.add(genderBox);

        form.add(new JLabel("Date of Birth"));
        form.add(dobChooser);

        form.add(new JLabel("Address"));
        form.add(addressField);

        form.add(new JLabel("Role *"));
        form.add(roleBox);

        JButton backBtn = new JButton("Back");
        JButton createBtn = new JButton("Create Account");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(backBtn);
        actions.add(createBtn);

        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);

        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        createBtn.addActionListener(e -> onCreate());
    }

    private void onCreate() {
        try {
            String email = emailField.getText();
            String pass = new String(passField.getPassword());
            String confirm = new String(confirmPassField.getPassword());

            if (!pass.equals(confirm)) {
                Ui.error(this, "Passwords do not match.");
                return;
            }

            java.util.Date utilDob = dobChooser.getDate();
            Date dob = (utilDob == null) ? null : new Date(utilDob.getTime());

            Role selectedRole = (Role) roleBox.getSelectedItem();

            authService.register(
                    email,
                    pass,
                    selectedRole,
                    fullNameField.getText(),
                    gamerTagField.getText(),
                    (String) genderBox.getSelectedItem(),
                    dob,
                    addressField.getText()
            );

            if (selectedRole == Role.PLAYER) {
                Ui.info(this, "Registered successfully.\nYour PLAYER account is now PENDING approval by Manager.");
            } else {
                Ui.info(this, "Registered successfully.\nYour VIEWER account is ACTIVE. You can login now.");
            }

            dispose();
            new LoginFrame().setVisible(true);

        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}