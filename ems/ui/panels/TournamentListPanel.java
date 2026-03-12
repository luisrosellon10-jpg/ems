package ems.ui.panels;

import ems.dao.TournamentDao;
import ems.model.Tournament;
import ems.ui.MatchListFrame;
import ems.ui.StandingsFrame;
import ems.ui.TeamListFrame;
import ems.ui.TournamentFormDialog;
import ems.ui.Ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class TournamentListPanel extends JPanel {

    private final TournamentSelectionListener selectionListener;
    private final boolean readOnly;

    private final TournamentDao dao = new TournamentDao();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Start Date", "End Date", "Status", "Created"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public TournamentListPanel(TournamentSelectionListener selectionListener) {
        this(selectionListener, false);
    }

    public TournamentListPanel(TournamentSelectionListener selectionListener, boolean readOnly) {
        super(new BorderLayout());
        this.selectionListener = selectionListener;
        this.readOnly = readOnly;

        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton standingsBtn = new JButton("Standings");
        JButton teamsBtn = new JButton("Teams");
        JButton matchesBtn = new JButton("Matches");

        refreshBtn.addActionListener(e -> loadData());
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        standingsBtn.addActionListener(e -> onStandings());
        teamsBtn.addActionListener(e -> onTeams());
        matchesBtn.addActionListener(e -> onMatches());

        // ✅ Read-only behavior (Viewer)
        addBtn.setVisible(!readOnly);
        editBtn.setVisible(!readOnly);
        deleteBtn.setVisible(!readOnly);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel(readOnly ? "Tournaments (Read-only)" : "Tournaments"));
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(editBtn);
        top.add(deleteBtn);
        top.add(standingsBtn);
        top.add(teamsBtn);
        top.add(matchesBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0) return;
            long id = ((Number) model.getValueAt(row, 0)).longValue();
            String name = String.valueOf(model.getValueAt(row, 1));
            if (this.selectionListener != null) this.selectionListener.onTournamentSelected(id, name);
        });

        loadData();
    }

    public void loadData() {
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
        if (readOnly) return;
        TournamentFormDialog d = new TournamentFormDialog(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                null
        );
        d.setVisible(true);
        if (d.isSaved()) loadData();
    }

    private void onEdit() {
        if (readOnly) return;

        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a tournament first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();

        try {
            Tournament t = dao.findById(id).orElseThrow(() -> new IllegalStateException("Tournament not found"));
            TournamentFormDialog d = new TournamentFormDialog(
                    (java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                    t
            );
            d.setVisible(true);
            if (d.isSaved()) loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onDelete() {
        if (readOnly) return;

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

    private void onTeams() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a tournament first"); return; }
        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String name = String.valueOf(model.getValueAt(row, 1));
        new TeamListFrame(id, name).setVisible(true);
    }

    private void onMatches() {
        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a tournament first"); return; }
        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String name = String.valueOf(model.getValueAt(row, 1));
        new MatchListFrame(id, name).setVisible(true);
    }

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