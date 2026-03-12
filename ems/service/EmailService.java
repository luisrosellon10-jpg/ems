package ems.service;

import ems.config.AppConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {

    public void sendOtpEmail(String toEmail, String otp, int expiryMin) {
        // (keep your existing method exactly as-is)
        // ...
    }

    public void sendAccountApprovedEmail(String toEmail) {
        sendSimpleEmail(
                toEmail,
                "Your EMS account has been approved",
                "Good news!\n\nYour EMS account has been approved by the admin. You can now login.\n\n- EMS"
        );
    }

    public void sendAccountRejectedEmail(String toEmail) {
        sendSimpleEmail(
                toEmail,
                "Your EMS account was rejected",
                "Hello,\n\nYour EMS account registration was rejected by the admin.\n\n- EMS"
        );
    }

    private void sendSimpleEmail(String toEmail, String subject, String body) {
        String enabledStr = AppConfig.get("smtp.enabled");
        boolean enabled = enabledStr != null && enabledStr.equalsIgnoreCase("true");

        if (!enabled) {
            System.out.println("[EMS EMAIL DEBUG] To=" + toEmail + " Subject=" + subject + "\n" + body);
            return;
        }

        String host = AppConfig.get("smtp.host");
        String portStr = AppConfig.get("smtp.port");
        String username = AppConfig.get("smtp.username");
        String password = AppConfig.getOrEnv("smtp.password", "EMS_SMTP_PASSWORD");

        String fromName = AppConfig.get("smtp.from_name");
        String fromEmail = AppConfig.get("smtp.from_email");

        if (host == null || host.isBlank()) throw new IllegalStateException("Missing config: smtp.host");
        if (portStr == null || portStr.isBlank()) throw new IllegalStateException("Missing config: smtp.port");
        if (username == null || username.isBlank()) throw new IllegalStateException("Missing config: smtp.username");
        if (password == null || password.isBlank()) throw new IllegalStateException("Missing config: smtp.password");

        int port;
        try {
            port = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid config smtp.port: " + portStr);
        }

        if (fromName == null || fromName.isBlank()) fromName = "EMS";
        if (fromEmail == null || fromEmail.isBlank()) fromEmail = username;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject(subject, StandardCharsets.UTF_8.name());
            msg.setText(body, StandardCharsets.UTF_8.name());

            Transport.send(msg);
            System.out.println("[EMS] Email sent to " + toEmail + " (" + subject + ")");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}