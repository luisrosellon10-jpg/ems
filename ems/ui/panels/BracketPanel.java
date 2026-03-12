package ems.ui.panels;

import ems.dao.MatchDao;
import ems.model.Match;
import ems.ui.Ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BracketPanel extends JPanel {

    private final MatchDao matchDao = new MatchDao();

    private final JTextArea area = new JTextArea();

    public BracketPanel() {
        super(new BorderLayout());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(area), BorderLayout.CENTER);
        clear();
    }

    public void clear() {
        area.setText("No tournament selected.");
    }

    public void load(long tournamentId) {
        try {
            StringBuilder sb = new StringBuilder();

            sb.append("QUARTERFINALS (QF)\n");
            appendStage(sb, tournamentId, "QF");

            sb.append("\nSEMIFINALS (SF)\n");
            appendStage(sb, tournamentId, "SF");

            sb.append("\nFINAL (F)\n");
            appendStage(sb, tournamentId, "F");

            area.setText(sb.toString());
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void appendStage(StringBuilder sb, long tournamentId, String stage) throws Exception {
        List<Match> list = matchDao.listByTournamentFiltered(tournamentId, stage);
        if (list.isEmpty()) {
            sb.append("  (none)\n");
            return;
        }
        for (Match m : list) {
            sb.append("  ")
              .append(m.getTeamA() != null ? m.getTeamA() : String.valueOf(m.getTeamAId()))
              .append(" ")
              .append(m.getScoreA() == null ? "-" : m.getScoreA())
              .append(" : ")
              .append(m.getScoreB() == null ? "-" : m.getScoreB())
              .append(" ")
              .append(m.getTeamB() != null ? m.getTeamB() : String.valueOf(m.getTeamBId()))
              .append(" [").append(m.getStatus()).append("]")
              .append("\n");
        }
    }
}