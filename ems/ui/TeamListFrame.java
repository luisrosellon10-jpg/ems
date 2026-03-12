package ems.ui;

import ems.dao.TeamDao;
import ems.model.Team;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TeamListFrame extends JFrame {

    private final long tournamentId;
    private final boolean readOnly;

    private final TeamDao dao = new TeamDao();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Created"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public TeamListFrame(long tournamentId, String tournamentName) {
        this(tournamentId, tournamentName, false);
    }

    public TeamListFrame(long tournamentId, String tournamentName, boolean readOnly) {
        this.tournamentId = tournamentId;
        this.readOnly = readOnly;

        setTitle("EMS - Teams (" + tournamentName + ")" + (readOnly ? " [READ-ONLY]" : ""));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(650, 420);
        setLocationRelativeTo(null);

        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add");
        JButton renameBtn = new JButton("Rename");
        JButton deleteBtn = new JButton("Delete");
        JButton playersBtn = new JButton("Players");

        refreshBtn.addActionListener(e -> load());
        addBtn.addActionListener(e -> onAdd());
        renameBtn.addActionListener(e -> onRename());
        deleteBtn.addActionListener(e -> onDelete());
        playersBtn.addActionListener(e -> onPlayers());

        // ✅ read-only
        addBtn.setVisible(!readOnly);
        renameBtn.setVisible(!readOnly);
        deleteBtn.setVisible(!readOnly);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(renameBtn);
        top.add(deleteBtn);
        top.add(playersBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            List<Team> list = dao.listByTournament(tournamentId);
            for (Team t : list) {
                model.addRow(new Object[]{t.getId(), t.getName(), t.getCreatedAt()});
            }
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onAdd() {
        if (readOnly) return;
        String name = JOptionPane.showInputDialog(this, "Team name:");
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) { Ui.error(this, "Team name is required"); return; }

        try {
            dao.insert(tournamentId, name);
            load();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onRename() {
        if (readOnly) return;
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a team first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String current = String.valueOf(model.getValueAt(row, 1));

        String name = JOptionPane.showInputDialog(this, "New team name:", current);
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) { Ui.error(this, "Team name is required"); return; }

        try {
            dao.update(id, name);
            load();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onPlayers() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a team first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String name = String.valueOf(model.getValueAt(row, 1));

        new PlayerListFrame(tournamentId, id, name, readOnly).setVisible(true);
    }

    private void onDelete() {
        if (readOnly) return;
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a team first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String name = String.valueOf(model.getValueAt(row, 1));

        if (!Ui.confirm(this, "Delete team: " + name + " ?")) return;

        try {
            dao.delete(id);
            load();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}