package ems.ui;

import ems.dao.PlayerDao;
import ems.dao.TeamDao;
import ems.model.Player;
import ems.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class PlayerListFrame extends JFrame {

    private final boolean readOnly;

    private final long tournamentId;
    private final long teamId;
    private final String teamName;

    private final PlayerDao dao = new PlayerDao();
    private final TeamDao teamDao = new TeamDao();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Gamer Tag", "Full Name", "Gender", "DOB", "Email", "Phone", "Created"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);
    public PlayerListFrame(long tournamentId, long teamId, String teamName) {
    this(tournamentId, teamId, teamName, false);
}

    public PlayerListFrame(long tournamentId, long teamId, String teamName, boolean readOnly) {
        this.tournamentId = tournamentId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.readOnly = readOnly;

        setTitle("EMS - Players (" + teamName + ")" + (readOnly ? " [READ-ONLY]" : ""));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(980, 450);
        setLocationRelativeTo(null);

        JButton refreshBtn = new JButton("Refresh");
        JButton addApprovedBtn = new JButton("Add Approved");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton transferBtn = new JButton("Transfer");

        // ✅ Action listeners (you were missing these)
        refreshBtn.addActionListener(e -> load());
        addApprovedBtn.addActionListener(e -> onAddApproved());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        transferBtn.addActionListener(e -> onTransfer());

        // ✅ Read-only: hide write actions
        addApprovedBtn.setVisible(!readOnly);
        editBtn.setVisible(!readOnly);
        deleteBtn.setVisible(!readOnly);
        transferBtn.setVisible(!readOnly);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);
        top.add(addApprovedBtn);
        top.add(editBtn);
        top.add(deleteBtn);
        top.add(transferBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            List<Player> list = dao.listByTeam(teamId);
            for (Player p : list) {
                model.addRow(new Object[]{
                        p.getId(),
                        nv(p.getGamerTag()),
                        nv(p.getFullName()),
                        nv(p.getGender()),
                        formatDob(p.getDateOfBirth()),
                        nv(p.getContactEmail()),
                        nv(p.getPhone()),
                        p.getCreatedAt()
                });
            }
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onAddApproved() {
        if (readOnly) { Ui.error(this, "Read-only mode."); return; }

        try {
            ApprovedPlayerPickerDialog d =
                    new ApprovedPlayerPickerDialog(SwingUtilities.getWindowAncestor(this));
            d.setVisible(true);
            if (!d.isSelected()) return;

            User u = d.getSelectedUser();
            if (u == null || u.getEmail() == null || u.getEmail().isBlank()) return;

            if (!Ui.confirm(this, "Add approved player " + u.getEmail() + " to " + teamName + " ?")) return;

            dao.assignApprovedUserToTeam(u, teamId);

            Ui.info(this, "Player added to team.");
            load();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onTransfer() {
        if (readOnly) { Ui.error(this, "Read-only mode."); return; }

        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a player first"); return; }

        long playerId = ((Number) model.getValueAt(row, 0)).longValue();
        String gamerTag = String.valueOf(model.getValueAt(row, 1));

        try {
            List<ems.model.Team> others = teamDao.listByTournamentExcluding(tournamentId, teamId);
            if (others.isEmpty()) { Ui.error(this, "No other teams available in this tournament."); return; }

            JComboBox<TeamComboItem> box = new JComboBox<>();
            for (ems.model.Team t : others) box.addItem(new TeamComboItem(t.getId(), t.getName()));

            int ok = JOptionPane.showConfirmDialog(this, box, "Transfer " + gamerTag + " to:", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;

            TeamComboItem selected = (TeamComboItem) box.getSelectedItem();
            if (selected == null) return;

            if (!Ui.confirm(this, "Confirm transfer " + gamerTag + " to " + selected.getName() + " ?")) return;

            dao.transfer(playerId, selected.getId());
            Ui.info(this, "Transferred " + gamerTag + " to " + selected.getName());
            load();

        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onEdit() {
        if (readOnly) { Ui.error(this, "Read-only mode."); return; }

        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a player first"); return; }
        long id = ((Number) model.getValueAt(row, 0)).longValue();

        try {
            Player p = dao.findById(id).orElseThrow(() -> new IllegalStateException("Player not found"));
            PlayerFormDialog d = new PlayerFormDialog(this, teamId, p);
            d.setVisible(true);
            if (d.isSaved()) load();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onDelete() {
        if (readOnly) { Ui.error(this, "Read-only mode."); return; }

        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a player first"); return; }
        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String tag = String.valueOf(model.getValueAt(row, 1));

        if (!Ui.confirm(this, "Delete player: " + tag + " ?")) return;

        try {
            dao.delete(id);
            Ui.info(this, "Deleted player: " + tag);
            load();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static String nv(String s) { return s == null ? "" : s; }
    private static String formatDob(Date d) { return d == null ? "" : d.toString(); }
}