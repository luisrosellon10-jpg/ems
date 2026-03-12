package ems.ui.panels;

import ems.dao.TeamDao;
import ems.model.Team;
import ems.ui.PlayerListFrame;
import ems.ui.Ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TeamListPanel extends JPanel {

    private final long tournamentId;
    private final String tournamentName;

    private final TeamDao dao = new TeamDao();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Created"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public TeamListPanel(long tournamentId, String tournamentName) {
        super(new BorderLayout());
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;

        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add");
        JButton renameBtn = new JButton("Rename");
        JButton deleteBtn = new JButton("Delete");
        JButton playersBtn = new JButton("Players");

        refreshBtn.addActionListener(e -> reload());
        addBtn.addActionListener(e -> onAdd());
        renameBtn.addActionListener(e -> onRename());
        deleteBtn.addActionListener(e -> onDelete());
        playersBtn.addActionListener(e -> onPlayers());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Teams (" + tournamentName + ")"));
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(renameBtn);
        top.add(deleteBtn);
        top.add(playersBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        reload();
    }

    public void reload() {
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
        String name = JOptionPane.showInputDialog(this, "Team name:");
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) { Ui.error(this, "Team name is required"); return; }

        try {
            dao.insert(tournamentId, name);
            reload();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onRename() {
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
            reload();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onPlayers() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a team first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String name = String.valueOf(model.getValueAt(row, 1));

        // For now still opens a window; later we convert PlayerListFrame to a panel too.
        new PlayerListFrame(tournamentId, id, name).setVisible(true);
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a team first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String name = String.valueOf(model.getValueAt(row, 1));

        if (!Ui.confirm(this, "Delete team: " + name + " ?")) return;

        try {
            dao.delete(id);
            reload();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}