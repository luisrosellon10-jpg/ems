package ems.ui;

import ems.dao.TournamentDao;
import ems.model.Tournament;
import ems.model.TournamentStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class TournamentListFrame extends JFrame {
    
    private void onTeams() {
    int row = table.getSelectedRow();
    if (row < 0) { Ui.error(this, "Select a tournament first"); return; }
    long id = ((Number) model.getValueAt(row, 0)).longValue();
    String name = String.valueOf(model.getValueAt(row, 1));
    new TeamListFrame(id, name).setVisible(true);
}

    private final TournamentDao dao = new TournamentDao();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Start Date", "End Date", "Status", "Created"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public TournamentListFrame() {
        setTitle("EMS - Tournaments");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(900, 450);
        setLocationRelativeTo(null);

        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton standingsBtn = new JButton("Standings");
        JButton teamsBtn = new JButton("Teams");

        refreshBtn.addActionListener(e -> loadData());
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        standingsBtn.addActionListener(e -> onStandings());
        teamsBtn.addActionListener(e -> onTeams());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(editBtn);
        top.add(deleteBtn);
        top.add(standingsBtn);
        top.add(teamsBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        try {
            model.setRowCount(0);
            List<Tournament> list = dao.listAll();
            for (Tournament t : list) {
                model.addRow(new Object[]{
                        t.getId(),
                        nv(t.getName()),
                        formatDate(t.getStartDate()),
                        formatDate(t.getEndDate()),
                        t.getStatus(),
                        t.getCreatedAt()
                });
            }
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onAdd() {
        TournamentFormDialog d = new TournamentFormDialog(this, null);
        d.setVisible(true);
        if (d.isSaved()) loadData();
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a tournament first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();

        try {
            Tournament t = dao.findById(id).orElseThrow(() -> new IllegalStateException("Tournament not found"));
            TournamentFormDialog d = new TournamentFormDialog(this, t);
            d.setVisible(true);
            if (d.isSaved()) loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a tournament first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String name = String.valueOf(model.getValueAt(row, 1));

        if (!Ui.confirm(this, "Delete tournament: " + name + " ?")) return;

        try {
            dao.delete(id);
            Ui.info(this, "Deleted tournament: " + name);
            loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
    

// add method
private void onMatches() {
    int row = table.getSelectedRow();
    if (row < 0) { Ui.error(this, "Select a tournament first"); return; }
    long id = ((Number) model.getValueAt(row, 0)).longValue();
    String name = String.valueOf(model.getValueAt(row, 1));
    new MatchListFrame(id, name).setVisible(true);
}
    

// add method
    private void onStandings() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a tournament first"); return; }
        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String name = String.valueOf(model.getValueAt(row, 1));
        new StandingsFrame(id, name).setVisible(true);
}

    private static String nv(String s) { return s == null ? "" : s; }
    private static String formatDate(Date d) { return d == null ? "" : d.toString(); }
}