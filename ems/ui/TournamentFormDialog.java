package ems.ui;

import ems.dao.TournamentDao;
import ems.model.Tournament;
import ems.model.TournamentStatus;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.awt.Window;

public class TournamentFormDialog extends JDialog {
    
    public TournamentFormDialog(Window owner, ems.model.Tournament editing) {
    super(owner, ModalityType.APPLICATION_MODAL);
    this.editing = editing;

    setTitle(editing == null ? "Add Tournament" : "Edit Tournament");
    setSize(520, 320);
    setLocationRelativeTo(owner);

    // keep the rest of your UI setup exactly the same as your existing constructor
    // (copy/paste the body of the Frame constructor into here if needed)
}

    private final TournamentDao dao = new TournamentDao();

    private final JTextField nameField = new JTextField();
    private final JTextField startDateField = new JTextField(); // yyyy-mm-dd
    private final JTextField endDateField = new JTextField();   // yyyy-mm-dd
    private final JComboBox<TournamentStatus> statusBox = new JComboBox<>(TournamentStatus.values());

    private boolean saved = false;
    private final Tournament editing; // null when adding

    public TournamentFormDialog(Frame owner, Tournament editing) {
        super(owner, true);
        this.editing = editing;

        setTitle(editing == null ? "Add Tournament" : "Edit Tournament");
        setSize(450, 300);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Name"));
        form.add(nameField);

        form.add(new JLabel("Start Date (yyyy-mm-dd, optional)"));
        form.add(startDateField);

        form.add(new JLabel("End Date (yyyy-mm-dd, optional)"));
        form.add(endDateField);

        form.add(new JLabel("Status"));
        form.add(statusBox);

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

        if (editing != null) fillForm(editing);
    }

    public boolean isSaved() {
        return saved;
    }

    private void fillForm(Tournament t) {
        nameField.setText(t.getName());
        startDateField.setText(t.getStartDate() == null ? "" : t.getStartDate().toString());
        endDateField.setText(t.getEndDate() == null ? "" : t.getEndDate().toString());
        statusBox.setSelectedItem(t.getStatus());
    }

    private void onSave() {
        try {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            if (name.isBlank()) throw new IllegalStateException("Tournament name is required");

            Date start = parseDateOrNull(startDateField.getText());
            Date end = parseDateOrNull(endDateField.getText());
            if (start != null && end != null && end.before(start)) {
                throw new IllegalStateException("End date must be after start date");
            }

            TournamentStatus status = (TournamentStatus) statusBox.getSelectedItem();
            if (status == null) status = TournamentStatus.DRAFT;

            if (editing == null) {
                Tournament t = new Tournament();
                t.setName(name);
                t.setStartDate(start);
                t.setEndDate(end);
                t.setStatus(status);
                dao.insert(t);
            } else {
                editing.setName(name);
                editing.setStartDate(start);
                editing.setEndDate(end);
                editing.setStatus(status);
                dao.update(editing);
            }

            saved = true;
            dispose();

        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static Date parseDateOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        // expects yyyy-mm-dd
        return Date.valueOf(t);
    }
}