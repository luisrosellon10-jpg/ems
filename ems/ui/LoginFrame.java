package ems.ui;

import ems.model.User;
import ems.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField();
    private final JPasswordField passField = new JPasswordField();
    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("EMS - Login");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Password"));
        form.add(passField);

        JButton loginBtn = new JButton("Login");
        JButton regBtn = new JButton("Register");
        JButton forgotBtn = new JButton("Forgot Password");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(forgotBtn);
        actions.add(regBtn);
        actions.add(loginBtn);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);

        loginBtn.addActionListener(e -> onLogin());
        regBtn.addActionListener(e -> {
            dispose();
            new RegisterFrame().setVisible(true);
        });
        forgotBtn.addActionListener(e -> {
            dispose();
            new ForgotPasswordFrame().setVisible(true);
        });
    }

    private void onLogin() {
        try {
            String email = emailField.getText();
            String pass = new String(passField.getPassword());
            User u = authService.login(email, pass);

            // ✅ open correct workspace (sidebar-enabled for Player)
            dispose();
            RoleRouter.openWorkspace(u);

        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}