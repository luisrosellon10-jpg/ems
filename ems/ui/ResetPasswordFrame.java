package ems.ui;

import ems.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class ResetPasswordFrame extends JFrame {

    private final long userId;
    private final JPasswordField pass1 = new JPasswordField();
    private final JPasswordField pass2 = new JPasswordField();
    private final AuthService authService = new AuthService();

    public ResetPasswordFrame(long userId) {
        this.userId = userId;

        setTitle("EMS - Reset Password");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.add(new JLabel("New Password"));
        form.add(pass1);
        form.add(new JLabel("Confirm Password"));
        form.add(pass2);

        JButton saveBtn = new JButton("Save");
        JButton toLoginBtn = new JButton("Back to Login");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(toLoginBtn);
        actions.add(saveBtn);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);

        toLoginBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        saveBtn.addActionListener(e -> onSave());
        getRootPane().setDefaultButton(saveBtn); // press Enter to save
    }

    private void onSave() {
        try {
            String p1 = new String(pass1.getPassword());
            String p2 = new String(pass2.getPassword());

            if (p1.isBlank() || p2.isBlank()) {
                throw new IllegalStateException("Password is required");
            }
            if (p1.length() < 8) {
                throw new IllegalStateException("Password must be at least 8 characters");
            }
            if (!p1.equals(p2)) {
                throw new IllegalStateException("Passwords do not match");
            }

            authService.resetPassword(userId, p1);

            Ui.info(this, "Password updated. You can login now.");
            dispose();
            new LoginFrame().setVisible(true);

        } catch (Exception ex) {
            // Clear fields after error (avoids confusion)
            pass1.setText("");
            pass2.setText("");
            Ui.error(this, ex.getMessage());
        }
    }
}