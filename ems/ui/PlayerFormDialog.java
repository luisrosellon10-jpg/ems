package ems.ui;

import ems.dao.PlayerDao;
import ems.model.Player;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

public class PlayerFormDialog extends JDialog {

    private final PlayerDao dao = new PlayerDao();
    private final long teamId;
    private final Player editing;

    private final JTextField gamerTagField = new JTextField();
    private final JTextField fullNameField = new JTextField();
    private final JTextField genderField = new JTextField();
    private final JTextField dobField = new JTextField(); // yyyy-mm-dd optional
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();

    private boolean saved = false;

    public PlayerFormDialog(Frame owner, long teamId, Player editing) {
        super(owner, true);
        this.teamId = teamId;
        this.editing = editing;

        setTitle(editing == null ? "Add Player" : "Edit Player");
        setSize(520, 420);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Gamer Tag *"));
        form.add(gamerTagField);

        form.add(new JLabel("Full Name"));
        form.add(fullNameField);

        form.add(new JLabel("Gender"));
        form.add(genderField);

        form.add(new JLabel("Date of Birth (yyyy-mm-dd)"));
        form.add(dobField);

        form.add(new JLabel("Contact Email"));
        form.add(emailField);

        form.add(new JLabel("Phone"));
        form.add(phoneField);

        JButton cancelBtn = new JButton("Cancel");
        JButton saveBtn = new JButton("Save");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(cancelBtn);
        actions.add(saveBtn);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);

        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> onSave());
        getRootPane().setDefaultButton(saveBtn);

        if (editing != null) fill(editing);
    }

    public boolean isSaved() { return saved; }

    private void fill(Player p) {
        gamerTagField.setText(nv(p.getGamerTag()));
        fullNameField.setText(nv(p.getFullName()));
        genderField.setText(nv(p.getGender()));
        dobField.setText(p.getDateOfBirth() == null ? "" : p.getDateOfBirth().toString());
        emailField.setText(nv(p.getContactEmail()));
        phoneField.setText(nv(p.getPhone()));
    }

    private void onSave() {
        try {
            String tag = text(gamerTagField);
            if (tag.isBlank()) throw new IllegalStateException("Gamer Tag is required");

            Date dob = parseDateOrNull(text(dobField));

            if (editing == null) {
                Player p = new Player();
                p.setTeamId(teamId);
                p.setGamerTag(tag);
                p.setFullName(emptyToNull(text(fullNameField)));
                p.setGender(emptyToNull(text(genderField)));
                p.setDateOfBirth(dob);
                p.setContactEmail(emptyToNull(text(emailField)));
                p.setPhone(emptyToNull(text(phoneField)));
                dao.insert(p);
            } else {
                editing.setGamerTag(tag);
                editing.setFullName(emptyToNull(text(fullNameField)));
                editing.setGender(emptyToNull(text(genderField)));
                editing.setDateOfBirth(dob);
                editing.setContactEmail(emptyToNull(text(emailField)));
                editing.setPhone(emptyToNull(text(phoneField)));
                dao.update(editing);
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static String text(JTextField f) { return f.getText() == null ? "" : f.getText().trim(); }
    private static String nv(String s) { return s == null ? "" : s; }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Date parseDateOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        return Date.valueOf(s.trim());
    }
}