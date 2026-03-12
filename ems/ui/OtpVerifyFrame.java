package ems.ui;

import ems.service.OtpService;

import javax.swing.*;
import java.awt.*;

public class OtpVerifyFrame extends JFrame {

    private final String email;
    private final JTextField otpField = new JTextField();
    private final OtpService otpService = new OtpService();

    public OtpVerifyFrame(String email) {
        this.email = email;

        setTitle("EMS - Verify OTP");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 220);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.add(new JLabel("Enter OTP sent to: " + email));
        form.add(otpField);

        JButton backBtn = new JButton("Back");
        JButton verifyBtn = new JButton("Verify");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(backBtn);
        actions.add(verifyBtn);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);

        backBtn.addActionListener(e -> {
            dispose();
            new ForgotPasswordFrame().setVisible(true);
        });

        verifyBtn.addActionListener(e -> onVerify());

        // Press Enter to verify
        getRootPane().setDefaultButton(verifyBtn);
    }

    private void onVerify() {
        try {
            String otp = otpField.getText() == null ? "" : otpField.getText().trim();

            long userId = otpService.verifyOtp(email, otp);

            Ui.info(this, "OTP verified. You can reset your password.");
            dispose();
            new ResetPasswordFrame(userId).setVisible(true);

        } catch (Exception ex) {
            otpField.setText("");
            Ui.error(this, ex.getMessage());
        }
    }
}