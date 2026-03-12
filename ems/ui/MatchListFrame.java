package ems.ui;

import ems.dao.MatchDao;
import ems.dao.TeamDao;
import ems.dao.TournamentDao;
import ems.model.Match;
import ems.model.Team;
import ems.model.Tournament;
import ems.model.TournamentStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchListFrame extends JFrame {
    

    private final long tournamentId;
    private final String tournamentName;

    private final MatchDao matchDao = new MatchDao();
    private final TeamDao teamDao = new TeamDao();
    private final TournamentDao tournamentDao = new TournamentDao();

    private final JButton refreshBtn = new JButton("Refresh");
    private final JButton addBtn = new JButton("Add");
    private final JButton editBtn = new JButton("Edit");
    private final JButton deleteBtn = new JButton("Delete");
    private final JButton genBtn = new JButton("Generate Schedule");
    private final JButton koBtn = new JButton("Generate KO (Top 8)");
    private final JButton sfBtn = new JButton("Generate SF");
    private final JButton fBtn  = new JButton("Generate Final");
    private final JButton champBtn = new JButton("Show Champion");

    private final JComboBox<String> stageFilterBox = new JComboBox<>(new String[]{
            "ALL", "GROUP", "QF", "SF", "F", "(NULL)"
    });

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Stage", "Team A", "Team B", "Scheduled At", "Score A", "Score B", "Status"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    public MatchListFrame(long tournamentId, String tournamentName) {
        this.tournamentId = tournamentId;
        this.tournamentName = tournamentName;

        setTitle("EMS - Matches (" + tournamentName + ")");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1050, 450);
        setLocationRelativeTo(null);

        refreshBtn.addActionListener(e -> loadData());
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        deleteBtn.addActionListener(e -> onDelete());
        champBtn.addActionListener(e -> onShowChampion());

        genBtn.addActionListener(e -> onGenerateSchedule());
        koBtn.addActionListener(e -> onGenerateKnockoutTop8());
        sfBtn.addActionListener(e -> onGenerateSemifinals());
        fBtn.addActionListener(e -> onGenerateFinal());

        stageFilterBox.addActionListener(e -> loadData());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);
        top.add(addBtn);
        top.add(editBtn);
        top.add(deleteBtn);
        top.add(champBtn);

        top.add(new JLabel("Stage:"));
        top.add(stageFilterBox);

        top.add(genBtn);
        top.add(koBtn);
        top.add(sfBtn);
        top.add(fBtn);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        applyTournamentLockUi();
        loadData();
    }

    private void applyTournamentLockUi() {
        try {
            Tournament t = tournamentDao.findById(this.tournamentId)
                    .orElseThrow(() -> new IllegalStateException("Tournament not found"));

            boolean canEdit = t.getStatus() == TournamentStatus.ACTIVE;

            addBtn.setEnabled(canEdit);
            editBtn.setEnabled(canEdit);
            deleteBtn.setEnabled(canEdit);

            genBtn.setEnabled(canEdit);
            koBtn.setEnabled(canEdit);
            sfBtn.setEnabled(canEdit);
            fBtn.setEnabled(canEdit);

            stageFilterBox.setEnabled(true);

            setTitle("EMS - Matches (" + tournamentName + ") - " + t.getStatus());
        } catch (Exception ex) {
            System.err.println("[EMS] Failed to apply tournament lock UI: " + ex.getMessage());
        }
    }

    private boolean ensureTournamentActive() {
    try {
        Tournament t = tournamentDao.findById(this.tournamentId)
                .orElseThrow(() -> new IllegalStateException("Tournament not found"));

        if (t.getStatus() != TournamentStatus.ACTIVE) {
            Ui.error(this, "Tournament must be ACTIVE to modify matches or generate stages. Current: " + t.getStatus());
            return false;
        }
        return true;
    } catch (Exception ex) {
        Ui.error(this, ex.getMessage());
        return false;
    }
}

    private void onShowChampion() {
        try {
            ems.service.TournamentWinnerService.Winner w =
                    new ems.service.TournamentWinnerService().getWinner(this.tournamentId);

            Ui.info(this, "Champion: " + w.teamName + " (Team ID: " + w.teamId + ")");
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void loadData() {
        try {
            Map<Long, String> teamNameById = new HashMap<>();
            List<Team> teams = teamDao.listByTournament(tournamentId);
            for (Team t : teams) {
                if (t.getName() != null) teamNameById.put(t.getId(), t.getName());
            }

            model.setRowCount(0);

            String stageFilter = (String) stageFilterBox.getSelectedItem();
            List<Match> list = matchDao.listByTournamentFiltered(tournamentId, stageFilter);

            for (Match m : list) {
                String teamAName = resolveTeamName(m.getTeamAId(), m.getTeamA(), teamNameById);
                String teamBName = resolveTeamName(m.getTeamBId(), m.getTeamB(), teamNameById);

                model.addRow(new Object[]{
                        m.getId(),
                        m.getStage(),
                        teamAName,
                        teamBName,
                        formatTs(m.getScheduledAt()),
                        m.getScoreA(),
                        m.getScoreB(),
                        m.getStatus()
                });
            }

            applyTournamentLockUi();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onGenerateSchedule() {
        if (!ensureTournamentActive()) return;
        try {
            if (matchDao.existsByTournamentAndStage(this.tournamentId, "GROUP")) {
                Ui.error(this, "Group schedule already exists (stage=GROUP).");
                return;
            }

            String start = JOptionPane.showInputDialog(
                    this,
                    "Start date/time (yyyy-mm-dd hh:mm:ss):",
                    java.time.LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ')
            );
            if (start == null) return;
            start = start.trim();
            if (start.isEmpty()) { Ui.error(this, "Start datetime is required"); return; }

            String daysStr = JOptionPane.showInputDialog(this, "Days between rounds (e.g., 1):", "1");
            if (daysStr == null) return;
            daysStr = daysStr.trim();
            int daysBetween = Integer.parseInt(daysStr);

            if (!Ui.confirm(this,
                    "Generate DOUBLE round-robin schedule?\n\n" +
                    "This will INSERT many matches (stage=GROUP).\n" +
                    "Start: " + start + "\n" +
                    "Days between rounds: " + daysBetween + "\n\n" +
                    "Proceed?")) return;

            java.time.LocalDateTime startDt = parseLdt(start);

            new ems.service.RoundRobinSchedulerService()
                    .generateDoubleRoundRobin(this.tournamentId, startDt, daysBetween);

            Ui.info(this, "Schedule generated.");
            loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onGenerateKnockoutTop8() {
        if (!ensureTournamentActive()) return;
        try {
            if (matchDao.existsByTournamentAndStage(this.tournamentId, "QF")) {
                Ui.error(this, "Quarterfinals already exist (stage=QF).");
                return;
            }

            String start = JOptionPane.showInputDialog(
                    this,
                    "Quarterfinal date/time (yyyy-mm-dd hh:mm:ss):",
                    java.time.LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ')
            );
            if (start == null) return;
            start = start.trim();
            if (start.isEmpty()) { Ui.error(this, "Start datetime is required"); return; }

            java.time.LocalDateTime startDt = parseLdt(start);

            if (!Ui.confirm(this,
                    "Generate TOP 8 Knockout (Quarterfinals)?\n\n" +
                    "This will INSERT 4 matches (stage=QF).\n" +
                    "Start: " + start + "\n\n" +
                    "Proceed?")) return;

            new ems.service.KnockoutBracketService()
                    .generateTop8QuarterFinals(this.tournamentId, startDt);

            Ui.info(this, "Knockout quarterfinals generated.");
            loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onGenerateSemifinals() {
        if (!ensureTournamentActive()) return;
        try {
            if (matchDao.existsByTournamentAndStage(this.tournamentId, "SF")) {
                Ui.error(this, "Semifinals already exist (stage=SF).");
                return;
            }

            String start = JOptionPane.showInputDialog(
                    this,
                    "Semifinal date/time (yyyy-mm-dd hh:mm:ss):",
                    java.time.LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ')
            );
            if (start == null) return;
            start = start.trim();
            if (start.isEmpty()) { Ui.error(this, "Start datetime is required"); return; }

            java.time.LocalDateTime startDt = parseLdt(start);

            if (!Ui.confirm(this,
                    "Generate Semifinals?\n\n" +
                    "Requires: 4 completed QF matches with non-draw scores.\n" +
                    "This will INSERT 2 matches (stage=SF).\n" +
                    "Start: " + start + "\n\n" +
                    "Proceed?")) return;

            new ems.service.KnockoutBracketService().generateSemifinals(this.tournamentId, startDt);

            Ui.info(this, "Semifinals generated.");
            loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onGenerateFinal() {
        if (!ensureTournamentActive()) return;
        try {
            if (matchDao.existsByTournamentAndStage(this.tournamentId, "F")) {
                Ui.error(this, "Final already exists (stage=F).");
                return;
            }

            String start = JOptionPane.showInputDialog(
                    this,
                    "Final date/time (yyyy-mm-dd hh:mm:ss):",
                    java.time.LocalDateTime.now().withSecond(0).withNano(0).toString().replace('T', ' ')
            );
            if (start == null) return;
            start = start.trim();
            if (start.isEmpty()) { Ui.error(this, "Start datetime is required"); return; }

            java.time.LocalDateTime startDt = parseLdt(start);

            if (!Ui.confirm(this,
                    "Generate Final?\n\n" +
                    "Requires: 2 completed SF matches with non-draw scores.\n" +
                    "This will INSERT 1 match (stage=F).\n" +
                    "Start: " + start + "\n\n" +
                    "Proceed?")) return;

            new ems.service.KnockoutBracketService().generateFinal(this.tournamentId, startDt);

            Ui.info(this, "Final generated.");
            loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static java.time.LocalDateTime parseLdt(String s) {
        String t = s.trim().replace('T', ' ');
        if (t.length() == 16) t = t + ":00"; // yyyy-mm-dd hh:mm
        return java.time.LocalDateTime.parse(t.replace(' ', 'T'));
    }

    private void onAdd() {
        if (!ensureTournamentActive()) return;
        MatchFormDialog d = new MatchFormDialog(this, tournamentId, null);
        d.setVisible(true);
        if (d.isSaved()) loadData();
    }

    private void onEdit() {
        if (!ensureTournamentActive()) return;

        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a match first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();

        try {
            Match m = matchDao.findById(id).orElseThrow(() -> new IllegalStateException("Match not found"));
            MatchFormDialog d = new MatchFormDialog(this, tournamentId, m);
            d.setVisible(true);
            if (d.isSaved()) loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private void onDelete() {
       if (!ensureTournamentActive()) return;

        int row = table.getSelectedRow();
        if (row < 0) { Ui.error(this, "Select a match first"); return; }

        long id = ((Number) model.getValueAt(row, 0)).longValue();
        String a = String.valueOf(model.getValueAt(row, 2));
        String b = String.valueOf(model.getValueAt(row, 3));

        if (!Ui.confirm(this, "Delete match: " + a + " vs " + b + " ?")) return;

        try {
            matchDao.delete(id);
            Ui.info(this, "Deleted match");
            loadData();
        } catch (Exception ex) {
            Ui.error(this, ex.getMessage());
        }
    }

    private static String resolveTeamName(Long teamId, String legacyName, Map<Long, String> teamNameById) {
        if (teamId != null) {
            String n = teamNameById.get(teamId);
            if (n != null && !n.isBlank()) return n;
        }
        return legacyName == null ? "" : legacyName;
    }

    private static String formatTs(Timestamp ts) {
        return ts == null ? "" : ts.toString();
    }
}