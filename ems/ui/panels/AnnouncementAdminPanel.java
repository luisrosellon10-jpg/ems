package ems.ui.panels;

import ems.dao.AnnouncementDao;
import ems.model.Announcement;
import ems.model.AnnouncementTarget;
import ems.model.User;
import ems.ui.Ui;

import javax.swing.*;
import java.awt.*;

public class AnnouncementAdminPanel extends JPanel {
    private final User user;
    private final AnnouncementDao dao = new AnnouncementDao();

    private final JTextField titleField = new JTextField();
    private final JTextArea bodyField = new JTextArea(8, 60);
    private final JComboBox<AnnouncementTarget> targetBox = new JComboBox<>(AnnouncementTarget.values());

    public AnnouncementAdminPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout(10, 10));
        JLabel label = new JLabel("Post New Announcement");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Title"));
        form.add(titleField);
        form.add(new JLabel("Message body"));
        form.add(new JScrollPane(bodyField));
        form.add(new JLabel("Target audience"));
        form.add(targetBox);

        JButton postBtn = new JButton("Post Announcement");
        postBtn.addActionListener(e -> onPost());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(postBtn);

        add(label, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void onPost() {
        try {
            Announcement a = new Announcement();
            a.setTitle(titleField.getText());
            a.setBody(bodyField.getText());
            a.setTarget((AnnouncementTarget) targetBox.getSelectedItem());
            a.setAuthorId(user.getId());
            a.setAuthorName(user.getFullName() != null ? user.getFullName() : user.getEmail());

            if (a.getTitle() == null || a.getTitle().isBlank()) {
                Ui.error(this, "Title required");
                return;
            }
            if (a.getBody() == null || a.getBody().isBlank()) {
                Ui.error(this, "Body required");
                return;
            }

            dao.insert(a);

            Ui.info(this, "Posted announcement!");
            titleField.setText("");
            bodyField.setText("");
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}