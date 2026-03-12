package ems.ui;

import ems.model.StandingRow;
import ems.service.StandingsService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StandingsFrame extends JFrame {

    private final long tournamentId;
    private final String tournamentName;

    private final StandingsService service = new StandingsService();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"#", "Team", "P", "W", "D", "L", "For", "Against", "Diff", "Pts"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public StandingsFrame(long tournamentId, String tournamentName) {
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;

        setTitle("EMS - Standings (" + tournamentName + ")");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(850, 420);
        setLocationRelativeTo(null);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> load());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            List<StandingRow> rows = service.computeStandings(tournamentId);
            int rank = 1;
            for (StandingRow r : rows) {
                model.addRow(new Object[]{
                        rank++,
                        r.getTeam(),
                        r.getPlayed(),
                        r.getWins(),
                        r.getDraws(),
                        r.getLosses(),
                        r.getGoalsFor(),
                        r.getGoalsAgainst(),
                        r.getGoalDiff(),
                        r.getPoints()
                });
            }
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }
}