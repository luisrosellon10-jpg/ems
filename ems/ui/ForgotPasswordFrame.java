package ems.ui;

import ems.service.OtpService;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordFrame extends JFrame {

    private final JTextField emailField = new JTextField();
    private final OtpService otpService = new OtpService();

    public ForgotPasswordFrame() {
        setTitle("EMS - Forgot Password");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(420, 220);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JPanel form = new JPanel(new GridLayout(0,1,8,8));
        form.add(new JLabel("Enter your registered email"));
        form.add(emailField);

        JButton backBtn = new JButton("Back");
        JButton sendBtn = new JButton("Send OTP");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(backBtn);
        actions.add(sendBtn);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);

        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        sendBtn.addActionListener(e -> onSendOtp());
    }

    private void onSendOtp() {
        try {
            otpService.sendOtpToEmail(emailField.getText());
            Ui.info(this, "OTP sent. Check your email.");
            dispose();
            new OtpVerifyFrame(emailField.getText()).setVisible(true);
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}