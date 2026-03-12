package ems.ui.panels;

import ems.dao.UserDao;
import ems.model.User;
import ems.ui.Ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;

public class EditableProfilePanel extends JPanel {

    private final UserDao userDao = new UserDao();
    private final User user;

    private final JTextField emailField = new JTextField();
    private final JTextField roleField = new JTextField();

    private final JTextField fullNameField = new JTextField();
    private final JTextField gamerTagField = new JTextField();
    private final JComboBox<String> genderBox = new JComboBox<>(new String[]{"", "MALE", "FEMALE", "OTHER"});
    private final JTextField dobField = new JTextField(); // yyyy-mm-dd
    private final JTextArea addressArea = new JTextArea(4, 20);

    private final JButton editBtn = new JButton("Edit");
    private final JButton saveBtn = new JButton("Save");
    private final JButton cancelBtn = new JButton("Cancel");

    public EditableProfilePanel(User user) {
        super(new BorderLayout(10, 10));
        this.user = user;

        JLabel title = new JLabel("Profile");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        emailField.setEditable(false);
        roleField.setEditable(false);

        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        addRow(form, gc, "Email", emailField);
        addRow(form, gc, "Role", roleField);
        addRow(form, gc, "Full Name", fullNameField);
        addRow(form, gc, "Gamer Tag", gamerTagField);
        addRow(form, gc, "Gender", genderBox);
        addRow(form, gc, "Date of Birth (yyyy-mm-dd)", dobField);

        // Address row (textarea)
        gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1;
        form.add(new JLabel("Address"), gc);
        gc.gridx = 1; gc.weightx = 1; gc.gridwidth = 2;
        form.add(new JScrollPane(addressArea), gc);
        gc.gridy++;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(editBtn);
        actions.add(cancelBtn);
        actions.add(saveBtn);

        editBtn.addActionListener(e -> setEditing(true));
        cancelBtn.addActionListener(e -> {
            fillFromUser();
            setEditing(false);
        });
        saveBtn.addActionListener(e -> onSave());

        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        fillFromUser();
        setEditing(false);
    }

    private void fillFromUser() {
        emailField.setText(nv(user.getEmail()));
        roleField.setText(user.getRole() == null ? "" : user.getRole().name());

        fullNameField.setText(nv(user.getFullName()));
        gamerTagField.setText(nv(user.getGamerTag()));
        genderBox.setSelectedItem(nv(user.getGender()).toUpperCase());
        dobField.setText(user.getDateOfBirth() == null ? "" : user.getDateOfBirth().toString());
        addressArea.setText(nv(user.getAddress()));
    }

    private void setEditing(boolean editing) {
        fullNameField.setEditable(editing);
        gamerTagField.setEditable(editing);
        genderBox.setEnabled(editing);
        dobField.setEditable(editing);
        addressArea.setEditable(editing);

        editBtn.setEnabled(!editing);
        saveBtn.setEnabled(editing);
        cancelBtn.setEnabled(editing);
    }

    private void onSave() {
        try {
            String fullName = fullNameField.getText().trim();
            String gamerTag = gamerTagField.getText().trim();
            String gender = String.valueOf(genderBox.getSelectedItem()).trim();
            String dobText = dobField.getText().trim();
            String address = addressArea.getText().trim();

            Date dob = null;
            if (!dobText.isBlank()) {
                // must be yyyy-mm-dd
                dob = Date.valueOf(dobText);
            }

            // Basic validation (keep it simple for demo)
            if (fullName.isBlank()) {
                Ui.error(this, "Full Name is required.");
                return;
            }

            userDao.updateProfile(user.getId(), fullName, gamerTag, gender, dob, address);

            // Update in-memory user object so UI stays consistent
            user.setFullName(fullName);
            user.setGamerTag(gamerTag);
            user.setGender(gender);
            user.setDateOfBirth(dob);
            user.setAddress(address);

            Ui.info(this, "Profile updated.");
            setEditing(false);

        } catch (IllegalArgumentException ex) {
            Ui.error(this, "Invalid DOB. Use yyyy-mm-dd.");
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static void addRow(JPanel form, GridBagConstraints gc, String label, JComponent field) {
        gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1;
        form.add(new JLabel(label), gc);

        gc.gridx = 1; gc.weightx = 1; gc.gridwidth = 2;
        form.add(field, gc);

        gc.gridy++;
    }

    private static String nv(String s) { return s == null ? "" : s; }
}