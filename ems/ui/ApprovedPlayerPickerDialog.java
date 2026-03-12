package ems.ui;

import ems.dao.PlayerDao;
import ems.dao.UserDao;
import ems.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ApprovedPlayerPickerDialog extends JDialog {

    private final UserDao userDao = new UserDao();
    private final PlayerDao playerDao = new PlayerDao();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Email", "Full Name", "Gamer Tag"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    private boolean selected;
    private User selectedUser;

    public ApprovedPlayerPickerDialog(Window owner) {
        super(owner, "Select Approved Player", ModalityType.APPLICATION_MODAL);
        setSize(760, 450);
        setLocationRelativeTo(owner);

        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add To Team");
        JButton cancelBtn = new JButton("Cancel");

        refreshBtn.addActionListener(e -> load());
        addBtn.addActionListener(e -> onSelect());
        cancelBtn.addActionListener(e -> dispose());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancelBtn);
        bottom.add(addBtn);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        load();
    }

    private void load() {
        try {
            model.setRowCount(0);

            List<User> list = userDao.listApprovedPlayers();
            for (User u : list) {
                if (u.getEmail() == null || u.getEmail().isBlank()) continue;

                // ✅ ONLY show players not yet assigned to a team
                if (!playerDao.isAvailableByEmail(u.getEmail())) continue;

                model.addRow(new Object[]{u.getId(), u.getEmail(), u.getFullName(), u.getGamerTag()});
            }
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onSelect() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a player first"); return; }

        User u = new User();
        u.setId(((Number) model.getValueAt(row, 0)).longValue());
        u.setEmail(String.valueOf(model.getValueAt(row, 1)));
        u.setFullName(String.valueOf(model.getValueAt(row, 2)));
        u.setGamerTag(String.valueOf(model.getValueAt(row, 3)));

        selectedUser = u;
        selected = true;
        dispose();
    }

    public boolean isSelected() { return selected; }
    public User getSelectedUser() { return selectedUser; }
}