package ems.ui.panels;

import ems.dao.MatchDao;
import ems.dao.TeamDao;
import ems.dao.TournamentDao;
import ems.model.Match;
import ems.model.Team;
import ems.model.Tournament;
import ems.model.TournamentStatus;
import ems.ui.MatchListFrame;
import ems.ui.StandingsFrame;
import ems.ui.TeamListFrame;
import ems.ui.Ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TournamentOverviewPanel extends JPanel {
    
    private final boolean readOnly;

    private final TournamentDao tournamentDao = new TournamentDao();
    private final TeamDao teamDao = new TeamDao();
    private final MatchDao matchDao = new MatchDao();

    private long tournamentId = -1;
    private String tournamentName = "";

    private final JLabel title = new JLabel("Tournament Overview");
    private final JLabel meta = new JLabel("Select a tournament from the Tournaments tab.");

    private final JLabel teamsCount = new JLabel("-");
    private final JLabel matchesGroup = new JLabel("-");
    private final JLabel matchesQf = new JLabel("-");
    private final JLabel matchesSf = new JLabel("-");
    private final JLabel matchesF = new JLabel("-");
    private final JLabel nextAction = new JLabel("-");

    private final JButton openTeamsBtn = new JButton("Manage Teams");
    private final JButton openMatchesBtn = new JButton("Matches");
    private final JButton openStandingsBtn = new JButton("Standings");
    private final JButton refreshBtn = new JButton("Refresh");

    private final JButton activateBtn = new JButton("Activate");
    private final JButton cancelBtn = new JButton("Cancel");

    private final JButton genGroupBtn = new JButton("Generate GROUP");
    private final JButton genQfBtn = new JButton("Generate QF");
    private final JButton genSfBtn = new JButton("Generate SF");
    private final JButton genFinalBtn = new JButton("Generate Final");
    private final JButton championBtn = new JButton("Show Champion");

    private final BracketPanel bracketPanel = new BracketPanel();
    
    

    public TournamentOverviewPanel(boolean readOnly) {
    super(new BorderLayout(10, 10));
    this.readOnly = readOnly;

        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JPanel header = new JPanel(new GridLayout(0, 1));
        header.add(title);
        header.add(meta);

        JPanel stats = new JPanel(new GridLayout(0, 2, 8, 6));
        stats.setBorder(BorderFactory.createTitledBorder("Progress"));
        stats.add(new JLabel("Teams:"));
        stats.add(teamsCount);

        stats.add(new JLabel("GROUP matches:"));
        stats.add(matchesGroup);
        stats.add(new JLabel("QF matches:"));
        stats.add(matchesQf);
        stats.add(new JLabel("SF matches:"));
        stats.add(matchesSf);
        stats.add(new JLabel("Final matches:"));
        stats.add(matchesF);

        stats.add(new JLabel("Next action:"));
        stats.add(nextAction);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(refreshBtn);
        actions.add(openTeamsBtn);
        actions.add(openMatchesBtn);
        actions.add(openStandingsBtn);
        actions.add(activateBtn);
        actions.add(cancelBtn);

        actions.add(genGroupBtn);
        actions.add(genQfBtn);
        actions.add(genSfBtn);
        actions.add(genFinalBtn);
        actions.add(championBtn);

        // ---------- wiring ----------
        refreshBtn.addActionListener(e -> reload());

        openTeamsBtn.addActionListener(e -> {
            if (tournamentId < 0) return;
            new TeamListFrame(tournamentId, tournamentName).setVisible(true);
        });

        openMatchesBtn.addActionListener(e -> {
            if (tournamentId < 0) return;
            new MatchListFrame(tournamentId, tournamentName).setVisible(true);
        });

        openStandingsBtn.addActionListener(e -> {
            if (tournamentId < 0) return;
            new StandingsFrame(tournamentId, tournamentName).setVisible(true);
        });

        activateBtn.addActionListener(e -> {
            if (tournamentId < 0) return;
            try {
                tournamentDao.updateStatus(tournamentId, TournamentStatus.ACTIVE);
                reload();
            } catch (Exception ex) {
                Ui.error(this, ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> {
            if (tournamentId < 0) return;
            if (!Ui.confirm(this, "Cancel this tournament?")) return;
            try {
                tournamentDao.updateStatus(tournamentId, TournamentStatus.CANCELLED);
                reload();
            } catch (Exception ex) {
                Ui.error(this, ex.getMessage());
            }
        });

        genGroupBtn.addActionListener(e -> onGenerateGroup());
        genQfBtn.addActionListener(e -> onGenerateQf());
        genSfBtn.addActionListener(e -> onGenerateSf());
        genFinalBtn.addActionListener(e -> onGenerateFinal());

        championBtn.addActionListener(e -> {
            try {
                ems.service.TournamentWinnerService.Winner w =
                        new ems.service.TournamentWinnerService().getWinner(tournamentId);
                Ui.info(this, "Champion: " + w.teamName + " (Team ID: " + w.teamId + ")");
            } catch (Exception ex) {
                Ui.error(this, ex.getMessage());
            }
        });

        // ---------- layout ----------
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(header, BorderLayout.NORTH);
        top.add(actions, BorderLayout.CENTER);
        top.add(stats, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);

        bracketPanel.setBorder(BorderFactory.createTitledBorder("Bracket (QF / SF / Final)"));
        add(bracketPanel, BorderLayout.CENTER);

        setTournament(-1, "");
    }

    // called by ManagerWorkspaceFrame listener
    public void setTournament(long tournamentId, String tournamentName) {
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;

        if (tournamentId < 0) {
            meta.setText("Select a tournament from the Tournaments tab.");
            setCoreButtonsEnabled(false);
            bracketPanel.clear();
            return;
        }

        setCoreButtonsEnabled(true);
        reload();
    }
    public TournamentOverviewPanel() {
    this(false);
}
    private void setCoreButtonsEnabled(boolean enabled) {
        if (readOnly) {
        openTeamsBtn.setEnabled(false);   // viewer can still view teams later via dedicated read-only screens
        openMatchesBtn.setEnabled(false);
        activateBtn.setEnabled(false);
        cancelBtn.setEnabled(false);

        genGroupBtn.setEnabled(false);
        genQfBtn.setEnabled(false);
        genSfBtn.setEnabled(false);
        genFinalBtn.setEnabled(false);

    // championBtn is view-only; keep enabled when available
}
        refreshBtn.setEnabled(enabled);
        openTeamsBtn.setEnabled(enabled);
        openMatchesBtn.setEnabled(enabled);
        openStandingsBtn.setEnabled(enabled);

        activateBtn.setEnabled(enabled);
        cancelBtn.setEnabled(enabled);

        genGroupBtn.setEnabled(false);
        genQfBtn.setEnabled(false);
        genSfBtn.setEnabled(false);
        genFinalBtn.setEnabled(false);
        championBtn.setEnabled(false);
    }

    public void reload() {
        if (tournamentId < 0) return;

        try {
            Tournament t = tournamentDao.findById(tournamentId)
                    .orElseThrow(() -> new IllegalStateException("Tournament not found"));

            meta.setText("Selected: " + t.getName() + " | Status: " + t.getStatus());

            List<Team> teams = teamDao.listByTournament(tournamentId);
            teamsCount.setText(String.valueOf(teams.size()));

            int group = countStage("GROUP");
            int qf = countStage("QF");
            int sf = countStage("SF");
            int fin = countStage("F");

            matchesGroup.setText(String.valueOf(group));
            matchesQf.setText(String.valueOf(qf));
            matchesSf.setText(String.valueOf(sf));
            matchesF.setText(String.valueOf(fin));

            nextAction.setText(computeNextAction(t.getStatus(), teams.size(), group, qf, sf, fin));

            // status buttons
            activateBtn.setEnabled(t.getStatus() == TournamentStatus.DRAFT);
            cancelBtn.setEnabled(t.getStatus() == TournamentStatus.DRAFT || t.getStatus() == TournamentStatus.ACTIVE);

            // wizard enable/disable
            boolean active = t.getStatus() == TournamentStatus.ACTIVE;

            boolean groupDone = active && matchDao.allMatchesCompletedByTournamentAndStage(tournamentId, "GROUP");
            boolean qfDone = active && matchDao.allMatchesCompletedByTournamentAndStage(tournamentId, "QF")
                    && !matchDao.anyMatchDrawByTournamentAndStage(tournamentId, "QF");
            boolean sfDone = active && matchDao.allMatchesCompletedByTournamentAndStage(tournamentId, "SF")
                    && !matchDao.anyMatchDrawByTournamentAndStage(tournamentId, "SF");

            genGroupBtn.setEnabled(active && group == 0);
            genQfBtn.setEnabled(active && group > 0 && groupDone && qf == 0);
            genSfBtn.setEnabled(active && qf > 0 && qfDone && sf == 0);
            genFinalBtn.setEnabled(active && sf > 0 && sfDone && fin == 0);

            championBtn.setEnabled(fin > 0); // allow after completion too

            bracketPanel.load(tournamentId);

        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private int countStage(String stage) throws Exception {
        List<Match> list = matchDao.listByTournamentFiltered(tournamentId, stage);
        return list.size();
    }

    private void onGenerateGroup() {
        if (readOnly) { Ui.error(this, "Viewer cannot generate schedules."); return; }
        if (tournamentId < 0) return;
        try {
            Tournament t = tournamentDao.findById(tournamentId).orElseThrow();
            if (t.getStatus() != TournamentStatus.ACTIVE) {
                Ui.error(this, "Tournament must be ACTIVE.");
                return;
            }

            if (matchDao.existsByTournamentAndStage(tournamentId, "GROUP")) {
                Ui.error(this, "GROUP already exists.");
                return;
            }

            String start = JOptionPane.showInputDialog(
                    this,
                    "Start datetime (yyyy-mm-dd hh:mm:ss):",
                    java.time.LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ')
            );
            if (start == null || start.trim().isEmpty()) return;

            String daysStr = JOptionPane.showInputDialog(this, "Days between rounds:", "1");
            if (daysStr == null || daysStr.trim().isEmpty()) return;
            int daysBetween = Integer.parseInt(daysStr.trim());

            java.time.LocalDateTime startDt = java.time.LocalDateTime.parse(start.trim().replace(' ', 'T'));
            new ems.service.RoundRobinSchedulerService().generateDoubleRoundRobin(tournamentId, startDt, daysBetween);

            Ui.info(this, "GROUP schedule generated.");
            reload();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onGenerateQf() {
        if (tournamentId < 0) return;
        try {
            Tournament t = tournamentDao.findById(tournamentId).orElseThrow();
            if (t.getStatus() != TournamentStatus.ACTIVE) {
                Ui.error(this, "Tournament must be ACTIVE.");
                return;
            }

            if (!matchDao.allMatchesCompletedByTournamentAndStage(tournamentId, "GROUP")) {
                Ui.error(this, "Complete all GROUP matches first.");
                return;
            }

            if (matchDao.existsByTournamentAndStage(tournamentId, "QF")) {
                Ui.error(this, "QF already exists.");
                return;
            }

            String start = JOptionPane.showInputDialog(
                    this,
                    "QF datetime (yyyy-mm-dd hh:mm:ss):",
                    java.time.LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ')
            );
            if (start == null || start.trim().isEmpty()) return;

            java.time.LocalDateTime startDt = java.time.LocalDateTime.parse(start.trim().replace(' ', 'T'));
            new ems.service.KnockoutBracketService().generateTop8QuarterFinals(tournamentId, startDt);

            Ui.info(this, "QF generated.");
            reload();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onGenerateSf() {
        if (tournamentId < 0) return;
        try {
            Tournament t = tournamentDao.findById(tournamentId).orElseThrow();
            if (t.getStatus() != TournamentStatus.ACTIVE) {
                Ui.error(this, "Tournament must be ACTIVE.");
                return;
            }

            if (!matchDao.allMatchesCompletedByTournamentAndStage(tournamentId, "QF")) {
                Ui.error(this, "Complete all QF matches first.");
                return;
            }

            if (matchDao.anyMatchDrawByTournamentAndStage(tournamentId, "QF")) {
                Ui.error(this, "QF has a draw result. KO matches cannot draw.");
                return;
            }

            if (matchDao.existsByTournamentAndStage(tournamentId, "SF")) {
                Ui.error(this, "SF already exists.");
                return;
            }

            String start = JOptionPane.showInputDialog(
                    this,
                    "SF datetime (yyyy-mm-dd hh:mm:ss):",
                    java.time.LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ')
            );
            if (start == null || start.trim().isEmpty()) return;

            java.time.LocalDateTime startDt = java.time.LocalDateTime.parse(start.trim().replace(' ', 'T'));
            new ems.service.KnockoutBracketService().generateSemifinals(tournamentId, startDt);

            Ui.info(this, "SF generated.");
            reload();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onGenerateFinal() {
        if (tournamentId < 0) return;
        try {
            Tournament t = tournamentDao.findById(tournamentId).orElseThrow();
            if (t.getStatus() != TournamentStatus.ACTIVE) {
                Ui.error(this, "Tournament must be ACTIVE.");
                return;
            }

            if (!matchDao.allMatchesCompletedByTournamentAndStage(tournamentId, "SF")) {
                Ui.error(this, "Complete all SF matches first.");
                return;
            }

            if (matchDao.anyMatchDrawByTournamentAndStage(tournamentId, "SF")) {
                Ui.error(this, "SF has a draw result. KO matches cannot draw.");
                return;
            }

            if (matchDao.existsByTournamentAndStage(tournamentId, "F")) {
                Ui.error(this, "Final already exists.");
                return;
            }

            String start = JOptionPane.showInputDialog(
                    this,
                    "Final datetime (yyyy-mm-dd hh:mm:ss):",
                    java.time.LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ')
            );
            if (start == null || start.trim().isEmpty()) return;

            java.time.LocalDateTime startDt = java.time.LocalDateTime.parse(start.trim().replace(' ', 'T'));
            new ems.service.KnockoutBracketService().generateFinal(tournamentId, startDt);

            Ui.info(this, "Final generated.");
            reload();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static String computeNextAction(TournamentStatus status, int teamCount,
                                           int group, int qf, int sf, int fin) {
        if (status == TournamentStatus.CANCELLED) return "Tournament cancelled.";
        if (status == TournamentStatus.COMPLETED) return "Tournament completed. Show Champion.";

        if (teamCount == 0) return "Add teams.";
        if (status == TournamentStatus.DRAFT) return "Activate tournament to start scheduling.";

        if (group == 0) return "Generate GROUP schedule.";
        if (qf == 0) return "Complete GROUP results then Generate QF.";
        if (sf == 0) return "Complete QF results then Generate SF.";
        if (fin == 0) return "Complete SF results then Generate Final.";

        return "Enter Final result to complete tournament.";
    }
}