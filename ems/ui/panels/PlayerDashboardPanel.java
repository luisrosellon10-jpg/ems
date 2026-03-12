package ems.ui.panels;

import ems.dao.MatchDao;
import ems.dao.PlayerDao;
import ems.dao.TeamDao;
import ems.model.Match;
import ems.model.Player;
import ems.model.Team;
import ems.model.User;
import ems.ui.PlayerListFrame;
import ems.ui.Ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class PlayerDashboardPanel extends JPanel {

    private final PlayerDao playerDao = new PlayerDao();
    private final TeamDao teamDao = new TeamDao();
    private final MatchDao matchDao = new MatchDao();

    private final User user;

    private final JLabel header = new JLabel("Player Dashboard");
    private final JLabel profileLine = new JLabel("-");
    private final JLabel teamLine = new JLabel("-");
    private final JLabel hintLine = new JLabel("Tip: Your account email must match players.contact_email to appear here.");

    private final JButton refreshBtn = new JButton("Refresh");
    private final JButton openTeamBtn = new JButton("My Team");
    private final JButton openMatchesBtn = new JButton("My Matches (All)");

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Stage", "Opponent", "Score", "Status", "Scheduled At"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    // cached after load
    private Player currentPlayer;
    private Team currentTeam;

    public PlayerDashboardPanel(User user) {
        super(new BorderLayout(10, 10));
        this.user = user;

        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));

        JPanel top = new JPanel(new GridLayout(0, 1, 6, 6));
        top.add(header);
        top.add(profileLine);
        top.add(teamLine);
        top.add(hintLine);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(refreshBtn);
        actions.add(openTeamBtn);
        actions.add(openMatchesBtn);

        refreshBtn.addActionListener(e -> load());

        openTeamBtn.addActionListener(e -> {
            if (currentTeam == null) {
                Ui.error(this, "No team assigned.");
                return;
            }
            // Reuse your existing players list window (it’s okay if it has edit buttons; for demo just view)
            new PlayerListFrame(currentTeam.getTournamentId(), currentTeam.getId(), currentTeam.getName(),false ).setVisible(true);
        });

        openMatchesBtn.addActionListener(e -> {
            if (currentTeam == null) {
                Ui.error(this, "No team assigned.");
                return;
            }
            Ui.info(this, "Open Matches from Manager view if needed.\nThis dashboard already lists your matches below.");
        });

        add(top, BorderLayout.NORTH);
        add(actions, BorderLayout.CENTER);
        add(new JScrollPane(table), BorderLayout.SOUTH);

        // better sizing for table area
        table.setFillsViewportHeight(true);
        ((JScrollPane) table.getParent().getParent()).setPreferredSize(new Dimension(900, 420));

        load();
    }

    private void load() {
        try {
            currentPlayer = null;
            currentTeam = null;

            profileLine.setText("Logged in: " + nv(user.getEmail()) + " | Role: " + user.getRole() + " | GamerTag: " + nv(user.getGamerTag()));
            model.setRowCount(0);

            Player p = playerDao.findByContactEmail(user.getEmail()).orElse(null);
            if (p == null) {
                teamLine.setText("No Player record found for this account.");
                openTeamBtn.setEnabled(false);
                openMatchesBtn.setEnabled(false);
                return;
            }
            currentPlayer = p;

            long teamId = p.getTeamId();
            if (teamId <= 0) {
                teamLine.setText("You are not assigned to any team yet.");
                openTeamBtn.setEnabled(false);
                openMatchesBtn.setEnabled(false);
                return;
            }

            Team team = teamDao.findById(teamId).orElse(null);
            if (team == null) {
                teamLine.setText("Team not found (team_id=" + teamId + ").");
                openTeamBtn.setEnabled(false);
                openMatchesBtn.setEnabled(false);
                return;
            }
            currentTeam = team;

            int teamSize = playerDao.countByTeam(teamId);
            teamLine.setText("Team: " + nv(team.getName()) + " (ID: " + teamId + ") | Teammates: " + teamSize);

            openTeamBtn.setEnabled(true);
            openMatchesBtn.setEnabled(true);

            List<Match> matches = matchDao.listByTeam(teamId);
            for (Match m : matches) {
                boolean iAmTeamA = (m.getTeamAId() != null && m.getTeamAId() == teamId);
                String myName = iAmTeamA ? teamName(m.getTeamA(), m.getTeamAId()) : teamName(m.getTeamB(), m.getTeamBId());
                String oppName = iAmTeamA ? teamName(m.getTeamB(), m.getTeamBId()) : teamName(m.getTeamA(), m.getTeamAId());

                String score;
                if (m.getScoreA() == null || m.getScoreB() == null) {
                    score = "-";
                } else {
                    score = iAmTeamA ? (m.getScoreA() + " : " + m.getScoreB()) : (m.getScoreB() + " : " + m.getScoreA());
                }

                // show "vs opponent" (my name is known already from teamLine)
                model.addRow(new Object[]{
                        m.getId(),
                        nv(m.getStage()),
                        "vs " + oppName,
                        score,
                        m.getStatus(),
                        formatTs(m.getScheduledAt())
                });
            }

        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static String teamName(String legacy, Long id) {
        if (legacy != null && !legacy.isBlank()) return legacy;
        return id == null ? "" : ("Team ID " + id);
    }

    private static String formatTs(Timestamp ts) {
        return ts == null ? "" : ts.toString();
    }

    private static String nv(String s) { return s == null ? "" : s; }
}